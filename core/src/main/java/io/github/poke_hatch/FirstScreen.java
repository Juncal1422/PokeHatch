package io.github.poke_hatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.poke_hatch.Catalog.Item;
import io.github.poke_hatch.Catalog.Species;
import static io.github.poke_hatch.Catalog.contains;

/**
 * Persistent navigation and input actors. Simulation updates only the affected
 * widgets.
 */
public final class FirstScreen extends ScreenAdapter implements GameEngine.Listener {
  private static final float DETAIL_FRAME = .025f;
  private static final int CURSOR_FRAME_MILLIS = 60;
  private static final int CURSOR_FRAME_WIDTH = 24;
  private static final int CURSOR_FRAME_HEIGHT = 32;
  private static final int CURSOR_FRAME_COUNT = 4;
  private static final int CURSOR_IMAGE_SIZE = 32;
  private static final int[] CURSOR_CLICK_FRAMES = { 1, 2, 3 };
  private static final float FARM_EGG_SIZE = 100;
  private static final float HATCH_SPRITE_SIZE = 304;
  private static final int[] MENU_TAB_FRAME_Y = { 0, 23, 46, 69, 92 };
  private static final int MENU_TAB_FRAME_HEIGHT = 20;
  private static final Color UNAVAILABLE_BADGE_TINT = new Color(.16f, .17f, .16f, 1);
  private static final float[][] COIN_TRAJECTORIES = {
      { -42, 52 }, { 42, 52 }, { 0, 76 }, { -24, 90 }, { 24, 90 }
  };
  private static final float[] FAIRY_SPARKLE_COLOR = { 1, .34f, .7f };
  private static final float[][] MINERAL_SPARKLE_COLORS = {
      { .35f, .82f, 1 },
      { .78f, .38f, .92f },
      { 1, .62f, .2f },
      { .35f, .9f, .45f }
  };
  private final Main app;
  private final Stage stage = new Stage(new ScreenViewport());
  private final Catalog catalog = new Catalog();
  private final SaveStore store = new SaveStore(catalog);
  private final GameEngine game = new GameEngine(catalog, store.load());
  private final Table root = new Table();
  private final Table farmContent = new Table();
  private final Table menuContent = new Table();
  private final Table dexContent = new Table();
  private final Table births = new Table();
  private final Table coins = new Table();
  private final Table effects = new Table();
  private Table content = menuContent;
  private Table fieldPanel, menuPanel, dexDrawer;
  private Cell<?> menuTabsCell;
  private final List<Runnable> bindings = new ArrayList<>();
  private final List<Dialog> dialogs = new ArrayList<>();
  private final List<Actor> floatingCoins = new ArrayList<>();
  private final Map<Actor, Float> birthTargets = new IdentityHashMap<>();
  private final HatchSprite[] eggs = new HatchSprite[9];
  private final Image[] menuIcons = new Image[5];
  private Texture[] menuTabTextures;
  private TextureRegion[] menuTabRegions;
  private Table menuTabs;
  private Table cards;
  private int cardCount, cardColumns = 3, columns = 3;
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
  private final String[] tabs = { "Criadero", "Tienda", "Piedra Alba", "Magmatizador", "Mochila" };
  private final String[] menuIconKeys = { "egg", "coin-case", "dawn-stone", "magmarizer", "explorer-kit" };
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
  private Texture emptyBarTexture;
  private Texture[] fillBarTextures;
  private TextureRegion emptyBarRegion;
  private TextureRegion[] fillBarRegions;
  private TextureRegion buttonGlint;
  private Texture eggParticleTexture;
  private TextureRegion particlePixel, particleDot, particleBubble, particleFlower, particleDrop;
  private BitmapFont bodyFont, headingFont, brandFont;
  private ParticleEffect eggClickParticleTemplate;
  private ParticleEffectPool eggClickParticlePool;
  private Cursor idleCursor, activeCursor;
  private Cursor[] cursorFrames;
  private TextButton.TextButtonStyle affordablePurchaseStyle;
  private TextButton.TextButtonStyle lockedPurchaseStyle;
  private TextButton.TextButtonStyle pixelAffordablePurchaseStyle;
  private TextButton.TextButtonStyle pixelBuyPurchaseStyle;
  private Table wallet;
  private Label coinBalance, rcBalance;
  private ScrollPane scroll, dexScroll;
  private Image dexToggle;
  private TextButton settingsButton;
  private int tab = 2, shop, slot, lastStep, gen, group, lastRevision = -1;
  private boolean registeredOnly, shinyOnly, readyOnly, paused;
  private boolean walletBuilt, walletShowsRc, gsBallListed, cursorPressed, dexOpen;
  private long cursorClickStart = -1;
  private String search = "";
  private DexGrid dexList;
  private Label dexResults;
  private float elapsed, saveTime;
  private Runnable detailUpdate;
  private Dialog activeDetail;
  private Dialog farmDetailsDialog;
  private Runnable farmDetailsUpdate;
  private Dialog settingsDialog;

  FirstScreen(Main app) {
    this.app = app;
    game.listener = this;
  }

