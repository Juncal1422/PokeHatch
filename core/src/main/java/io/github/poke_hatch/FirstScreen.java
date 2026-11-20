package io.github.poke_hatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.poke_hatch.Catalog.Item;
import io.github.poke_hatch.Catalog.Species;
import static io.github.poke_hatch.Catalog.contains;

/** Persistent navigation and input actors. Simulation updates only the affected widgets. */
public final class FirstScreen extends ScreenAdapter implements GameEngine.Listener {
  private static final float DETAIL_FRAME = .025f;
  private static final float[][] COIN_TRAJECTORIES = {
    {-42, 52}, {42, 52}, {0, 76}, {-24, 90}, {24, 90}
  };
  private static final float[][] HP_COLORS = {
    {1, .67f, .52f, .89f, .35f, .31f},
    {1, .91f, .53f, .93f, .7f, .25f},
    {.53f, .93f, .67f, .25f, .73f, .48f}
  };
  private final Main app;
  private final Stage stage = new Stage(new ScreenViewport());
  private final Catalog catalog = new Catalog();
  private final SaveStore store = new SaveStore(catalog);
  private final GameEngine game = new GameEngine(catalog, store.load());
  private final Table root = new Table(),
      content = new Table(),
      births = new Table(),
      coins = new Table(),
      effects = new Table();
  private final List<Runnable> bindings = new ArrayList<>();
  private final List<Actor> floatingCoins = new ArrayList<>();
  private final Map<Actor, Float> birthTargets = new IdentityHashMap<>();
  private final String[] revealedNames = new String[9];
  private final HatchSprite[] eggs = new HatchSprite[9];
  private final TextButton[] navButtons = new TextButton[5];
  private Table cards;
  private int cardCount, columns = 3;
  private final int[] eggGroups = new int[9];
  private Drawable neutralGroupPanel;
  private Drawable activeGroupPanel;
  private Drawable inactiveGroupPanel;
  private Drawable hatchedGroupPanel;
  private Drawable completeGroupPanel;
  private Drawable firstCoinPanel;
  private Drawable repeatCoinPanel;
  private Drawable shinyCoinPanel;
  private Drawable silverCoinPanel;
  private final String[] tabs = {"Criadero", "Pokédex", "Tienda", "Mochila", "Ajustes"};
  private final String[] shops = {
    "Criadero", "Mejoras", "Evolutivos", "Incubadoras", "Beneficios Temporales"
  };
  private final String[] sections = {
    "Económicos", "Cuidador", "Crianza", "Huevos", "Optimización", "Evolutivos"
  };
  private final String[] flutes = {
    "blue-flute",
    "yellow-flute",
    "red-flute",
    "black-flute",
    "white-flute",
    "azure-flute",
    "poke-flute",
    "eon-flute"
  };
  private Skin skin;
  private TextureRegion hpPixel;
  private TextButton.TextButtonStyle affordablePurchaseStyle;
  private TextButton.TextButtonStyle lockedPurchaseStyle;
  private Table wallet;
  private Label coinBalance, rcBalance;
  private ScrollPane scroll;
  private int tab, shop, slot, lastStep, gen, group, lastRevision = -1;
  private boolean registeredOnly, shinyOnly, readyOnly, paused;
  private boolean walletBuilt, walletShowsRc, gsBallListed;
  private String search = "";
  private DexGrid dexList;
  private Label dexResults;
  private Label farmStatus;
  private float elapsed, saveTime;
  private Runnable detailUpdate;
  private Dialog activeDetail;

  FirstScreen(Main app) {
    this.app = app;
    game.listener = this;
  }