  @Override
  public void show() {
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    skin = new Skin(Gdx.files.internal("interface/skin.json"));
    TextureAtlas uiAtlas = app.getAssets().get("interface/ui.atlas", TextureAtlas.class);
    skin.addRegions(uiAtlas);
    buttonGlint = skin.getRegion("button-glint");
    initializeFonts();
    initializeButtonStyles();
    initializeEggClickParticles();
    initializeCursor();
    initializeProgressBars();
    initializeMenuTabSprites();
    neutralGroupPanel = skin.getDrawable("panel");
    activeGroupPanel = skin.newDrawable("panel", new Color(.72f, .9f, .68f, 1));
    inactiveGroupPanel = skin.newDrawable("panel", new Color(.68f, .72f, .7f, 1));
    hatchedGroupPanel = skin.newDrawable("panel", new Color(.82f, .9f, .95f, 1));
    completeGroupPanel = skin.newDrawable("panel", new Color(1, .9f, .58f, 1));
    firstCoinPanel = skin.newDrawable("panel", new Color(.42f, .9f, .94f, 1));
    repeatCoinPanel = skin.getDrawable("canvas");
    shinyCoinPanel = skin.newDrawable("panel", new Color(1, .77f, .24f, 1));
    silverCoinPanel = skin.newDrawable("panel", new Color(.78f, .84f, .9f, 1));
    affordablePurchaseStyle = coloredPurchaseStyle(new Color(.5f, .86f, .54f, 1));
    lockedPurchaseStyle = coloredPurchaseStyle(new Color(.94f, .42f, .37f, 1));
    pixelAffordablePurchaseStyle =
        coloredPurchaseStyle(new Color(.5f, .86f, .54f, 1), headingFont);
    pixelBuyPurchaseStyle = pixelBuyPurchaseStyle();
    TooltipManager.getInstance().initialTime = .25f;
    root.setFillParent(true);

    fieldPanel = new Table();
    fieldPanel.setBackground(skin.getDrawable("ui/box-standard"));
    farmContent.top().left();
    ScrollPane fieldScroll = new ScrollPane(farmContent, skin);
    fieldScroll.setScrollingDisabled(true, false);
    fieldScroll.setFadeScrollBars(false);
    fieldPanel.add(fieldScroll).grow();

    menuPanel = new Table();
    menuPanel.setBackground(skin.getDrawable("ui/menu"));
    menuPanel.top();
    wallet = new Table();
    menuTabs = new Table();
    menuTabs.setBackground(
        new BaseDrawable() {
          @Override
          public void draw(
              com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float width, float height) {
            batch.draw(menuTabRegions[selectedMenuIndex()], x, y, width, height);
          }
        });
    for (int i = 0; i < tabs.length; i++) {
      final int n = i;
      Image menuIcon = menuIcons[i] = icon(menuIconKeys[i]);
      TextTooltip tooltip = new TextTooltip(tabs[i], skin);
      tooltip.setInstant(false);
      menuIcon.addListener(tooltip);
      click(menuIcon, () -> selectMenu(n));
      menuTabs.add(menuIcon).expandX().fill().pad(2);
    }
    menuTabsCell = menuPanel.add(menuTabs).growX();
    layoutMenuTabs();
    Table walletRow = new Table();
    walletRow.add(wallet).right();
    menuPanel.add(walletRow).growX().pad(4, 8, 2, 8).row();
    scroll = new ScrollPane(menuContent, skin);
    scroll.setScrollingDisabled(true, false);
    scroll.setFadeScrollBars(false);
    menuPanel.add(scroll).grow();
    layoutPanels();
    stage.addActor(root);
    for (Table rail : new Table[] { births, coins }) {
      rail.setTouchable(Touchable.disabled);
      stage.addActor(rail);
    }
    effects.setFillParent(true);
    effects.setTouchable(Touchable.disabled);
    stage.addActor(effects);
    buildDexDrawer();
    buildSettingsButton();
    stage.getRoot()
        .addCaptureListener(
            new InputListener() {
              @Override
              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Dialog detail = activeDetail;
                if (detail == null || !detail.isVisible())
                  return false;
                float halfWidth = detail.getWidth() * detail.getScaleX() / 2;
                float halfHeight = detail.getHeight() * detail.getScaleY() / 2;
                float centerX = detail.getX() + detail.getOriginX();
                float centerY = detail.getY() + detail.getOriginY();
                if (event.getStageX() >= centerX - halfWidth
                    && event.getStageX() <= centerX + halfWidth
                    && event.getStageY() >= centerY - halfHeight
                    && event.getStageY() <= centerY + halfHeight)
                  return false;
                detail.hide();
                event.stop();
                return true;
              }
            });
    Gdx.input.setInputProcessor(stage);
    updateWallet();
    rebuild();
    if (store.blocked)
      message("Guardado ilegible. Puedes iniciar una partida desde Ajustes.");
  }

  private void initializeFonts() {
    bodyFont = generateFont("fonts/Terminal.ttf", 16, Texture.TextureFilter.Linear);
    headingFont = generateFont("fonts/Pixel.ttf", 22, Texture.TextureFilter.Nearest);
    brandFont = generateFont("fonts/Pixel.ttf", 26, Texture.TextureFilter.Nearest);
  }

  private void initializeMenuTabSprites() {
    Pixmap source = new Pixmap(Gdx.files.internal("art/ui/menu-tab.png"));
    try {
      menuTabTextures = new Texture[MENU_TAB_FRAME_Y.length];
      menuTabRegions = new TextureRegion[MENU_TAB_FRAME_Y.length];
      for (int i = 0; i < MENU_TAB_FRAME_Y.length; i++) {
        Pixmap frame =
            new Pixmap(source.getWidth(), MENU_TAB_FRAME_HEIGHT, Pixmap.Format.RGBA8888);
        frame.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < MENU_TAB_FRAME_HEIGHT; y++)
          for (int x = 0; x < source.getWidth(); x++)
            frame.drawPixel(x, y, source.getPixel(x, MENU_TAB_FRAME_Y[i] + y));
        menuTabTextures[i] = new Texture(frame);
        menuTabTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        menuTabRegions[i] = new TextureRegion(menuTabTextures[i]);
        frame.dispose();
      }
    } finally {
      source.dispose();
    }
  }

  private void initializeProgressBars() {
    Pixmap empty = new Pixmap(Gdx.files.internal("art/ui/empty-bar.png"));
    try {
      makeWhiteTransparent(empty);
      emptyBarTexture = new Texture(empty);
      emptyBarTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
      emptyBarRegion = new TextureRegion(emptyBarTexture);
    } finally {
      empty.dispose();
    }

    Pixmap source = new Pixmap(Gdx.files.internal("art/ui/filler-bar.png"));
    try {
      int[] rows = { 0, 3, 6 };
      fillBarTextures = new Texture[rows.length];
      fillBarRegions = new TextureRegion[rows.length];
      for (int i = 0; i < rows.length; i++) {
        Pixmap band = new Pixmap(source.getWidth(), 2, Pixmap.Format.RGBA8888);
        band.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < 2; y++)
          for (int x = 0; x < source.getWidth(); x++)
            band.drawPixel(x, y, source.getPixel(x, rows[i] + y));
        fillBarTextures[i] = new Texture(band);
        fillBarTextures[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fillBarRegions[i] = new TextureRegion(fillBarTextures[i]);
        band.dispose();
      }
    } finally {
      source.dispose();
    }
  }

  private void makeWhiteTransparent(Pixmap pixmap) {
    for (int y = 0; y < pixmap.getHeight(); y++)
      for (int x = 0; x < pixmap.getWidth(); x++) {
        int rgba = pixmap.getPixel(x, y);
        if (((rgba >>> 24) & 0xff) == 255
            && ((rgba >>> 16) & 0xff) == 255
            && ((rgba >>> 8) & 0xff) == 255)
          pixmap.drawPixel(x, y, 0);
      }
  }

  private void layoutPanels() {
    root.clearChildren();
    float width = stage.getViewport().getWorldWidth();
    root.add(fieldPanel).width(width * .6f).growY();
    root.add(menuPanel).width(width * .4f).growY();
  }

  private void layoutMenuTabs() {
    float width = stage.getViewport().getWorldWidth() * .4f;
    menuTabsCell.height(Math.max(42, width * MENU_TAB_FRAME_HEIGHT / 152f));
    menuPanel.invalidateHierarchy();
  }

  private int selectedMenuIndex() {
    return tab == 3 ? 4 : Math.max(0, Math.min(3, shop));
  }

  private void selectMenu(int index) {
    if (index == 4) {
      tab = 3;
    } else {
      tab = 2;
      shop = index;
    }
    rebuild();
  }

  private void buildDexDrawer() {
    dexDrawer = new Table();
    dexDrawer.setBackground(skin.getDrawable("canvas"));
    dexDrawer.pad(4);
    Table header = new Table();
    header.setBackground(skin.getDrawable("pine"));
    Label title = new Label("Pokédex", skin, "light");
    header.add(title).growX().center();
    dexDrawer.add(header).growX().height(40).row();

    dexScroll = new ScrollPane(dexContent, skin);
    dexScroll.setScrollingDisabled(true, false);
    dexScroll.setFadeScrollBars(false);
    dexDrawer.add(dexScroll).grow();
    stage.addActor(dexDrawer);

    dexToggle = icon("ui/lateral-pokeball");
    dexToggle.setSize(58, 58);
    dexToggle.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            toggleDex();
          }
        });
    stage.addActor(dexToggle);
    positionDexDrawer();
  }

  private void buildSettingsButton() {
    settingsButton = new TextButton("", skin);
    settingsButton.setStyle(skin.get("default", TextButton.TextButtonStyle.class));
    settingsButton.add(icon("settings")).size(34);
    settingsButton.setSize(56, 56);
    click(settingsButton, this::openSettingsDialog);
    stage.addActor(settingsButton);
    positionSettingsButton();
  }

  private void positionSettingsButton() {
    settingsButton.setSize(56, 56);
    settingsButton.setPosition(12, 12);
  }

  private float dexDrawerWidth() {
    return stage.getViewport().getWorldWidth() * .6f;
  }

  private void positionDexDrawer() {
    float width = dexDrawerWidth();
    float height = stage.getViewport().getWorldHeight();
    dexDrawer.clearActions();
    dexDrawer.setSize(width, height);
    dexToggle.setSize(116, 116);
    float toggleOffset = dexToggle.getWidth() * .5862f;
    dexDrawer.setPosition(dexOpen ? 0 : -width + toggleOffset, 0);
    dexDrawer.setVisible(dexOpen);
    dexDrawer.setTouchable(dexOpen ? Touchable.enabled : Touchable.disabled);
    syncDexToggle();
    dexToggle.setY((height - dexToggle.getHeight()) / 2);
  }

  private void syncDexToggle() {
    float toggleOffset = dexToggle.getWidth() * .5862f;
    float x = Math.max(-7, dexDrawer.getX() + dexDrawer.getWidth() - toggleOffset);
    dexToggle.setX(x);
  }

  private void toggleDex() {
    if (activeDetail != null)
      activeDetail.hide();
    float width = dexDrawerWidth();
    float height = stage.getViewport().getWorldHeight();
    float toggleOffset = dexToggle.getWidth() * .5862f;
    dexOpen = !dexOpen;
    if (dexOpen) {
      buildDexContent();
      dexScroll.setScrollY(0);
      dexDrawer.clearActions();
      dexDrawer.setVisible(true);
      dexDrawer.setTouchable(Touchable.enabled);
      dexDrawer.setPosition(-width + toggleOffset, 0);
      dexDrawer.setSize(width, height);
      dexDrawer.addAction(
          Actions.sequence(
              Actions.moveBy(-7, 0, .06f),
              Actions.moveTo(0, 0, .24f, Interpolation.swingOut)));
    } else {
      dexDrawer.clearActions();
      dexDrawer.setTouchable(Touchable.disabled);
      dexDrawer.addAction(
          Actions.sequence(
              Actions.moveTo(-width + toggleOffset, 0, .24f, Interpolation.swingIn),
              Actions.visible(false)));
    }
    syncDexToggle();
    dexToggle.setY((height - dexToggle.getHeight()) / 2);
  }

  private void openSettingsDialog() {
    if (settingsDialog != null && settingsDialog.isVisible()) {
      settingsDialog.toFront();
      return;
    }

    Table previousContent = content;
    Table previousCards = cards;
    int previousCardCount = cardCount;
    int previousCardColumns = cardColumns;
    int previousTab = tab;
    Table settingsContent = new Table();
    settingsContent.top().left().pad(8);
    content = settingsContent;
    cards = null;
    tab = 4;
    settings();
    content = previousContent;
    cards = previousCards;
    cardCount = previousCardCount;
    cardColumns = previousCardColumns;
    tab = previousTab;

    Dialog dialog =
        new Dialog("", skin) {
          @Override
          public void hide() {
            super.hide();
            if (settingsDialog == this)
              settingsDialog = null;
          }
        };
    addDialogHeader(dialog, "Ajustes");
    ScrollPane settingsScroll = new ScrollPane(settingsContent, skin);
    settingsScroll.setScrollingDisabled(true, false);
    settingsScroll.setFadeScrollBars(false);
    float width = Math.max(1, Math.min(780, stage.getViewport().getWorldWidth() - 48));
    float height = Math.max(1, Math.min(600, stage.getViewport().getWorldHeight() - 112));
    dialog.getContentTable().add(settingsScroll).width(width).height(height).pad(0).row();
    dialog.button("Cerrar", false);
    styleDialogButtons(dialog);
    dialog.setKeepWithinStage(true);
    settingsDialog = dialog;
    dialog.show(stage);
    fitDialog(dialog);
    registerDialog(dialog);
  }

  private void registerDialog(Dialog dialog) {
    dialogs.remove(dialog);
    dialogs.add(dialog);
  }

  private void bringDialogsToFront() {
    for (int i = dialogs.size() - 1; i >= 0; i--)
      if (!dialogs.get(i).isVisible())
        dialogs.remove(i);
    for (Dialog dialog : dialogs)
      dialog.toFront();
  }

  private void buildDexContent() {
    content = dexContent;
    dexContent.clearChildren();
    dexContent.top().left().pad(8, screenMargin(), 8, screenMargin());
    cards = null;
    dexList = null;
    dexResults = null;
    dex();
    content = menuContent;
    skin.add("GeneratedTerminal", bodyFont, BitmapFont.class);
    skin.add("GeneratedPixel", headingFont, BitmapFont.class);
    skin.add("GeneratedBrand", brandFont, BitmapFont.class);

    skin.get("default", Label.LabelStyle.class).font = bodyFont;
    skin.get("light", Label.LabelStyle.class).font = bodyFont;
    skin.get("pixel", Label.LabelStyle.class).font = headingFont;
    skin.get("brand", Label.LabelStyle.class).font = brandFont;
    for (String name : new String[] { "default", "buy", "active" })
      skin.get(name, TextButton.TextButtonStyle.class).font = bodyFont;
    skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle.class).font = bodyFont;
    skin.get("default", SelectBox.SelectBoxStyle.class).font = bodyFont;
    skin.get("default", TextField.TextFieldStyle.class).font = bodyFont;
    skin.get("default", CheckBox.CheckBoxStyle.class).font = bodyFont;
    skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle.class).titleFont =
        bodyFont;
  }

  private BitmapFont generateFont(String path, int size, Texture.TextureFilter filter) {
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
    try {
      FreeTypeFontParameter parameters = new FreeTypeFontParameter();
      parameters.size = size;
      parameters.characters =
          FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑüÜ¿¡♀♂";
      parameters.hinting = FreeTypeFontGenerator.Hinting.Full;
      parameters.kerning = true;
      parameters.minFilter = filter;
      parameters.magFilter = filter;
      BitmapFont font = generator.generateFont(parameters);
      font.setUseIntegerPositions(true);
      return font;
    } finally {
      generator.dispose();
    }
  }

  private void initializeButtonStyles() {
    setButtonStyle("default", new Color(.56f, .82f, .54f, 1));
    setButtonStyle("buy", new Color(.96f, .76f, .34f, 1));
    setButtonStyle("active", new Color(.55f, .78f, .88f, 1));
  }

  private void setButtonStyle(String name, Color color) {
    TextButton.TextButtonStyle style = skin.get(name, TextButton.TextButtonStyle.class);
    style.up = skin.newDrawable("button", color);
    style.over = reflective(skin.newDrawable("button", new Color(color).lerp(Color.WHITE, .2f)));
    style.down = skin.newDrawable("button", new Color(color).lerp(Color.BLACK, .18f));
    style.disabled = skin.newDrawable("button", new Color(.68f, .7f, .65f, 1));
  }

  private Drawable reflective(Drawable background) {
    return new BaseDrawable(background) {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        background.draw(batch, x, y, width, height);
        Color color = batch.getColor();
        float red = color.r, green = color.g, blue = color.b, alpha = color.a;
        float shineWidth = Math.min(width, Math.max(8, width * .18f));
        float progress = (TimeUtils.millis() % 1700L) / 1700f;
        batch.setColor(red, green, blue, alpha * .45f);
        batch.draw(
            buttonGlint,
            x + (width - shineWidth) * progress,
            y,
            shineWidth,
            height);
        batch.setColor(red, green, blue, alpha);
      }
    };
  }

  private void initializeEggClickParticles() {
    initializeEggParticleSprites();
    eggClickParticleTemplate = new ParticleEffect();
    eggClickParticleTemplate.load(
        Gdx.files.internal("effects/egg-click.p"), Gdx.files.internal("effects"));
    eggClickParticlePool = new ParticleEffectPool(eggClickParticleTemplate, 2, 16);
  }

  private void initializeEggParticleSprites() {
    Pixmap sprites = new Pixmap(128, 32, Pixmap.Format.RGBA8888);
    try {
      sprites.setBlending(Pixmap.Blending.None);
      sprites.setColor(Color.WHITE);
      sprites.drawPixel(0, 0);
      sprites.fillCircle(8, 8, 5);
      sprites.setColor(1, 1, 1, .6f);
      sprites.drawCircle(24, 8, 6);
      sprites.setColor(Color.WHITE);
      sprites.drawLine(20, 5, 22, 3);
      sprites.fillCircle(44, 4, 3);
      sprites.fillCircle(51, 9, 3);
      sprites.fillCircle(49, 16, 3);
      sprites.fillCircle(39, 16, 3);
      sprites.fillCircle(37, 9, 3);
      sprites.fillCircle(44, 11, 3);
      sprites.fillTriangle(64, 1, 58, 10, 70, 10);
      sprites.fillCircle(64, 15, 6);
      eggParticleTexture = new Texture(sprites);
    } finally {
      sprites.dispose();
    }
    eggParticleTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    particlePixel = new TextureRegion(eggParticleTexture, 0, 0, 1, 1);
    particleDot = new TextureRegion(eggParticleTexture, 2, 2, 12, 12);
    particleBubble = new TextureRegion(eggParticleTexture, 16, 0, 16, 16);
    particleFlower = new TextureRegion(eggParticleTexture, 32, 0, 24, 24);
    particleDrop = new TextureRegion(eggParticleTexture, 56, 0, 16, 24);
  }

  private void initializeCursor() {
    if (Gdx.app.getType() != Application.ApplicationType.Desktop)
      return;
    Pixmap source = new Pixmap(Gdx.files.internal("art/ui/click.png"));
    try {
      if (source.getWidth() != CURSOR_FRAME_WIDTH * CURSOR_FRAME_COUNT
          || source.getHeight() != CURSOR_FRAME_HEIGHT)
        throw new IllegalStateException("Expected click cursor sprite sheet to be 96x32");

      Vector2 hitPoint = cursorHitPoint(source);
      cursorFrames = new Cursor[CURSOR_FRAME_COUNT];
      for (int frame = 0; frame < CURSOR_FRAME_COUNT; frame++) {
        Pixmap image =
            new Pixmap(CURSOR_IMAGE_SIZE, CURSOR_IMAGE_SIZE, Pixmap.Format.RGBA8888);
        image.setBlending(Pixmap.Blending.None);
        try {
          for (int y = 0; y < CURSOR_FRAME_HEIGHT; y++)
            for (int x = 0; x < CURSOR_FRAME_WIDTH; x++) {
              int pixel = source.getPixel(frame * CURSOR_FRAME_WIDTH + x, y);
              if (!isCursorBackground(pixel))
                image.drawPixel(x, y, pixel);
            }
          cursorFrames[frame] =
              Gdx.graphics.newCursor(image, Math.round(hitPoint.x), Math.round(hitPoint.y));
        } finally {
          image.dispose();
        }
      }
    } finally {
      source.dispose();
    }
    idleCursor = cursorFrames[0];
    activeCursor = idleCursor;
    Gdx.graphics.setCursor(idleCursor);
  }

  private Vector2 cursorHitPoint(Pixmap source) {
    int boundary = CURSOR_FRAME_HEIGHT;
    int left = CURSOR_FRAME_WIDTH;
    int right = -1;
    for (int y = 0; y < CURSOR_FRAME_HEIGHT; y++)
      for (int x = 0; x < CURSOR_FRAME_WIDTH; x++)
        if (!isCursorBackground(source.getPixel(x, y))) {
          if (y < boundary) {
            boundary = y;
            left = right = x;
          } else if (y == boundary) {
            left = Math.min(left, x);
            right = Math.max(right, x);
          }
        }
    if (right < 0)
      throw new IllegalStateException("Cursor image has no visible pixels");
    return new Vector2((left + right) / 2f, boundary);
  }

  private boolean isCursorBackground(int rgba) {
    int red = (rgba >>> 24) & 0xff;
    int green = (rgba >>> 16) & 0xff;
    int blue = (rgba >>> 8) & 0xff;
    int alpha = rgba & 0xff;
    return alpha == 0 || (red >= 250 && green >= 250 && blue >= 250);
  }

  private TextButton.TextButtonStyle coloredPurchaseStyle(Color color) {
    return coloredPurchaseStyle(color, bodyFont);
  }

  private TextButton.TextButtonStyle coloredPurchaseStyle(Color color, BitmapFont font) {
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    Color hover = new Color(color).lerp(Color.WHITE, .16f);
    style.font = font;
    style.fontColor = skin.getColor("ink");
    style.disabledFontColor = skin.getColor("faded");
    style.up = skin.newDrawable("button", color);
    style.over = reflective(skin.newDrawable("button", hover));
    style.down = skin.newDrawable("button", new Color(color).lerp(Color.BLACK, .18f));
    style.disabled = skin.newDrawable("button", new Color(.68f, .7f, .65f, 1));
    return style;
  }

  private TextButton.TextButtonStyle pixelBuyPurchaseStyle() {
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    style.font = headingFont;
    style.fontColor = skin.getColor("ink");
    style.disabledFontColor = skin.getColor("faded");
    style.up = skin.newDrawable("button", new Color(.95f, .76f, .32f, 1));
    style.over =
        reflective(skin.newDrawable("button", new Color(1, .9f, .52f, 1)));
    style.down = skin.newDrawable("button", new Color(.78f, .57f, .22f, 1));
    style.disabled = skin.newDrawable("button", new Color(.68f, .7f, .65f, 1));
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
    if (key.equals("egg") || key.matches("egg\\d+"))
      key = "eggs/" + key;
    else if (key.startsWith("badge-"))
      key = "medals/" + key;
    Image image = new Image(skin.getDrawable(key));
    image.setScaling(Scaling.fit);
    return image;
  }

  private String sprite(Species p, boolean shiny) {
    return "pokemon/" + (shiny ? "shiny" : "normal") + "/" + p.id + ".png";
  }

  private TextButton button(String name, Runnable run) {
    TextButton b = new TextButton(name, skin);
    if (tab == 2 && affordablePurchaseStyle != null)
      b.setStyle(affordablePurchaseStyle);
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

  private float screenMargin() {
    return 8;
  }

  private float contentWidth() {
    float width = stage.getViewport().getWorldWidth();
    float widthFraction = content == menuContent ? .4f : .6f;
    float scrollMargin = content == menuContent ? 28 : 0;
    return width * widthFraction - screenMargin() * 2 - scrollMargin;
  }

  private void rebuild() {
    TooltipManager.getInstance().hideAll();
    cards = null;
    bindings.clear();
    Arrays.fill(eggs, null);
    dexList = null;
    dexResults = null;
    farmContent.clearChildren();
    menuContent.clearChildren();
    dexContent.clearChildren();
    slot = Math.min(slot, game.s.slots - 1);

    content = farmContent;
    farmContent.top().left().pad(8, screenMargin(), 8, screenMargin());
    farm();

    content = menuContent;
    menuContent.top().left().pad(8, screenMargin(), 8, screenMargin() + 28);
    if (tab == 2)
      shop();
    else if (tab == 3)
      bag();
    else
      settings();

    scroll.setScrollY(0);
    if (dexOpen) {
      buildDexContent();
      dexScroll.setScrollY(0);
    }
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
    Table tools = new Table();
    if (game.has("card-key")) {
      Image generations = farmTool("card-key", "Generaciones", () -> openFarmDetails(true));
      tools.add(generations).size(42).pad(3);
    }
    if (game.has("scope-lens")) {
      Image groups = farmTool("scope-lens", "Progreso por grupo", () -> openFarmDetails(false));
      tools.add(groups).size(42).pad(3);
    }
    if (tools.getChildren().size > 0)
      content.add(tools).growX().left().row();

    Table field = new Table();
    field.center();
    float availableWidth = contentWidth();
    float eggSize = FARM_EGG_SIZE;
    int farmColumns = Math.max(1, Math.min(3, (int) ((availableWidth + 8) / (eggSize + 8))));
    int centerIndex = farmColumns == 2 ? 5 : 4;
    int[] preferredOrder = { 5, 3, 6, 1, 0, 2, 7, 4, 8 };
    int[] order = new int[preferredOrder.length];
    order[centerIndex] = 0;
    int nextPosition = 0;
    for (int candidate : preferredOrder)
      if (candidate != 0) {
        while (nextPosition == centerIndex)
          nextPosition++;
        order[nextPosition++] = candidate;
      }
    float nestWidth = availableWidth / farmColumns - 8;
    float nestHeight = 118;
    for (int i = 0; i < 9; i++) {
      final int j = order[i];
      Table nest = new Table();
      nest.center();
      if (j < game.s.slots) {
        eggGroups[j] = game.s.group[j];
        HatchSprite egg = eggs[j] = new HatchSprite(icon("egg" + eggGroups[j]).getDrawable());
        nest.add(egg).size(eggSize);
        egg.setOrigin(eggSize / 2, 0);
        nest.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0, 5, .9f, Interpolation.sine),
                    Actions.moveBy(0, -5, .9f, Interpolation.sine))));
        nest.addListener(
            new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                Vector2 clickPosition = nest.localToStageCoordinates(new Vector2(x, y));
                slot = j;
                spawnEggClickParticles(j, clickPosition.x, clickPosition.y);
                game.tap(j);
                updateFarm();
              }
            });
        if (game.has("silph-scope")) {
          HpBar bar = hpBar();
          nest.row();
          nest.add(bar).width(eggSize * .72f).height(12).padTop(3);
          bindings.add(() -> setHpBarValue(bar, game.s.progress[j]));
        }
      }
      field.add(nest).size(nestWidth, nestHeight).pad(5, 4, 4, 4);
      if (i % farmColumns == farmColumns - 1)
        field.row();
    }
    int farmRows = (order.length + farmColumns - 1) / farmColumns;
    content
        .add(field)
        .growX()
        .height(farmRows * (nestHeight + 9))
        .row();
    bindings.add(this::updateFarm);
  }

  private Image farmTool(String iconId, String tooltip, Runnable action) {
    Image button = icon(iconId);
    TextTooltip textTooltip = new TextTooltip(tooltip, skin);
    textTooltip.setInstant(false);
    button.addListener(textTooltip);
    click(button, action);
    return button;
  }

  private HpBar hpBar() {
    return new HpBar();
  }

  private void setHpBarValue(HpBar bar, double value) {
    bar.setValue(value);
  }

  private Table groupProgressContent(Catalog.Group group, Table panel) {
    return groupProgressContent(group, panel, bindings);
  }

  private Table groupProgressContent(Catalog.Group group, Table panel, List<Runnable> updates) {
    Table row = new Table();
    Label progress = label("");
    HpBar bar = hpBar();
    progress.setAlignment(com.badlogic.gdx.utils.Align.center);
    row.add(progress).growX().center().row();
    row.add(bar).growX().height(10).padTop(3);
    updates.add(
        () -> {
          int total = 0, hatched = 0;
          for (Species p : catalog.species)
            if (contains(p.pool, group.id)) {
              total++;
              if (game.s.counts[p.id] > 0)
                hatched++;
            }
          progress.setText(hatched + " / " + total);
          if (game.completeGroup(group.id))
            progress.setColor(1, .65f, .1f, 1);
          else if (total > 0 && hatched == total)
            progress.setColor(.36f, .55f, .72f, 1);
          else
            progress.setColor(Color.WHITE);
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
      if (width < 3 || height < 3)
        return;

      Color old = batch.getColor();
      Color actor = getColor();
      float oldR = old.r, oldG = old.g, oldB = old.b, oldA = old.a;
      float tintR = oldR * actor.r, tintG = oldG * actor.g, tintB = oldB * actor.b;
      float alpha = oldA * actor.a * parentAlpha;
      batch.setColor(tintR, tintG, tintB, alpha);
      batch.draw(emptyBarRegion, x, y, width, height);
      int zone = value < .25f ? 2 : value < .5f ? 1 : 0;
      float fillWidth = Math.round((width - 2) * value);
      if (fillWidth > 0)
        batch.draw(fillBarRegions[zone], x + 1, y + 1, fillWidth, height - 2);
      batch.setColor(oldR, oldG, oldB, oldA);
    }
  }

  private int count(boolean[] flags) {
    int n = 0;
    for (boolean b : flags)
      if (b)
        n++;
    return n;
  }

  private void openFarmDetails(boolean showGenerations) {
    if (farmDetailsDialog != null && farmDetailsDialog.isVisible())
      farmDetailsDialog.hide();

    float width = Math.min(900, stage.getViewport().getWorldWidth() * .58f);
    List<Runnable> updates = new ArrayList<>();
    Table details = showGenerations
        ? generationDetails(updates, width)
        : groupDetails(updates, width);
    Dialog dialog = new Dialog("", skin) {
      @Override
      public void hide() {
        super.hide();
        if (farmDetailsDialog == this) {
          farmDetailsDialog = null;
          farmDetailsUpdate = null;
        }
      }
    };
    addDialogHeader(dialog, showGenerations ? "Generaciones" : "Progreso por grupo");
    ScrollPane detailScroll = new ScrollPane(details, skin);
    detailScroll.setScrollingDisabled(true, false);
    detailScroll.setFadeScrollBars(false);
    float height = Math.min(640, stage.getViewport().getWorldHeight() * .72f);
    dialog.getContentTable().add(detailScroll).width(width).height(height).row();
    dialog.button("Cerrar", false);
    styleDialogButtons(dialog);
    dialog.setKeepWithinStage(true);
    farmDetailsDialog = dialog;
    farmDetailsUpdate =
        () -> {
          for (Runnable update : updates)
            update.run();
        };
    farmDetailsUpdate.run();
    dialog.show(stage);
    fitDialog(dialog);
    registerDialog(dialog);
  }

  private Table generationDetails(List<Runnable> updates, float width) {
    Table details = new Table();
    details.top().center();
    float tileWidth = Math.max(80, (width - 54) / 3);
    Table row = null;
    for (int i = 1; i <= 5; i++) {
      final int generation = i;
      if ((i - 1) % 3 == 0) {
        row = new Table();
        row.center();
        details.add(row).center().growX().row();
      }
      Table tile = new Table();
      tile.top().center().pad(7, 8, 7, 8);
      tile.add(icon("badge-" + catalog.badges[(generation - 1) * 8])).size(52).pad(4).row();
      Label name = label(catalog.regions[generation - 1]);
      name.setWrap(true);
      name.setAlignment(com.badlogic.gdx.utils.Align.center);
      tile.add(name).growX().center().row();
      Label status = label("");
      tile.add(status).center();
      tile.setTouchable(Touchable.enabled);
      click(
          tile,
          () -> {
            if (game.s.gen[generation] && count(game.s.gen) == 1) {
              message("Debe quedar una generación activa");
              return;
            }
            game.s.gen[generation] = !game.s.gen[generation];
            refresh();
            save();
          });
      updates.add(
          () -> {
            boolean active = game.s.gen[generation];
            tile.setBackground(active ? activeGroupPanel : inactiveGroupPanel);
            status.setText(active ? "Activa" : "Inactiva");
          });
      row.add(tile).width(tileWidth).pad(3).fillY().top();
    }
    return details;
  }

  private Table groupDetails(List<Runnable> updates, float width) {
    Table details = new Table();
    details.top().center();
    float tileWidth = Math.max(80, (width - 54) / 3);
    int column = 0;
    for (Catalog.Group group : catalog.groups) {
      Table tile = new Table();
      tile.setBackground(neutralGroupPanel);
      tile.pad(4, 6, 4, 6);
      Label name = label(group.name);
      name.setWrap(true);
      name.setAlignment(com.badlogic.gdx.utils.Align.center);
      tile.add(name).center().growX().row();
      tile.add(icon("egg" + group.id)).size(42).pad(3).row();
      tile.add(groupProgressContent(group, tile, updates)).growX();
      details.add(tile).width(tileWidth).pad(3).fillX().top();
      if (++column == 3) {
        details.row();
        column = 0;
      }
    }
    return details;
  }

  private void updateFarm() {
    for (int i = 0; i < game.s.slots; i++)
      if (eggs[i] != null && eggGroups[i] != game.s.group[i]) {
        eggGroups[i] = game.s.group[i];
        eggs[i].next(icon("egg" + eggGroups[i]).getDrawable());
      }
  }

  public void step(int i) {
    lastStep = i;
    Image egg = eggs[i];
    if (egg == null || eggs[i].revealing())
      return;
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
    if (amount > 0)
      coinToast(amount, egg, reward);
    if (egg != null) {
      eggGroups[lastStep] = game.s.group[lastStep];
      egg.next(icon("egg" + eggGroups[lastStep]).getDrawable());
      egg.reveal(picture(sprite(p, shiny)).getDrawable(), HATCH_SPRITE_SIZE / FARM_EGG_SIZE);
    }
    if (store.volume > 0)
      app.getAssets().get("cries/" + p.id + ".ogg", Sound.class).play(store.volume / 100f);
  }

  private void hatchToast(Species p, boolean shiny, List<String> drops) {
    Table line = new Table();
    line.setBackground(alphaBackground(skin.getDrawable("panel"), .5f));
    line.add(picture(sprite(p, shiny))).size(42).pad(3);
    line.add(label((shiny ? "Shiny · " : "") + p.name)).pad(4);
    for (String id : drops) {
      line.add(icon(id)).size(28).padLeft(5);
      line.add(label(catalog.item.get(id).name)).pad(3);
    }
    line.pack();
    addToast(births, line, 9, 2.4f);
  }

  private Drawable alphaBackground(Drawable drawable, float alpha) {
    return new BaseDrawable(drawable) {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        Color color = batch.getColor();
        float red = color.r, green = color.g, blue = color.b, previousAlpha = color.a;
        batch.setColor(red, green, blue, previousAlpha * alpha);
        drawable.draw(batch, x, y, width, height);
        batch.setColor(red, green, blue, previousAlpha);
      }
    };
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
    Vector2 origin = source.localToStageCoordinates(new Vector2(source.getWidth() / 2, source.getHeight() / 2));
    popup.setPosition(origin.x - popup.getWidth() / 2, origin.y - popup.getHeight() / 2);
    popup.setScale(.2f);
    popup.getColor().a = 0;
    effects.addActor(popup);
    floatingCoins.add(popup);
    float[] trajectory = COIN_TRAJECTORIES[(int) (Math.random() * COIN_TRAJECTORIES.length)];
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
    if (image != null)
      line.add(image).size(30);
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
    float birthX = 12;
    for (int i = births.getChildren().size - 1; i >= 0; i--) {
      Actor a = births.getChildren().get(i);
      Float previousTarget = birthTargets.put(a, y);
      if (previousTarget == null)
        a.setPosition(birthX, y);
      else if (Math.abs(previousTarget - y) > .5f) {
        for (int j = a.getActions().size - 1; j >= 0; j--) {
          Action action = a.getActions().get(j);
          if (action instanceof MoveToAction)
            a.removeAction(action);
        }
        a.addAction(Actions.moveTo(birthX, y, .14f, Interpolation.sineOut));
      }
      y += a.getHeight() + 1;
    }
    y = h - 64;
    for (Actor a : coins.getChildren()) {
      y -= a.getHeight();
      float leftWidth = w * .6f;
      a.setPosition(Math.max(8, leftWidth - a.getWidth() - 12), y);
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
    if (!description.isEmpty())
      subtitle(r, description);
    return r;
  }

  private Table card(String key, String title) {
    return card(key, title, columns);
  }

  private Table card(String key, String title, int requestedColumns) {
    if (cards == null) {
      cards = new Table();
      cards.left();
      content.add(cards).growX().row();
      cardCount = 0;
      cardColumns = requestedColumns;
    }
    if (cardCount > 0 && cardCount % cardColumns == 0)
      cards.row();
    Table tile = new Table();
    tile.top().center().pad(5, 3, 5, 3);
    tile.setBackground(skin.getDrawable("panel"));
    Label name = label(title);
    name.setWrap(true);
    name.setAlignment(com.badlogic.gdx.utils.Align.center);
    tile.add(name).minWidth(0).growX().center().row();
    float width = contentWidth() / cardColumns;
    float iconSize = Math.min(52, Math.max(20, width * .6f));
    tile.add(icon(key)).size(iconSize).center().pad(3).row();
    cards.add(tile).width(width - 2).pad(1).fillY().top();
    cardCount++;
    return tile;
  }

  private Image cardIcon(Table card) {
    for (Actor child : card.getChildren())
      if (child instanceof Image)
        return (Image) child;
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
    Vector2 start = source.localToStageCoordinates(new Vector2(source.getWidth() / 2, source.getHeight() / 2));
    Image backpack = menuIcons[4];
    Vector2 end = backpack.localToStageCoordinates(
        new Vector2(backpack.getWidth() / 2, backpack.getHeight() / 2));
    Image duplicate = new Image(source.getDrawable(), Scaling.fit);
    duplicate.setSize(52, 52);
    duplicate.setOrigin(26, 26);
    duplicate.setPosition(start.x - 26, start.y - 26);
    duplicate.setScale(.22f);
    effects.addActor(duplicate);
    effects.addActor(new ItemTrail(duplicate, source.getDrawable()));
    duplicate.addAction(
        Actions.sequence(
            Actions.parallel(
                Actions.sequence(
                    Actions.scaleTo(1.15f, 1.15f, .22f, Interpolation.elasticOut),
                    Actions.scaleTo(.18f, .18f, .3f, Interpolation.sineIn)),
                Actions.moveTo(end.x - 26, end.y - 26, .52f, Interpolation.sine)),
            Actions.removeActor()));
  }

  private void spawnEggClickParticles(int index, float x, float y) {
    HatchSprite egg = eggs[index];
    if (egg == null || eggClickParticlePool == null)
      return;
    int group = game.s.group[index];
    if (group == 6) {
      spawnSparkleEffect(x, y, FAIRY_SPARKLE_COLOR, 20);
    } else if (group == 10) {
      for (float[] color : MINERAL_SPARKLE_COLORS)
        spawnSparkleEffect(x, y, color, 8);
    } else {
      effects.addActor(
          new EggBurstActor(
              group,
              x,
              y,
              particlePixel,
              particleDot,
              particleBubble,
              particleFlower,
              particleDrop));
    }
  }

  private void spawnSparkleEffect(float x, float y, float[] color, int count) {
    ParticleEffectPool.PooledEffect effect = eggClickParticlePool.obtain();
    ParticleEmitter emitter = effect.getEmitters().first();
    emitter.getTint().setColors(color);
    effect.setPosition(x, y);
    emitter.addParticles(count);
    effects.addActor(new ParticleActor(effect));
  }

  private static final class ParticleActor extends Actor {
    private ParticleEffectPool.PooledEffect effect;

    ParticleActor(ParticleEffectPool.PooledEffect effect) {
      this.effect = effect;
      setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      if (effect == null)
        return;
      effect.update(delta);
      if (effect.isComplete())
        remove();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      if (effect != null)
        effect.draw(batch);
    }

    @Override
    public boolean remove() {
      if (effect != null) {
        effect.free();
        effect = null;
      }
      return super.remove();
    }
  }

  private static final class ItemTrail extends Actor {
    private static final float SAMPLE_INTERVAL = .045f;
    private static final float FADE_DURATION = .22f;
    private final Actor source;
    private final Drawable sprite;
    private final float[] xs = new float[6], ys = new float[6], sizes = new float[6], ages = new float[6];
    private float sampleTimer, elapsed;
    private int count;

    ItemTrail(Actor source, Drawable sprite) {
      this.source = source;
      this.sprite = sprite;
      setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      elapsed += delta;
      sampleTimer += delta;
      for (int i = 0; i < count; i++)
        ages[i] += delta;
      if (source.getParent() != null && sampleTimer >= SAMPLE_INTERVAL) {
        sampleTimer %= SAMPLE_INTERVAL;
        capture();
      }
      if (elapsed >= .82f)
        remove();
    }

    private void capture() {
      for (int i = xs.length - 1; i > 0; i--) {
        xs[i] = xs[i - 1];
        ys[i] = ys[i - 1];
        sizes[i] = sizes[i - 1];
        ages[i] = ages[i - 1];
      }
      xs[0] = source.getX() + source.getOriginX();
      ys[0] = source.getY() + source.getOriginY();
      sizes[0] = source.getWidth() * source.getScaleX() * .42f;
      ages[0] = 0;
      count = Math.min(count + 1, xs.length);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      Color color = batch.getColor();
      float red = color.r, green = color.g, blue = color.b, alpha = color.a;
      for (int i = count - 1; i >= 0; i--) {
        float opacity = Math.max(0, 1 - ages[i] / FADE_DURATION) * .32f * alpha * parentAlpha;
        if (opacity <= 0)
          continue;
        float size = sizes[i];
        batch.setColor(red, green, blue, opacity);
        sprite.draw(batch, xs[i] - size / 2, ys[i] - size / 2, size, size);
      }
      batch.setColor(red, green, blue, alpha);
    }
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
    gsBallListed = false;
    if (shop == 0)
      heading("Criadero");
    else if (shop == 3)
      heading("Incubadoras");
    if (shop == 0) {
      heading("Grupos de huevo");
      for (Catalog.Group g : catalog.groups) {
        Table r = card("egg" + g.id, g.name);
        r.setTouchable(Touchable.enabled);
        TextButton b = new TextButton("", skin);
        change(
            b,
            () -> {
              if (!game.s.open[g.id]) {
                if (game.unlock(g))
                  afterPurchase();
                else
                  denyPurchase(b);
              }
            });
        r.addListener(
            new ClickListener() {
              private boolean wasOpen;

              @Override
              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                wasOpen = game.s.open[g.id];
                return super.touchDown(event, x, y, pointer, button);
              }

              @Override
              public void clicked(InputEvent event, float x, float y) {
                if (!wasOpen || !game.s.open[g.id])
                  return;
                if (!game.has("oval-charm")) {
                  message("Amuleto Oval para alternar");
                  return;
                }
                if (game.s.active[g.id] && count(game.s.active) == 1) {
                  message("Debe quedar un grupo activo");
                  return;
                }
                game.s.active[g.id] = !game.s.active[g.id];
                afterPurchase();
              }
            });
        Image currency = addCurrencyIcon(b, false);
        r.add(b).growX().height(30).row();
        if (game.has("scope-lens"))
          r.add(groupProgressContent(g, null)).growX().row();
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
                  forSale
                      ? game.s.coins >= game.price(g.cost)
                          ? affordablePurchaseStyle
                          : skin.get("buy", TextButton.TextButtonStyle.class)
                      : active
                          ? affordablePurchaseStyle
                          : skin.get("default", TextButton.TextButtonStyle.class));
              setCurrencySubtitle(
                  req,
                  forSale
                      ? ""
                      : game.has("oval-charm")
                          ? "Selecciona los grupos activos"
                          : "Amuleto Oval para alternar",
                  false);
              b.setText(
                  game.s.open[g.id]
                      ? (game.s.active[g.id] ? "Activo" : "Inactivo")
                      : Integer.toString(game.price(g.cost)));
              currency.setVisible(forSale);
              b.setDisabled(false);
            });
      }
      heading("Plazas adicionales");
      for (int j = 0; j < 8; j++) {
        final int i = j;
        Table r = row(flutes[i], "Plaza " + (i + 2), "");
        TextButton b = button(
            "",
            () -> {
              if (i == game.s.slots - 1 && game.expand())
                afterPurchase();
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
              b.setStyle(
                  i > game.s.slots - 1
                      ? lockedPurchaseStyle
                      : i < game.s.slots - 1
                          ? skin.get("default", TextButton.TextButtonStyle.class)
                          : cost <= game.s.coins
                              ? affordablePurchaseStyle
                              : skin.get("buy", TextButton.TextButtonStyle.class));
              b.setDisabled(i != game.s.slots - 1 || cost > game.s.coins);
            });
      }
      Item gsBall = catalog.item.get("gs-ball");
      gsBallListed = game.gate(gsBall).isEmpty();
      if (gsBallListed)
        travelCard(gsBall);
      return;
    }
    if (shop == 3) {
      for (int j = 0; j < game.s.slots; j++) {
        final int i = j;
        int lv = game.s.level[i];
        Table r = row("egg" + game.s.group[i], "Plaza " + (i + 1), "Nivel " + lv + " / 5");
        int price = lv == 5 ? 0 : game.price(game.levels[lv]);
        TextButton b = button(
            lv == 5 ? "Nivel máximo" : Integer.toString(price),
            () -> {
              if (game.level(i))
                afterPurchase();
            });
        Image currency = addCurrencyIcon(b, false);
        currency.setVisible(lv != 5);
        r.add(b).growX().height(30).row();
        bindings.add(
            () -> {
              b.setStyle(
                  lv == 5
                      ? skin.get("default", TextButton.TextButtonStyle.class)
                      : price <= game.s.coins
                          ? affordablePurchaseStyle
                          : skin.get("buy", TextButton.TextButtonStyle.class));
              b.setDisabled(lv == 5 || price > game.s.coins);
            });
      }
    } else if (shop == 4) {
      List<Item> temporal = new ArrayList<>();
      for (Item i : catalog.items)
        if (i.kind.equals("RC"))
          temporal.add(i);
      temporal.sort(Comparator.comparingInt(i -> i.price));
      for (int g = 0; g < 2; g++) {
        heading(g == 0 ? "Beneficios Temporales" : "Huevos");
        for (Item i : temporal)
          if ((i.group > 0) == (g == 1))
            item(i, true);
      }
    } else {
      for (String category : sections)
        if (category.equals("Evolutivos") == (shop == 2)) {
          heading(category);
          for (Item i : catalog.items)
            if (i.kind.equals("PC") && i.section.equals(category) && !i.id.equals("gs-ball"))
              item(i, true);
        }
      if (shop == 1 && game.s.trips > 0) {
        List<Item> temporal = new ArrayList<>();
        for (Item i : catalog.items)
          if (i.kind.equals("RC"))
            temporal.add(i);
        temporal.sort(Comparator.comparingInt(i -> i.price));
        for (int g = 0; g < 2; g++) {
          heading(g == 0 ? "Beneficios Temporales" : "Huevos");
          for (Item i : temporal)
            if ((i.group > 0) == (g == 1))
              item(i, true);
        }
      }
    }
  }

  private void travelCard(Item item) {
    cards = null;
    float width = Math.min(460, contentWidth() - 32);
    Table panel = new Table();
    panel.setBackground(skin.getDrawable("panel"));
    panel.pad(10);

    Table titleBar = new Table();
    titleBar.setBackground(skin.getDrawable("leaf"));
    titleBar.pad(6, 10, 6, 10);
    Label title = new Label("Viaje al pasado", skin, "pixel");
    title.setColor(skin.getColor("cream"));
    title.setAlignment(com.badlogic.gdx.utils.Align.center);
    titleBar.add(title).growX().center();
    panel.add(titleBar).growX().row();

    panel.add(picture("pokemon/normal/251.png")).size(112).pad(8).row();
    Label description = new Label(item.desc, skin, "pixel");
    description.setAlignment(com.badlogic.gdx.utils.Align.center);
    description.setWrap(true);
    panel.add(description).width(width - 28).pad(4, 8, 8, 8).row();

    TextButton buy = new TextButton("", pixelBuyPurchaseStyle);
    Image currency = addCurrencyIcon(buy, false);
    panel.add(buy).width(240).height(42).padBottom(4).row();
    bindings.add(
        () -> {
          int price = game.price(item);
          buy.setText(Integer.toString(price));
          buy.setStyle(
              game.s.coins >= price ? pixelAffordablePurchaseStyle : pixelBuyPurchaseStyle);
          currency.setVisible(true);
        });
    change(buy, () -> confirmTravel(() -> buyItem(item, buy, null)));

    content.add(panel).width(width).center().pad(6).row();
  }

  private void buyItem(Item item, TextButton button, Table sourceCard) {
    if (!game.buy(item)) {
      denyPurchase(button);
      return;
    }
    if (item.id.equals("gs-ball")) {
      births.clearChildren();
      coins.clearChildren();
      effects.clearChildren();
      floatingCoins.clear();
      birthTargets.clear();
      tab = 2;
      shop = 1;
      rebuild();
      save();
      return;
    }
    if (sourceCard != null && (shop == 1 || shop == 2 || shop == 4))
      animateItemPurchase(cardIcon(sourceCard));
    afterPurchase();
  }

  private void item(Item i, boolean sell) {
    Table r = card(i.id.equals("golden-egg") ? "egg" : i.id, i.name, 6);
    TextTooltip tip = new TextTooltip(i.desc.replaceFirst("\\.$", ""), skin);
    tip.setInstant(false);
    r.addListener(tip);
    if (!sell) {
      TextButton owned = new TextButton("Adquirido", skin);
      owned.setDisabled(true);
      r.add(owned).growX().height(28).row();
      String rule = game.requirement(i, false);
      if (!i.source.isEmpty())
        subtitle(r, i.source);
      else if (!rule.isEmpty())
        subtitle(r, rule);
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
          if (i.id.equals("gs-ball"))
            confirmTravel(() -> buyItem(i, b, r));
          else
            buyItem(i, b, r);
        });
    bindings.add(
        () -> {
          String reason = game.gate(i);
          boolean owned = game.has(i.id);
          int cost = game.price(i);
          String caption = owned
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
          if (owned)
            b.setStyle(skin.get("default", TextButton.TextButtonStyle.class));
          else if (!reason.isEmpty())
            b.setStyle(lockedPurchaseStyle);
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
        if (game.s.owned.contains(i.id) && i.section.equals(category) && !isTrophy(i)) {
          if (!started) {
            heading(category);
            started = true;
          }
          item(i, false);
        }
    }
    boolean hasBenefits = false;
    for (Item i : catalog.items)
      hasBenefits |= game.s.benefits.contains(i.id) && !isTrophy(i);
    if (hasBenefits) {
      heading("Beneficios Temporales");
      for (Item i : catalog.items)
        if (game.s.benefits.contains(i.id) && !isTrophy(i))
          item(i, false);
    }
    boolean hasTrophies = false;
    for (Item i : catalog.items)
      if (isTrophy(i))
        hasTrophies |= ownsTrophy(i);
    if (hasTrophies) {
      heading("Trofeos");
      for (Item i : catalog.items)
        if (isTrophy(i) && ownsTrophy(i))
          item(i, false);
    }
    heading("Medallas");
    int numberOfColumns = (catalog.badges.length + 7) / 8;
    float columnWidth = (contentWidth() - numberOfColumns * 4) / numberOfColumns;
    Table medalColumns = new Table();
    Table column = null;
    int earned = Math.min(catalog.badges.length, game.evolutions() / 8);
    for (int i = 0; i < catalog.badges.length; i++) {
      if (i % 8 == 0) {
        column = new Table();
        medalColumns.add(column).width(columnWidth).top().padRight(4);
      }
      Table badge = new Table();
      badge.setBackground(skin.getDrawable("panel"));
      Image sprite = icon("badge-" + catalog.badges[i]);
      badge.add(sprite).size(34).pad(3).row();
      Label name = label(catalog.badgeNames[i]);
      name.setWrap(true);
      name.setAlignment(com.badlogic.gdx.utils.Align.center);
      badge.add(name).width(columnWidth - 8).row();
      Label region = label(catalog.regions[i / 8]);
      region.setWrap(true);
      region.setAlignment(com.badlogic.gdx.utils.Align.center);
      region.setColor(skin.getColor("faded"));
      badge.add(region).width(columnWidth - 8).padBottom(3).row();
      final int medalIndex = i;
      bindings.add(
          () ->
              sprite.setColor(
                  medalIndex < earned ? Color.WHITE : UNAVAILABLE_BADGE_TINT));
      column.add(badge).width(columnWidth).padBottom(3).top().row();
    }
    content.add(medalColumns).growX().row();
  }

  private boolean isTrophy(Item item) {
    return item.kind.equals("drop") && !item.evolution;
  }

  private boolean ownsTrophy(Item item) {
    return game.s.owned.contains(item.id)
        || (item.id.equals("golden-egg") && game.s.benefits.contains(item.id));
  }

  private void dex() {
    Table filters = new Table();
    content.add(filters).growX().row();
    float filterWidth = contentWidth();
    TextField field = new TextField(search, skin);
    field.setMessageText("Nombre o número");
    field.setTextFieldListener(
        (f, ch) -> {
          search = f.getText();

          filterDex();
        });
    filters.add(field).width(filterWidth * .42f).fillX();
    SelectBox<String> gens = new SelectBox<>(skin);
    String[] gn = new String[6];
    gn[0] = "Generaciones";
    System.arraycopy(catalog.regions, 0, gn, 1, 5);
    gens.setItems(gn);
    gens.setSelectedIndex(gen);
    filters.add(gens).width(filterWidth * .29f).fillX();
    change(
        gens,
        () -> {
          gen = gens.getSelectedIndex();

          filterDex();
        });
    SelectBox<String> groups = new SelectBox<>(skin);
    String[] names = new String[catalog.groups.length + 1];
    names[0] = "Grupos";
    for (int j = 0; j < catalog.groups.length; j++)
      names[j + 1] = catalog.groups[j].name;
    groups.setItems(names);
    groups.setSelectedIndex(group);
    filters.add(groups).width(filterWidth * .29f).fillX();
    change(
        groups,
        () -> {
          group = groups.getSelectedIndex();

          filterDex();
        });
    Table flags = new Table();
    content.add(flags).growX().left().row();
    CheckBox only = new CheckBox("Registrados", skin),
        ready = new CheckBox("Listos para evolucionar", skin),
        sh = new CheckBox("Shiny", skin);
    only.setChecked(registeredOnly);
    ready.setChecked(readyOnly);
    sh.setChecked(shinyOnly);
    for (CheckBox box : new CheckBox[] { only, ready, sh }) {
      box.getLabel().setWrap(false);
      flags.add(box).pad(3).left();
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
    dexScroll.setScrollY(0);
    entries();
  }

  private void entries() {
    if (dexList == null)
      return;
    List<Species> matches = new ArrayList<>();
    boolean[] parents = new boolean[650];
    if (readyOnly)
      for (Species p : catalog.species)
        if (game.ready(p))
          parents[p.parent] = true;
    String q = search.toLowerCase(Locale.ROOT);
    for (Species p : catalog.species) {
      if ((gen > 0 && p.gen != gen)
          || (group > 0 && !contains(p.groups, catalog.groups[group - 1].id)))
        continue;
      if ((registeredOnly && !game.s.dex[p.id])
          || (shinyOnly && !game.s.shiny[p.id])
          || (readyOnly && !parents[p.id]))
        continue;
      if (!p.name.toLowerCase(Locale.ROOT).contains(q)
          && !Integer.toString(1000 + p.id).substring(1).contains(q))
        continue;
      matches.add(p);
    }
    dexResults.setText(matches.size() + " resultados");
    dexList.setEntries(matches);
  }

  private void detail(Species p) {
    Dialog d = new Dialog("", skin) {
      private boolean closing;

      public void hide() {
        if (closing)
          return;
        closing = true;
        clearActions();
        if (activeDetail == this)
          activeDetail = null;
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
    addDialogHeader(d, "N.º " + p.id + " · " + p.name);
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
      if (groups.length() > 0)
        groups.append(" / ");
      groups.append(catalog.byGroup[g].name);
    }
    t.add(label("Gen. " + p.gen + " · " + groups)).colspan(2).row();
    String condition;
    if (p.id == 292)
      condition = "Se registra al evolucionar a Nincada en Ninjask";
    else if (p.parent == 0) {
      StringJoiner pools = new StringJoiner(" o ");
      for (int g : p.pool)
        pools.add(catalog.byGroup[g].name);
      condition = p.pool.length == 0 ? "Sin método de obtención" : "Nace en " + pools;
    } else {
      condition = game.required(p)
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
        TextButton evolve = button(
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
              boolean ready = game.ready(child);
              evolve.setStyle(ready ? affordablePurchaseStyle : lockedPurchaseStyle);
              evolve.setDisabled(!ready);
            });
      }
    detailUpdate = () -> {
      count.setText(
          "Crías de " + catalog.byId[p.root].name + " obtenidas: " + game.s.counts[p.root]);
      sh.setVisible(game.s.shiny[p.id]);
      for (Runnable r : live)
        r.run();
    };
    detailUpdate.run();
    d.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Input.Keys.SPACE)
              return false;
            if (!evolveFirstReady(p, d))
              d.hide();
            return true;
          }
        });
    styleDialogButtons(d);
    d.setKeepWithinStage(true);
    activeDetail = d;
    d.show(stage, null);
    fitDialog(d);
    registerDialog(d);
    d.setOrigin(d.getWidth() / 2, d.getHeight() / 2);
    d.setScale(.12f);
    d.addAction(
        Actions.sequence(
            Actions.scaleTo(.35f, .35f, DETAIL_FRAME),
            Actions.scaleTo(.60f, .60f, DETAIL_FRAME),
            Actions.scaleTo(.82f, .82f, DETAIL_FRAME),
            Actions.scaleTo(1, 1, DETAIL_FRAME)));
  }

  private boolean evolveFirstReady(Species parent, Dialog detail) {
    for (Species child : catalog.species)
      if (child.parent == parent.id && child.id != 292 && game.ready(child))
        return evolveDetail(child, detail);
    return false;
  }

  private boolean evolveDetail(Species species, Dialog detail) {
    long before = game.s.coins;
    if (!game.evolve(species))
      return false;
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
    heading("Pantalla");
    Table windowModes = new Table();
    content.add(windowModes).growX().row();
    windowModes
        .add(button("Ventana con bordes", () -> applyWindowMode(false)))
        .expandX()
        .fillX()
        .pad(3);
    windowModes
        .add(button("Pantalla completa sin bordes", () -> applyWindowMode(true)))
        .expandX()
        .fillX()
        .pad(3);
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
                () -> confirm(
                    "Nueva partida",
                    "¿Borrar la partida actual?",
                    () -> replace(new GameState()))))
        .pad(3);
    tools.add(button("Salir", () -> Gdx.app.exit())).pad(3);

    heading("Depuración");
    Table debug = new Table();
    content.add(debug).growX().row();
    debug
        .add(
            button(
                "+10000 PC",
                () -> {
                  game.s.coins += 10000;
                  game.revision++;
                  afterPurchase();
                }))
        .expandX()
        .fillX()
        .pad(3);
    debug
        .add(
            button(
                "+10000 RC",
                () -> {
                  game.s.rc += 10000;
                  game.s.trips = Math.max(1, game.s.trips);
                  game.revision++;
                  afterPurchase();
                }))
        .expandX()
        .fillX()
        .pad(3)
        .row();
    debug
        .add(
            button(
                "Completar Pokédex",
                () -> {
                  for (Species species : game.c.species)
                    game.register(species);
                  game.revision++;
                  afterPurchase();
                }))
        .expandX()
        .fillX()
        .pad(3);
    debug
        .add(
            button(
                "Aumentar Shiny chance al 50 %",
                () -> {
                  game.debugShinyChance50 = true;
                  message("Probabilidad shiny al 50 % activada");
                }))
        .expandX()
        .fillX()
        .pad(3);
  }

  private void applyWindowMode(boolean borderless) {
    Graphics graphics = Gdx.graphics;
    Graphics.Monitor monitor = graphics.getMonitor();
    Graphics.DisplayMode displayMode =
        monitor == null ? null : graphics.getDisplayMode(monitor);
    if (displayMode == null) {
      Gdx.app.error("PokeHatch", "No se pudo obtener el modo de pantalla actual");
      message("No se pudo cambiar el modo de pantalla");
      return;
    }

    int width = displayMode.width;
    int height = displayMode.height;
    if (!borderless) {
      float scale = Math.min(1, Math.min(width / 1280f, height / 720f));
      width = Math.round(1280 * scale);
      height = Math.round(720 * scale);
    }
    graphics.setUndecorated(borderless);
    if (!graphics.setWindowedMode(width, height)) {
      Gdx.app.error(
          "PokeHatch",
          "No se pudo cambiar al modo " + (borderless ? "sin bordes" : "de ventana"));
      message("No se pudo cambiar el modo de pantalla");
    }
  }

  private void replace(GameState next) {
    if (settingsDialog != null)
      settingsDialog.hide();
    game.s = next;
    game.revision++;
    store.blocked = false;
    tab = 2;
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
    Dialog d = new Dialog("", skin) {
      protected void result(Object accepted) {
        if (Boolean.TRUE.equals(accepted))
          yes.run();
      }
    };
    addDialogHeader(d, title);
    Label body = label(text);
    body.setAlignment(com.badlogic.gdx.utils.Align.center);
    body.setWrap(true);
    d.getContentTable().add(body).center().pad(4).row();
    d.button("Cancelar", false);
    d.button("Confirmar", true);
    styleDialogButtons(d);
    d.setKeepWithinStage(true);
    d.show(stage);
    fitDialog(d);
    registerDialog(d);
  }

  private void styleDialogButtons(Dialog dialog) {
    for (Actor actor : dialog.getButtonTable().getChildren())
      if (actor instanceof TextButton)
        ((TextButton) actor).setStyle(affordablePurchaseStyle);
  }

  private void addDialogHeader(Dialog dialog, String text) {
    Table titleTable = dialog.getTitleTable();
    titleTable.clearChildren();
    titleTable.pad(0);
    titleTable.setVisible(false);

    Table contentTable = dialog.getContentTable();
    contentTable.setBackground(skin.getDrawable("canvas"));
    contentTable.pad(10);
    contentTable.defaults().pad(4);
    Table header = new Table();
    header.setBackground(skin.getDrawable("leaf"));
    header.pad(7, 10, 7, 10);
    Label label = new Label(text, skin, "pixel");
    label.setColor(skin.getColor("cream"));
    label.setAlignment(com.badlogic.gdx.utils.Align.center);
    header.add(label).growX().center();
    contentTable.add(header).growX().padBottom(8).row();
  }

  private void fitDialog(Dialog dialog) {
    dialog.pack();
    float width =
        Math.min(dialog.getWidth(), Math.max(1, stage.getViewport().getWorldWidth() - 32));
    float height =
        Math.min(dialog.getHeight(), Math.max(1, stage.getViewport().getWorldHeight() - 32));
    dialog.setSize(width, height);
    dialog.setPosition(
        (stage.getViewport().getWorldWidth() - width) / 2,
        (stage.getViewport().getWorldHeight() - height) / 2);
  }

  private void confirmTravel(Runnable yes) {
    Dialog d = new Dialog("", skin) {
      protected void result(Object accepted) {
        if (Boolean.TRUE.equals(accepted))
          yes.run();
      }
    };
    addDialogHeader(d, "Volver al pasado");
    Label text = label("Reinicia esta crianza y recibe");
    text.setAlignment(com.badlogic.gdx.utils.Align.center);
    text.setWrap(true);
    float textWidth = Math.min(380, Math.max(120, stage.getViewport().getWorldWidth() - 64));
    d.getContentTable().add(text).width(textWidth).center().pad(4, 2, 4, 2).row();
    Table amount = new Table();
    amount.add(label(Integer.toString(game.registered()))).center();
    amount.add(icon("reset-urge")).size(18).padLeft(4);
    d.getContentTable().add(amount).center().pad(4).row();
    d.button("Cancelar", false);
    d.button("Confirmar", true);
    styleDialogButtons(d);
    d.setKeepWithinStage(true);
    d.show(stage);
    fitDialog(d);
    registerDialog(d);
  }

  private void updateWallet() {
    if (wallet == null)
      return;
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
    if (showRc)
      rcBalance.setText(Long.toString(game.s.rc));
  }

  private void refresh() {
    updateWallet();
    for (Runnable r : bindings)
      r.run();
    if (detailUpdate != null)
      detailUpdate.run();
    if (farmDetailsUpdate != null)
      farmDetailsUpdate.run();
  }

  @Override
  public void render(float delta) {
    updateCursor();
    if (!paused)
      game.tick(Math.min(delta, .25f));
    elapsed += delta;
    saveTime += delta;
    if (elapsed >= .15f) {
      elapsed = 0;
      refresh();
      if (lastRevision != game.revision && !Gdx.input.isTouched()) {
        lastRevision = game.revision;
        if (dexOpen)
          entries();
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
    syncDexToggle();
    positionRails();
    dexDrawer.toFront();
    dexToggle.toFront();
    settingsButton.toFront();
    bringDialogsToFront();
    stage.draw();
  }

  private void updateCursor() {
    if (idleCursor == null)
      return;
    boolean pressed =
        Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
    long now = TimeUtils.millis();
    if (pressed && !cursorPressed)
      cursorClickStart = now;
    cursorPressed = pressed;

    Cursor nextCursor = idleCursor;
    if (cursorClickStart >= 0) {
      long elapsed = now - cursorClickStart;
      int frame = (int) (elapsed / CURSOR_FRAME_MILLIS);
      if (frame >= CURSOR_CLICK_FRAMES.length)
        cursorClickStart = -1;
      else
        nextCursor = cursorFrames[CURSOR_CLICK_FRAMES[frame]];
    }
    if (nextCursor != activeCursor) {
      activeCursor = nextCursor;
      Gdx.graphics.setCursor(activeCursor);
    }
  }

  @Override
  public void resize(int w, int h) {
    ((ScreenViewport) stage.getViewport())
        .setUnitsPerPixel(Math.max(1f, Math.max(640f / Math.max(1, w), 460f / Math.max(1, h))));
    stage.getViewport().update(w, h, true);
    columns = Math.max(1, (int) (stage.getViewport().getWorldWidth() * .4f / 160));
    if (skin != null) {
      layoutPanels();
      layoutMenuTabs();
      positionDexDrawer();
      positionSettingsButton();
      afterResize();
      if (settingsDialog != null && settingsDialog.isVisible())
        fitDialog(settingsDialog);
    }
  }

  private void afterResize() {
    float y = scroll.getScrollY();
    float dexY = dexScroll.getScrollY();
    rebuild();
    scroll.validate();
    scroll.setScrollY(y);
    scroll.updateVisualScroll();
    if (dexOpen) {
      dexScroll.validate();
      dexScroll.setScrollY(dexY);
      dexScroll.updateVisualScroll();
    }
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
    effects.clearChildren();
    stage.clear();
    stage.dispose();
    skin.dispose();
    if (eggClickParticleTemplate != null)
      eggClickParticleTemplate.dispose();
    if (eggParticleTexture != null)
      eggParticleTexture.dispose();
    if (emptyBarTexture != null)
      emptyBarTexture.dispose();
    if (fillBarTextures != null)
      for (Texture texture : fillBarTextures)
        if (texture != null)
          texture.dispose();
    if (menuTabTextures != null)
      for (Texture texture : menuTabTextures)
        if (texture != null)
          texture.dispose();
    if (cursorFrames != null) {
      Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
      for (Cursor cursor : cursorFrames)
        if (cursor != null)
          cursor.dispose();
    }
  }
}