  @Override
  public void show() {
    skin = new Skin(Gdx.files.internal("interface/skin.json"));
    configureFont(skin.getFont("Terminal"), 1.08f);
    configureFont(skin.getFont("Pixel"), 2.25f);
    configureFont(skin.getFont("Brand"), 1.08f);
    neutralGroupPanel = skin.getDrawable("panel");
    activeGroupPanel = skin.newDrawable("panel", new Color(.72f, .9f, .68f, 1));
    inactiveGroupPanel = skin.newDrawable("panel", new Color(.68f, .72f, .7f, 1));
    hatchedGroupPanel = skin.newDrawable("panel", new Color(.82f, .9f, .95f, 1));
    completeGroupPanel = skin.newDrawable("panel", new Color(1, .9f, .58f, 1));
    TextureRegion panelRegion = skin.getRegion("panel");
    hpPixel =
        new TextureRegion(
            panelRegion.getTexture(),
            panelRegion.getRegionX() + 8,
            panelRegion.getRegionY() + 8,
            1,
            1);
    firstCoinPanel = skin.newDrawable("panel", new Color(.42f, .9f, .94f, 1));
    repeatCoinPanel = skin.getDrawable("canvas");
    shinyCoinPanel = skin.newDrawable("panel", new Color(1, .77f, .24f, 1));
    silverCoinPanel = skin.newDrawable("panel", new Color(.78f, .84f, .9f, 1));
    affordablePurchaseStyle = coloredPurchaseStyle(new Color(.5f, .86f, .54f, 1));
    lockedPurchaseStyle = coloredPurchaseStyle(new Color(.94f, .42f, .37f, 1));
    TooltipManager.getInstance().initialTime = .25f;
    stage.addActor(root);
    root.setFillParent(true);
    root.setBackground(skin.getDrawable("canvas"));
    Table head = new Table();
    head.setBackground(skin.getDrawable("pine"));
    head.pad(10, 16, 10, 16);
    head.add(icon("egg")).size(28).padRight(10);
    head.add(new Label("PokéHatch", skin, "brand")).expandX().left();
    wallet = new Table();
    head.add(wallet).right();
    root.add(head).growX().row();
    Table nav = new Table();
    nav.setBackground(skin.getDrawable("panel"));
    nav.pad(3, 8, 3, 8);
    String[] icons = {"egg", "poke-ball", "coin-case", "explorer-kit", "go-goggles"};
    for (int i = 0; i < tabs.length; i++) {
      final int n = i;
      TextButton b =
          navButtons[i] =
              button(
                  tabs[i],
                  () -> {
                    tab = n;
                    rebuild();
                  });
      Label title = b.getLabel();
      b.clearChildren();
      b.add(icon(icons[i])).size(19).padRight(5);
      b.add(title);
      nav.add(b).expandX().fillX().height(34).pad(1);
    }
    root.add(nav).growX().row();
    scroll = new ScrollPane(content, skin);
    scroll.setScrollingDisabled(true, false);
    scroll.setFadeScrollBars(false);
    root.add(scroll).grow();
    for (Table rail : new Table[] {births, coins}) {
      rail.setTouchable(Touchable.disabled);
      stage.addActor(rail);
    }
    effects.setFillParent(true);
    effects.setTouchable(Touchable.disabled);
    stage.addActor(effects);
    stage.getRoot()
        .addCaptureListener(
            new InputListener() {
              @Override
              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Dialog detail = activeDetail;
                if (detail == null || !detail.isVisible()) return false;
                float halfWidth = detail.getWidth() * detail.getScaleX() / 2;
                float halfHeight = detail.getHeight() * detail.getScaleY() / 2;
                float centerX = detail.getX() + detail.getOriginX();
                float centerY = detail.getY() + detail.getOriginY();
                if (event.getStageX() >= centerX - halfWidth
                    && event.getStageX() <= centerX + halfWidth
                    && event.getStageY() >= centerY - halfHeight
                    && event.getStageY() <= centerY + halfHeight) return false;
                detail.hide();
                event.stop();
                return true;
              }
            });
    Gdx.input.setInputProcessor(stage);
    updateWallet();
    rebuild();
    if (store.blocked) message("Guardado ilegible. Puedes iniciar una partida desde Ajustes.");
  }

  private void configureFont(BitmapFont font, float scale) {
    font.getRegion()
        .getTexture()
        .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    font.setUseIntegerPositions(true);
    font.getData().setScale(scale);
  }

  private TextButton.TextButtonStyle coloredPurchaseStyle(Color color) {
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    Color hover = new Color(color).lerp(Color.WHITE, .16f);
    style.font = skin.getFont("Terminal");
    style.fontColor = skin.getColor("ink");
    style.disabledFontColor = skin.getColor("faded");
    style.up = skin.newDrawable("panel", color);
    style.over = skin.newDrawable("panel", hover);
    style.down = skin.getDrawable("pressed");
    style.disabled = skin.newDrawable("panel", color);
    return style;
  }

  private Label label(String text) {
    return new Label(text, skin);
  }

  private Image picture(String path) {
    Image image = new Image(app.getAssets().get(path, Texture.class));
    image.setScaling(Scaling.fit);
    return image;
  }

  private Image icon(String key) {
    return picture("art/" + key + ".png");
  }

  private String sprite(Species p, boolean shiny) {
    return "pokemon/" + (shiny ? "shiny" : "normal") + "/" + p.id + ".png";
  }

  private TextButton button(String name, Runnable run) {
    TextButton b = new TextButton(name, skin);
    change(b, run);
    return b;
  }

  private void change(Actor a, Runnable run) {
    a.addListener(
        new ChangeListener() {
          public void changed(ChangeEvent event, Actor actor) {
            run.run();
          }
        });
  }

  private void click(Actor a, Runnable run) {
    a.addListener(
        new ClickListener() {
          public void clicked(InputEvent event, float x, float y) {
            run.run();
          }
        });
  }

  private void heading(String title) {
    cards = null;
    Label l = new Label(title, skin, "pixel");
    l.setAlignment(com.badlogic.gdx.utils.Align.center);
    content.add(l).growX().padTop(16).padBottom(12).row();
  }

  private void rebuild() {
    TooltipManager.getInstance().hideAll();
    for (int n = 0; n < tabs.length; n++)
      navButtons[n].setStyle(
          skin.get(n == tab ? "active" : "default", TextButton.TextButtonStyle.class));
    cards = null;
    bindings.clear();
    Arrays.fill(eggs, null);

    dexList = null;
    dexResults = null;
    content.clearChildren();
    content.top().left().pad(8);
    slot = Math.min(slot, game.s.slots - 1);
    if (tab == 0) farm();
    else if (tab == 1) dex();
    else if (tab == 2) shop();
    else if (tab == 3) bag();
    else settings();
    scroll.setScrollY(0);
    refresh();
  }

  private void afterPurchase() {
    float y = scroll.getScrollY();
    rebuild();
    scroll.validate();
    scroll.setScrollY(y);
    scroll.updateVisualScroll();
    save();
  }

  private void save() {
    try {
      store.save(game.s);
    } catch (RuntimeException e) {
      message("No se pudo guardar la partida");
      Gdx.app.error("PokeHatch", "Guardado", e);
    }
  }

  private void farm() {
    heading("Criadero");
    Table meadowHeader = new Table();
    meadowHeader.pad(4, 10, 4, 10);
    meadowHeader.setBackground(skin.getDrawable("pine"));
    meadowHeader.add(new Label("Pradera de crianza", skin, "light")).left().expandX();
    Label stats = new Label("", skin, "light");
    meadowHeader.add(stats).right();
    bindings.add(
        () ->
            stats.setText(
                "Eclosiones " + game.s.total + " · Registrados " + game.registered()));
    content.add(meadowHeader).growX().row();
    Table field = new Table();
    final Texture meadow = app.getAssets().get("art/meadow.png", Texture.class);
    field.setBackground(
        new BaseDrawable() {
          public void draw(
              com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float w, float h) {
            float scale = Math.max(w / meadow.getWidth(), h / meadow.getHeight());
            int sw = Math.round(w / scale), sh = Math.round(h / scale);
            batch.draw(
                meadow,
                x,
                y,
                w,
                h,
                (meadow.getWidth() - sw) / 2,
                (meadow.getHeight() - sh) / 2,
                sw,
                sh,
                false,
                false);
          }
        });
    int[] order = {5, 3, 6, 1, 0, 2, 7, 4, 8};
    for (int i = 0; i < 9; i++) {
      final int j = order[i];
      Table nest = new Table();
      if (j < game.s.slots) {
        eggGroups[j] = game.s.group[j];
        HatchSprite egg =
            eggs[j] =
                new HatchSprite(icon("egg" + eggGroups[j]).getDrawable());
        nest.add(egg).size(58);
        egg.setOrigin(29, 0);
        click(
            nest,
            () -> {
              slot = j;
              game.tap(j);
              updateFarm();
            });
        if (game.has("silph-scope")) {
          HpBar bar = hpBar();
          nest.row();
          nest.add(bar).width(72).height(10).padTop(3);
          bindings.add(() -> setHpBarValue(bar, game.s.progress[j]));
        }
      }
      field.add(nest).size(100, 82).pad(5, 8, 4, 8);
      if (i % 3 == 2) field.row();
    }
    content
        .add(field)
        .growX()
        .height(Math.max(252, Math.min(340, stage.getViewport().getWorldHeight() * .48f)))
        .row();
    farmStatus = label("");
    Table status = new Table();
    status.setBackground(skin.getDrawable("panel"));
    status.pad(7, 12, 7, 12);
    status.add(farmStatus).expandX().left();
    content.add(status).growX().row();
    bindings.add(this::updateFarm);
    if (game.has("card-key")) {
      heading("Generaciones");
      Table row = new Table();
      content.add(row).left().row();
      for (int i = 1; i <= 5; i++) {
        final int n = i;
        CheckBox box = new CheckBox(catalog.regions[n - 1], skin);
        box.setChecked(game.s.gen[n]);
        change(
            box,
            () -> {
              if (game.s.gen[n] && count(game.s.gen) == 1) {
                box.setChecked(true);
                return;
              }
              game.s.gen[n] = box.isChecked();
              save();
            });
        row.add(box).pad(3);
      }
    }
    if (game.has("scope-lens")) {
      heading("Progreso por grupo");
      Table groups = new Table();
      float tileWidth = (stage.getViewport().getWorldWidth() - 34) / 3;
      int column = 0;
      for (Catalog.Group g : catalog.groups) {
        Table tile = new Table();
        tile.setBackground(neutralGroupPanel);
        tile.pad(4, 6, 4, 6);
        tile.add(label(g.name)).center().growX().row();
        tile.add(groupProgressContent(g, tile)).growX();
        groups.add(tile).width(tileWidth).pad(3).fillX().top();
        if (++column == 3) {
          groups.row();
          column = 0;
        }
      }
      content.add(groups).growX().row();
    }
  }

  private HpBar hpBar() {
    return new HpBar();
  }

  private void setHpBarValue(HpBar bar, double value) {
    bar.setValue(value);
  }

  private Table groupProgressContent(Catalog.Group group, Table panel) {
    Table row = new Table();
    Label progress = label("");
    HpBar bar = hpBar();
    progress.setAlignment(com.badlogic.gdx.utils.Align.center);
    row.add(progress).growX().center().row();
    row.add(bar).growX().height(10).padTop(3);
    bindings.add(
        () -> {
          int total = 0, hatched = 0;
          for (Species p : catalog.species)
            if (contains(p.pool, group.id)) {
              total++;
              if (game.s.counts[p.id] > 0) hatched++;
            }
          progress.setText(hatched + " / " + total);
          if (game.completeGroup(group.id)) progress.setColor(1, .65f, .1f, 1);
          else if (total > 0 && hatched == total) progress.setColor(.36f, .55f, .72f, 1);
          else progress.setColor(Color.WHITE);
          setHpBarValue(bar, total == 0 ? 0 : (double) hatched / total);
          if (panel != null) {
            panel.setBackground(
                game.completeGroup(group.id)
                    ? completeGroupPanel
                    : total > 0 && hatched == total ? hatchedGroupPanel : neutralGroupPanel);
          }
        });
    return row;
  }

  private final class HpBar extends Widget {
    private float value;

    void setValue(double value) {
      this.value = (float) Math.max(0, Math.min(1, value));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      float x = Math.round(getX()), y = Math.round(getY());
      float width = Math.max(0, Math.round(getWidth()));
      float height = Math.max(0, Math.round(getHeight()));
      if (width < 7 || height < 7) return;

      Color old = batch.getColor();
      Color actor = getColor();
      float oldR = old.r, oldG = old.g, oldB = old.b, oldA = old.a;
      float tintR = oldR * actor.r, tintG = oldG * actor.g, tintB = oldB * actor.b;
      float alpha = oldA * actor.a * parentAlpha;
      drawBarRect(batch, x, y, width, height, .22f, .28f, .31f, tintR, tintG, tintB, alpha);
      drawBarRect(
          batch, x + 1, y + height - 2, width - 2, 1, .91f, .91f, .79f, tintR, tintG, tintB, alpha);
      drawBarRect(
          batch, x + 1, y + 1, 1, height - 2, .91f, .91f, .79f, tintR, tintG, tintB, alpha);
      drawBarRect(
          batch, x + 2, y + 2, width - 4, height - 4, .28f, .32f, .35f, tintR, tintG, tintB, alpha);

      int zone = value <= .2f ? 0 : value <= .5f ? 1 : 2;
      float innerX = x + 3, innerY = y + 3, innerWidth = width - 6, innerHeight = height - 6;
      float fillWidth = Math.round(innerWidth * value);
      if (fillWidth > 0) {
        float bottomHeight = (float) Math.floor(innerHeight / 2);
        float topHeight = innerHeight - bottomHeight;
        drawBarRect(
            batch,
            innerX,
            innerY,
            fillWidth,
            bottomHeight,
            HP_COLORS[zone][3],
            HP_COLORS[zone][4],
            HP_COLORS[zone][5],
            tintR,
            tintG,
            tintB,
            alpha);
        drawBarRect(
            batch,
            innerX,
            innerY + bottomHeight,
            fillWidth,
            topHeight,
            HP_COLORS[zone][0],
            HP_COLORS[zone][1],
            HP_COLORS[zone][2],
            tintR,
            tintG,
            tintB,
            alpha);
      }
      batch.setColor(oldR, oldG, oldB, oldA);
    }

    private void drawBarRect(
        Batch batch,
        float x,
        float y,
        float width,
        float height,
        float red,
        float green,
        float blue,
        float tintR,
        float tintG,
        float tintB,
        float alpha) {
      batch.setColor(red * tintR, green * tintG, blue * tintB, alpha);
      batch.draw(hpPixel, x, y, width, height);
    }
  }

  private int count(boolean[] flags) {
    int n = 0;
    for (boolean b : flags) if (b) n++;
    return n;
  }

  private void updateFarm() {
    if (tab != 0) return;
    for (int i = 0; i < game.s.slots; i++)
      if (eggs[i] != null && eggGroups[i] != game.s.group[i]) {
        eggGroups[i] = game.s.group[i];
        eggs[i].next(icon("egg" + eggGroups[i]).getDrawable());
      }
    double p = game.s.progress[slot];
    int g = game.s.group[slot];
    String mood =
        !game.eligible(g)
            ? "En pausa por generación"
            : p > .75 ? "¡Se mueve!" : p > .35 ? "Se balancea suavemente" : "Está calentito";
    if (eggs[slot] != null && eggs[slot].revealing()) mood = "Ha nacido " + revealedNames[slot];
    farmStatus.setText(
        "Plaza "
            + (slot + 1)
            + " · "
            + catalog.byGroup[g].name
            + " · "
            + mood
            + "\nIncubadora "
            + game.s.level[slot]
            + " / 5");
  }

  public void step(int i) {
    lastStep = i;
    Image egg = eggs[i];
    if (egg == null || eggs[i].revealing()) return;
    egg.clearActions();
    egg.setRotation(0);
    egg.addAction(
        Actions.sequence(
            Actions.rotateTo(-5, .07f), Actions.rotateTo(4, .12f), Actions.rotateTo(0, .08f)));
  }

  public void hatch(
      Species p, boolean shiny, GameEngine.HatchReward reward, long amount, List<String> drops) {
    HatchSprite egg = eggs[lastStep];
    hatchToast(p, shiny, drops);
    if (amount > 0) coinToast(amount, egg, reward);
    if (egg != null) {
      revealedNames[lastStep] = p.name;
      eggGroups[lastStep] = game.s.group[lastStep];
      egg.next(icon("egg" + eggGroups[lastStep]).getDrawable());
      egg.reveal(picture(sprite(p, shiny)).getDrawable());
    }
    if (store.volume > 0)
      app.getAssets().get("cries/" + p.id + ".ogg", Sound.class).play(store.volume / 100f);
  }

  private void hatchToast(Species p, boolean shiny, List<String> drops) {
    Table line = new Table();
    line.setBackground(skin.getDrawable("panel"));
    line.add(picture(sprite(p, shiny))).size(42).pad(3);
    line.add(label((shiny ? "Shiny · " : "") + p.name)).pad(4);
    for (String id : drops) {
      line.add(icon(id)).size(28).padLeft(5);
      line.add(label(catalog.item.get(id).name)).pad(3);
    }
    line.pack();
    addToast(births, line, 9, 2.4f);
  }

  private void coinToast(long value) {
    toast(coins, icon("poke-coin"), "+" + value, 6, .67f);
  }

  private void coinToast(long value, Actor source, GameEngine.HatchReward reward) {
    if (source == null) {
      coinToast(value);
      return;
    }
    if (floatingCoins.size() >= 6) {
      Actor oldest = floatingCoins.remove(0);
      oldest.clearActions();
      oldest.remove();
    }
    Table popup = new Table();
    popup.setBackground(coinPanel(reward));
    popup.pad(3, 8, 3, 8);
    popup.add(icon("poke-coin")).size(19).padRight(4);
    popup.add(label("+" + value));
    popup.pack();
    popup.setOrigin(popup.getWidth() / 2, popup.getHeight() / 2);
    Vector2 origin =
        source.localToStageCoordinates(new Vector2(source.getWidth() / 2, source.getHeight() / 2));
    popup.setPosition(origin.x - popup.getWidth() / 2, origin.y - popup.getHeight() / 2);
    popup.setScale(.2f);
    popup.getColor().a = 0;
    effects.addActor(popup);
    floatingCoins.add(popup);
    float[] trajectory =
        COIN_TRAJECTORIES[(int) (Math.random() * COIN_TRAJECTORIES.length)];
    popup.addAction(
        Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1, 1, .22f, Interpolation.elasticOut),
                Actions.sequence(
                    Actions.fadeIn(.08f),
                    Actions.parallel(
                        Actions.moveBy(trajectory[0], trajectory[1], .7f, Interpolation.sineOut),
                        Actions.fadeOut(.7f)))),
            Actions.run(() -> floatingCoins.remove(popup)),
            Actions.removeActor()));
  }

  private Drawable coinPanel(GameEngine.HatchReward reward) {
    switch (reward) {
      case FIRST:
        return firstCoinPanel;
      case SHINY:
        return shinyCoinPanel;
      case SHINY_REPEAT:
        return silverCoinPanel;
      default:
        return repeatCoinPanel;
    }
  }

  private void toast(Table rail, Image image, String text, int limit, float seconds) {
    Table line = new Table();
    line.setBackground(skin.getDrawable("panel"));
    if (image != null) line.add(image).size(30);
    line.add(label(text)).pad(3);
    line.pack();
    addToast(rail, line, limit, seconds);
  }

  private void addToast(Table rail, Table line, int limit, float seconds) {
    if (rail.getChildren().size >= limit) {
      Actor oldest = rail.getChildren().first();
      oldest.remove();
      birthTargets.remove(oldest);
    }
    rail.addActor(line);
    line.addAction(
        Actions.sequence(
            Actions.delay(seconds),
            Actions.fadeOut(.15f),
            Actions.run(() -> birthTargets.remove(line)),
            Actions.removeActor()));
  }

  private void positionRails() {
    float h = stage.getViewport().getWorldHeight(), w = stage.getViewport().getWorldWidth();
    float y = 6;
    for (int i = births.getChildren().size - 1; i >= 0; i--) {
      Actor a = births.getChildren().get(i);
      Float previousTarget = birthTargets.put(a, y);
      if (previousTarget == null) a.setPosition(8, y);
      else if (Math.abs(previousTarget - y) > .5f) {
        for (int j = a.getActions().size - 1; j >= 0; j--) {
          Action action = a.getActions().get(j);
          if (action instanceof MoveToAction) a.removeAction(action);
        }
        a.addAction(Actions.moveTo(8, y, .14f, Interpolation.sineOut));
      }
      y += a.getHeight() + 1;
    }
    y = h - 64;
    for (Actor a : coins.getChildren()) {
      y -= a.getHeight();
      a.setPosition(w - a.getWidth() - 12, y);
    }
    births.toFront();
    coins.toFront();
    effects.toFront();
  }

  private void message(String text) {
    toast(births, null, text, 9, 3);
  }

  private Table row(String key, String title, String description) {
    Table r = card(key, title);
    if (!description.isEmpty()) subtitle(r, description);
    return r;
  }

  private Table card(String key, String title) {
    if (cards == null) {
      cards = new Table();
      cards.left();
      content.add(cards).growX().row();
      cardCount = 0;
    }
    if (cardCount > 0 && cardCount % columns == 0) cards.row();
    Table tile = new Table();
    tile.top().center().pad(7, 8, 7, 8);
    tile.setBackground(skin.getDrawable("panel"));
    Label name = label(title);
    name.setWrap(true);
    name.setAlignment(com.badlogic.gdx.utils.Align.center);
    tile.add(name).minWidth(0).growX().center().row();
    tile.add(icon(key)).size(52).center().pad(4).row();
    float width = (stage.getViewport().getWorldWidth() - 36) / columns;
    cards.add(tile).width(width - 4).pad(2).fillY().top();
    cardCount++;
    return tile;
  }

  private Image cardIcon(Table card) {
    for (Actor child : card.getChildren())
      if (child instanceof Image) return (Image) child;
    throw new IllegalStateException("Card has no item icon");
  }

  private void denyPurchase(Actor button) {
    button.addAction(
        Actions.sequence(
            Actions.moveBy(-5, 0, .035f),
            Actions.moveBy(10, 0, .06f),
            Actions.moveBy(-10, 0, .06f),
            Actions.moveBy(5, 0, .035f)));
  }

  private void animateItemPurchase(Image source) {
    Vector2 start =
        source.localToStageCoordinates(new Vector2(source.getWidth() / 2, source.getHeight() / 2));
    TextButton backpack = navButtons[3];
    Vector2 end =
        backpack.localToStageCoordinates(
            new Vector2(backpack.getWidth() / 2, backpack.getHeight() / 2));
    Image duplicate = new Image(source.getDrawable(), Scaling.fit);
    duplicate.setSize(52, 52);
    duplicate.setOrigin(26, 26);
    duplicate.setPosition(start.x - 26, start.y - 26);
    duplicate.setScale(.22f);
    effects.addActor(duplicate);
    duplicate.addAction(
        Actions.sequence(
            Actions.parallel(
                Actions.sequence(
                    Actions.scaleTo(1.15f, 1.15f, .22f, Interpolation.elasticOut),
                    Actions.scaleTo(.18f, .18f, .3f, Interpolation.sineIn)),
                Actions.moveTo(end.x - 26, end.y - 26, .52f, Interpolation.sine)),
            Actions.removeActor()));
  }

  private Label subtitle(Table tile, String text) {
    Label l = label(text);
    l.setWrap(true);
    l.setColor(skin.getColor("faded"));
    tile.add(l).minWidth(0).growX().left().row();
    return l;
  }

  private Table currencySubtitle(Table tile, boolean rc) {
    Table line = new Table();
    Label text = label("");
    text.setWrap(true);
    text.setColor(skin.getColor("faded"));
    line.add(text).minWidth(0).growX().left();
    line.add(icon(rc ? "reset-urge" : "poke-coin")).size(15).padLeft(3);
    tile.add(line).minWidth(0).growX().left().row();
    return line;
  }

  private void setCurrencySubtitle(Table line, String text, boolean showIcon) {
    ((Label) line.getChildren().get(0)).setText(text);
    Image symbol = (Image) line.getChildren().get(1);
    symbol.setVisible(showIcon);
    line.getCell(symbol).width(showIcon ? 15 : 0);
    line.invalidateHierarchy();
  }

  private Image addCurrencyIcon(TextButton button, boolean rc) {
    Label text = button.getLabel();
    button.clearChildren();
    button.add(text).growX().fillX();
    Image symbol = icon(rc ? "reset-urge" : "poke-coin");
    button.add(symbol).size(16).padLeft(3);
    symbol.setVisible(false);
    return symbol;
  }

  private void shop() {
    heading("Tienda");
    gsBallListed = false;
    Table bar = new Table();
    content.add(bar).left().row();
    for (int i = 0; i < (game.s.trips > 0 ? 5 : 4); i++) {
      final int n = i;
      TextButton category =
          button(
              shops[i],
              () -> {
                shop = n;
                rebuild();
              });
      if (n == shop) category.setStyle(skin.get("active", TextButton.TextButtonStyle.class));
      bar.add(category).height(30).pad(2);
    }
    if (shop == 0) {
      heading("Grupos de huevo");
      for (Catalog.Group g : catalog.groups) {
        Table r = card("egg" + g.id, g.name);
        TextButton b =
            button(
                "",
                () -> {
                  if (!game.s.open[g.id]) game.unlock(g);
                  else if (game.has("oval-charm")) {
                    if (game.s.active[g.id] && count(game.s.active) == 1) {
                      message("Debe quedar un grupo activo");
                      return;
                    }
                    game.s.active[g.id] = !game.s.active[g.id];
                  }
                  afterPurchase();
                });
        Image currency = addCurrencyIcon(b, false);
        r.add(b).growX().height(30).row();
        if (game.has("scope-lens")) r.add(groupProgressContent(g, null)).growX().row();
        Table req = currencySubtitle(r, false);
        bindings.add(
            () -> {
              boolean forSale = !game.s.open[g.id];
              boolean active = game.s.active[g.id];
              r.setBackground(
                  !game.s.open[g.id]
                      ? neutralGroupPanel
                      : active ? activeGroupPanel : inactiveGroupPanel);
              b.setStyle(
                  skin.get(
                      forSale ? "buy" : active ? "active" : "default",
                      TextButton.TextButtonStyle.class));
              setCurrencySubtitle(
                  req,
                  forSale
                      ? "Desbloquea el grupo por " + game.price(g.cost)
                      : game.has("oval-charm")
                          ? "Selecciona los grupos activos"
                          : "Amuleto Oval para alternar",
                  forSale);
              b.setText(
                  game.s.open[g.id]
                      ? (game.s.active[g.id] ? "Activo" : "Inactivo")
                      : Integer.toString(game.price(g.cost)));
              currency.setVisible(forSale);
              b.setDisabled(
                  game.s.open[g.id] ? !game.has("oval-charm") : game.s.coins < game.price(g.cost));
            });
      }
      heading("Plazas adicionales");
      for (int j = 0; j < 8; j++) {
        final int i = j;
        Table r = row(flutes[i], "Plaza " + (i + 2), "");
        TextButton b =
            button(
                "",
                () -> {
                  if (i == game.s.slots - 1 && game.expand()) afterPurchase();
                });
        Image currency = addCurrencyIcon(b, false);
        r.add(b).growX().height(30).row();
        Table requirement = currencySubtitle(r, false);
        bindings.add(
            () -> {
              int cost = game.price(catalog.slots[i]);
              boolean hasPrice = i <= game.s.slots - 1;
              setCurrencySubtitle(
                  requirement,
                  i > game.s.slots - 1
                      ? "Adquiere la plaza " + (i + 1)
                      : "Precio · " + cost,
                  hasPrice);
              b.setText(
                  i < game.s.slots - 1
                      ? "Adquirida"
                      : i > game.s.slots - 1 ? "Bloqueado" : Integer.toString(cost));
              currency.setVisible(i == game.s.slots - 1);
              b.setDisabled(i != game.s.slots - 1 || cost > game.s.coins);
            });
      }
      Item gsBall = catalog.item.get("gs-ball");
      gsBallListed = game.gate(gsBall).isEmpty();
      if (gsBallListed) {
        heading("Viaje al pasado");
        item(gsBall, true);
      }
      return;
    }
    if (shop == 3) {
      heading("Incubadoras");
      for (int j = 0; j < game.s.slots; j++) {
        final int i = j;
        int lv = game.s.level[i];
        Table r = row("egg" + game.s.group[i], "Plaza " + (i + 1), "Nivel " + lv + " / 5");
        int price = lv == 5 ? 0 : game.price(game.levels[lv]);
        TextButton b =
            button(
                lv == 5 ? "Nivel máximo" : Integer.toString(price),
                () -> {
                  if (game.level(i)) afterPurchase();
                });
        Image currency = addCurrencyIcon(b, false);
        currency.setVisible(lv != 5);
        r.add(b).growX().height(30).row();
        bindings.add(() -> b.setDisabled(lv == 5 || price > game.s.coins));
      }
    } else if (shop == 4) {
      List<Item> temporal = new ArrayList<>();
      for (Item i : catalog.items) if (i.kind.equals("RC")) temporal.add(i);
      temporal.sort(Comparator.comparingInt(i -> i.price));
      for (int g = 0; g < 2; g++) {
        heading(g == 0 ? "Beneficios Temporales" : "Huevos");
        for (Item i : temporal) if ((i.group > 0) == (g == 1)) item(i, true);
      }
    } else {
      for (String category : sections)
        if (category.equals("Evolutivos") == (shop == 2)) {
          heading(category);
          for (Item i : catalog.items)
            if (i.kind.equals("PC") && i.section.equals(category) && !i.id.equals("gs-ball"))
              item(i, true);
        }
    }
  }

  private void item(Item i, boolean sell) {
    Table r = card(i.id, i.name);
    TextTooltip tip = new TextTooltip(i.desc.replaceFirst("\\.$", ""), skin);
    tip.setInstant(false);
    r.addListener(tip);
    if (!sell) {
      TextButton owned = new TextButton("Adquirido", skin);
      owned.setDisabled(true);
      r.add(owned).growX().height(28).row();
      String rule = game.requirement(i, false);
      if (!i.source.isEmpty()) subtitle(r, i.source);
      else if (!rule.isEmpty()) subtitle(r, rule);
      else {
        Table price = currencySubtitle(r, i.kind.equals("RC"));
        setCurrencySubtitle(price, "Compra por " + i.price, true);
      }
      return;
    }
    TextButton b = new TextButton("", skin);
    b.setStyle(skin.get("buy", TextButton.TextButtonStyle.class));
    Image currency = addCurrencyIcon(b, i.kind.equals("RC"));
    r.add(b).growX().height(30).row();
    Label gate = subtitle(r, "");
    change(
        b,
        () -> {
          Runnable purchase =
              () -> {
                if (game.buy(i)) {
                  if (i.id.equals("gs-ball")) {
                    births.clearChildren();
                    coins.clearChildren();
                    effects.clearChildren();
                    floatingCoins.clear();
                    birthTargets.clear();
                    tab = 0;
                    shop = 4;
                    rebuild();
                    save();
                  } else {
                    if (shop == 1 || shop == 2 || shop == 4) animateItemPurchase(cardIcon(r));
                    afterPurchase();
                  }
                } else denyPurchase(b);
              };
          if (i.id.equals("gs-ball")) confirmTravel(purchase);
          else purchase.run();
        });
    bindings.add(
        () -> {
          String reason = game.gate(i);
          boolean owned = game.has(i.id);
          int cost = game.price(i);
          String caption =
              owned
                  ? "Adquirido"
                  : !reason.isEmpty() ? "Bloqueado" : Integer.toString(cost);
          String rule = game.requirement(i, false);
          gate.setText(
              !reason.isEmpty()
                  ? reason
                  : !rule.isEmpty()
                      ? rule
                      : "");
          b.setText(caption);
          currency.setVisible(!owned && reason.isEmpty());
          if (owned) b.setStyle(skin.get("default", TextButton.TextButtonStyle.class));
          else if (!reason.isEmpty()) b.setStyle(lockedPurchaseStyle);
          else {
            long balance = i.kind.equals("RC") ? game.s.rc : game.s.coins;
            b.setStyle(
                balance >= cost
                    ? affordablePurchaseStyle
                    : skin.get("buy", TextButton.TextButtonStyle.class));
          }
          b.setDisabled(owned);
        });
  }

  private int bagSize;

  private void bag() {
    heading("Mochila");
    bagSize = game.s.owned.size() + game.s.benefits.size() + game.evolutions() / 8;
    for (String category : sections) {
      boolean started = false;
      for (Item i : catalog.items)
        if (game.s.owned.contains(i.id) && i.section.equals(category)) {
          if (!started) {
            heading(category);
            started = true;
          }
          item(i, false);
        }
    }
    if (!game.s.benefits.isEmpty()) {
      heading("Beneficios Temporales");
      for (Item i : catalog.items) if (game.s.benefits.contains(i.id)) item(i, false);
    }
    heading("Medallas");
    int n = Math.min(catalog.badges.length, game.evolutions() / 8);
    if (n == 0)
      content.add(label("Consigue una medalla por cada 8 evoluciones registradas")).left().row();
    for (int i = 0; i < n; i++)
      row("badge-" + catalog.badges[i], "Medalla " + catalog.badgeNames[i], catalog.regions[i / 8]);
  }

  private void dex() {
    heading("Pokédex");
    Table filters = new Table();
    content.add(filters).growX().row();
    TextField field = new TextField(search, skin);
    field.setMessageText("Nombre o número");
    field.setTextFieldListener(
        (f, ch) -> {
          search = f.getText();

          filterDex();
        });
    filters.add(field).growX();
    SelectBox<String> gens = new SelectBox<>(skin);
    String[] gn = new String[6];
    gn[0] = "Generaciones";
    System.arraycopy(catalog.regions, 0, gn, 1, 5);
    gens.setItems(gn);
    gens.setSelectedIndex(gen);
    filters.add(gens);
    change(
        gens,
        () -> {
          gen = gens.getSelectedIndex();

          filterDex();
        });
    SelectBox<String> groups = new SelectBox<>(skin);
    String[] names = new String[catalog.groups.length + 1];
    names[0] = "Grupos";
    for (int j = 0; j < catalog.groups.length; j++) names[j + 1] = catalog.groups[j].name;
    groups.setItems(names);
    groups.setSelectedIndex(group);
    filters.add(groups);
    change(
        groups,
        () -> {
          group = groups.getSelectedIndex();

          filterDex();
        });
    Table flags = new Table();
    content.add(flags).left().row();
    CheckBox only = new CheckBox("Registrados", skin),
        ready = new CheckBox("Listos para evolucionar", skin),
        sh = new CheckBox("Shiny", skin);
    only.setChecked(registeredOnly);
    ready.setChecked(readyOnly);
    sh.setChecked(shinyOnly);
    for (CheckBox box : new CheckBox[] {only, ready, sh}) {
      flags.add(box).pad(3);
      change(
          box,
          () -> {
            registeredOnly = only.isChecked();
            readyOnly = ready.isChecked();
            shinyOnly = sh.isChecked();

            filterDex();
          });
    }
    dexResults = label("");
    content.add(dexResults).left().padBottom(4).row();
    dexList = new DexGrid(app, game, skin, this::detail);
    content.add(dexList).growX().row();
    entries();
  }

  private void filterDex() {
    scroll.setScrollY(0);
    entries();
  }

  private void entries() {
    if (dexList == null) return;
    List<Species> matches = new ArrayList<>();
    boolean[] parents = new boolean[650];
    if (readyOnly) for (Species p : catalog.species) if (game.ready(p)) parents[p.parent] = true;
    String q = search.toLowerCase(Locale.ROOT);
    for (Species p : catalog.species) {
      if ((gen > 0 && p.gen != gen)
          || (group > 0 && !contains(p.groups, catalog.groups[group - 1].id))) continue;
      if ((registeredOnly && !game.s.dex[p.id])
          || (shinyOnly && !game.s.shiny[p.id])
          || (readyOnly && !parents[p.id])) continue;
      if (!p.name.toLowerCase(Locale.ROOT).contains(q)
          && !Integer.toString(1000 + p.id).substring(1).contains(q)) continue;
      matches.add(p);
    }
    dexResults.setText(matches.size() + " resultados");
    dexList.setEntries(matches);
  }

  private void detail(Species p) {
    Dialog d =
        new Dialog("N.º " + p.id + " · " + p.name, skin) {
          private boolean closing;

          public void hide() {
            if (closing) return;
            closing = true;
            clearActions();
            if (activeDetail == this) activeDetail = null;
            detailUpdate = null;
            setModal(false);
            setTouchable(Touchable.disabled);
            super.hide(
                Actions.sequence(
                    Actions.scaleTo(getScaleX() * .82f, getScaleY() * .82f, DETAIL_FRAME),
                    Actions.scaleTo(getScaleX() * .60f, getScaleY() * .60f, DETAIL_FRAME),
                    Actions.scaleTo(getScaleX() * .35f, getScaleY() * .35f, DETAIL_FRAME),
                    Actions.scaleTo(getScaleX() * .12f, getScaleY() * .12f, DETAIL_FRAME)));
          }
        };
    Table t = d.getContentTable();
    t.defaults().pad(3);
    Image img = picture(sprite(p, game.s.shiny[p.id]));
    CheckBox sh = new CheckBox("Shiny", skin);
    sh.setChecked(game.s.shiny[p.id]);
    sh.setVisible(game.s.shiny[p.id]);
    change(sh, () -> img.setDrawable(picture(sprite(p, sh.isChecked())).getDrawable()));
    click(img, () -> evolveFirstReady(p, d));
    t.add(img).size(80);
    t.add(sh).left().row();
    StringBuilder groups = new StringBuilder();
    for (int g : p.groups) {
      if (groups.length() > 0) groups.append(" / ");
      groups.append(catalog.byGroup[g].name);
    }
    t.add(label("Gen. " + p.gen + " · " + groups)).colspan(2).row();
    String condition;
    if (p.id == 292) condition = "Se registra al evolucionar a Nincada en Ninjask";
    else if (p.parent == 0) {
      StringJoiner pools = new StringJoiner(" o ");
      for (int g : p.pool) pools.add(catalog.byGroup[g].name);
      condition = p.pool.length == 0 ? "Sin método de obtención" : "Nace en " + pools;
    } else {
      condition =
          game.required(p)
              + " crías de "
              + catalog.byId[p.root].name
              + (p.extra ? " después de registrar a " + catalog.byId[p.parent].name : " en total")
              + (p.item.isEmpty() ? "" : " y " + catalog.item.get(p.item).name)
              + (p.id == 291 ? ". También registra a Shedinja" : "");
    }
    Label how = label(condition);
    how.setWrap(true);
    float detailWidth = Math.min(400, Math.max(120, stage.getViewport().getWorldWidth() - 72));
    t.add(how).width(detailWidth).colspan(2).row();
    Label count = label("");
    t.add(count).colspan(2).row();
    List<Runnable> live = new ArrayList<>();
    for (Species child : catalog.species)
      if (child.parent == p.id && child.id != 292) {
        Label rule = label("");
        rule.setEllipsis(true);
        t.add(rule).left().minWidth(0).growX();
        TextButton evolve =
            button(
                "Evolucionar",
                () -> evolveDetail(child, d));
        t.add(evolve).row();
        live.add(
            () -> {
              rule.setText(
                  child.name
                      + "   "
                      + Math.max(0, game.progress(child))
                      + " / "
                      + game.required(child)
                      + (child.item.isEmpty() ? "" : " · " + catalog.item.get(child.item).name));
              evolve.setDisabled(!game.ready(child));
            });
      }
    detailUpdate =
        () -> {
          count.setText(
              "Crías de " + catalog.byId[p.root].name + " obtenidas: " + game.s.counts[p.root]);
          sh.setVisible(game.s.shiny[p.id]);
          for (Runnable r : live) r.run();
        };
    detailUpdate.run();
    d.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Input.Keys.SPACE) return false;
            if (!evolveFirstReady(p, d)) d.hide();
            return true;
          }
        });
    d.button("Cerrar");
    d.setKeepWithinStage(true);
    activeDetail = d;
    d.show(stage, null);
    d.setOrigin(d.getWidth() / 2, d.getHeight() / 2);
    d.setScale(.12f);
    d.addAction(
        Actions.sequence(
            Actions.scaleTo(.35f, .35f, DETAIL_FRAME),
            Actions.scaleTo(.60f, .60f, DETAIL_FRAME),
            Actions.scaleTo(.82f, .82f, DETAIL_FRAME),
            Actions.scaleTo(1, 1, DETAIL_FRAME)));
    d.setPosition(
        (stage.getViewport().getWorldWidth() - d.getWidth()) / 2,
        (stage.getViewport().getWorldHeight() - d.getHeight()) / 2);
  }

  private boolean evolveFirstReady(Species parent, Dialog detail) {
    for (Species child : catalog.species)
      if (child.parent == parent.id && child.id != 292 && game.ready(child))
        return evolveDetail(child, detail);
    return false;
  }

  private boolean evolveDetail(Species species, Dialog detail) {
    long before = game.s.coins;
    if (!game.evolve(species)) return false;
    coinToast(game.s.coins - before);
    detail.hide();
    save();
    entries();
    return true;
  }

  private void settings() {
    heading("Sonido");
    Label value = label("");
    Slider slider = new Slider(0, 100, 1, false, skin);
    slider.setValue(store.volume);
    Table volume = new Table();
    volume.add(button("0 %", () -> slider.setValue(0)));
    volume.add(slider).width(260).pad(6);
    volume.add(button("100 %", () -> slider.setValue(100)));
    content.add(value).left().row();
    content.add(volume).left().row();
    value.setText("Volumen de los gritos · " + store.volume + " %");
    change(
        slider,
        () -> {
          store.volume((int) slider.getValue());
          value.setText("Volumen de los gritos · " + store.volume + " %");
        });
    heading("Partida");
    content
        .add(
            label(
                store.blocked
                    ? "Guardado ilegible. Puedes empezar de cero"
                    : "Tu partida se guarda automáticamente en este equipo"))
        .left()
        .row();
    Table tools = new Table();
    content.add(tools).left().row();
    tools
        .add(
            button(
                "Empezar de cero",
                () ->
                    confirm(
                        "Nueva partida",
                        "¿Borrar la partida actual?",
                        () -> replace(new GameState()))))
        .pad(3);
  }

  private void replace(GameState next) {
    game.s = next;
    game.revision++;
    store.blocked = false;
    tab = 0;
    shop = 0;
    slot = 0;
    births.clearChildren();
    coins.clearChildren();
    effects.clearChildren();
    floatingCoins.clear();
    birthTargets.clear();
    rebuild();
    save();
  }

  private void confirm(String title, String text, Runnable yes) {
    Dialog d =
        new Dialog(title, skin) {
          protected void result(Object accepted) {
            if (Boolean.TRUE.equals(accepted)) yes.run();
          }
        };
    d.text(text);
    d.button("Cancelar", false);
    d.button("Confirmar", true);
    d.show(stage);
  }

  private void confirmTravel(Runnable yes) {
    Dialog d =
        new Dialog("Volver al pasado", skin) {
          protected void result(Object accepted) {
            if (Boolean.TRUE.equals(accepted)) yes.run();
          }
        };
    d.text("Reinicia esta crianza y recibe");
    Table amount = new Table();
    amount.add(label(Integer.toString(game.registered()))).left();
    amount.add(icon("reset-urge")).size(18).padLeft(4);
    d.getContentTable().add(amount).left().row();
    d.button("Cancelar", false);
    d.button("Confirmar", true);
    d.show(stage);
  }

  private void updateWallet() {
    if (wallet == null) return;
    boolean showRc = game.s.trips > 0;
    if (!walletBuilt || walletShowsRc != showRc) {
      wallet.clearChildren();
      wallet.add(icon("poke-coin")).size(18).padRight(4);
      coinBalance = new Label("", skin, "light");
      wallet.add(coinBalance).padRight(showRc ? 8 : 0);
      if (showRc) {
        wallet.add(icon("reset-urge")).size(18).padRight(4);
        rcBalance = new Label("", skin, "light");
        wallet.add(rcBalance);
      } else {
        rcBalance = null;
      }
      walletBuilt = true;
      walletShowsRc = showRc;
    }
    coinBalance.setText(Long.toString(game.s.coins));
    if (showRc) rcBalance.setText(Long.toString(game.s.rc));
  }

  private void refresh() {
    updateWallet();
    for (Runnable r : bindings) r.run();
    if (detailUpdate != null) detailUpdate.run();
  }

  @Override
  public void render(float delta) {
    if (!paused) game.tick(Math.min(delta, .25f));
    elapsed += delta;
    saveTime += delta;
    if (elapsed >= .15f) {
      elapsed = 0;
      refresh();
      if (lastRevision != game.revision && !Gdx.input.isTouched()) {
        lastRevision = game.revision;
        if (tab == 1) entries();
        if (tab == 2 && shop == 0 && gsBallListed != game.gate(catalog.item.get("gs-ball")).isEmpty())
          afterResize();
        if (tab == 3
            && bagSize != game.s.owned.size() + game.s.benefits.size() + game.evolutions() / 8)
          afterPurchase();
      }
    }
    if (saveTime >= 4) {
      saveTime = 0;
      save();
    }
    Gdx.gl.glClearColor(.91f, .93f, .87f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    stage.act(delta);
    positionRails();
    stage.draw();
  }

  @Override
  public void resize(int w, int h) {
    ((ScreenViewport) stage.getViewport())
        .setUnitsPerPixel(Math.max(1f, Math.max(640f / Math.max(1, w), 460f / Math.max(1, h))));
    stage.getViewport().update(w, h, true);
    columns = Math.max(3, (int) (stage.getViewport().getWorldWidth() / 160));
    if (skin != null && (tab == 2 || tab == 3)) afterResize();
  }

  private void afterResize() {
    float y = scroll.getScrollY();
    rebuild();
    scroll.validate();
    scroll.setScrollY(y);
    scroll.updateVisualScroll();
  }

  @Override
  public void pause() {
    paused = true;
    save();
  }

  @Override
  public void resume() {
    paused = false;
  }

  @Override
  public void hide() {
    save();
  }

  @Override
  public void dispose() {
    stage.clear();
    stage.dispose();
    skin.dispose();
  }
}
