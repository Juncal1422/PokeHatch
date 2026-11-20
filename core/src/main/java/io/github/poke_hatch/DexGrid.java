package io.github.poke_hatch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/** Continuous list with a single input actor, drawing only rows within the viewport. */
final class DexGrid extends Widget {
  private static final int COLUMNS = 9;
  private static final int HEIGHT = 104;
  final Main app;
  final GameEngine game;
  final Skin skin;
  private final Vector2 edge = new Vector2();
  private List<Catalog.Species> entries = Collections.emptyList();
  private final GlyphLayout glyph = new GlyphLayout();

  DexGrid(Main app, GameEngine game, Skin skin, Consumer<Catalog.Species> open) {
    this.app = app;
    this.game = game;
    this.skin = skin;
    addListener(
        new ClickListener() {
          public void clicked(InputEvent e, float x, float y) {
            if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return;
            int i =
                (int) ((getHeight() - y) / HEIGHT) * COLUMNS
                    + (int) (x * COLUMNS / getWidth());
            if (i < entries.size()) open.accept(entries.get(i));
          }
        });
  }

  void setEntries(List<Catalog.Species> list) {
    if (!entries.equals(list)) {
      entries = list;
      invalidateHierarchy();
    }
  }

  @Override
  public float getPrefHeight() {
    return Math.max(1, (entries.size() + COLUMNS - 1) / COLUMNS) * HEIGHT;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    float x = getX(), y = getY(), width = getWidth() / COLUMNS, height = getHeight();
    int firstRow =
        Math.max(
            0,
            (int)
                    ((height
                            - stageToLocalCoordinates(edge.set(0, getStage().getHeight())).y)
                        / HEIGHT)
                - 1);
    int last =
        Math.min(
            entries.size(),
            COLUMNS
                * ((int)
                        ((height - stageToLocalCoordinates(edge.set(0, 0)).y) / HEIGHT)
                    + 2));
    BitmapFont font = skin.getFont("Terminal");
    Color ink = skin.getColor("ink");
    if (entries.isEmpty()) {
      font.setColor(ink);
      font.draw(batch, "Sin coincidencias", x + 8, y + height - 6);
      return;
    }
    for (int i = firstRow * COLUMNS; i < last; i++) {
      Catalog.Species p = entries.get(i);
      float left = x + (i % COLUMNS) * width, top = y + height - (i / COLUMNS) * HEIGHT;
      boolean shiny = game.s.shiny[p.id], seen = game.s.dex[p.id] || shiny;
      batch.setColor(1, 1, 1, parentAlpha);
      skin.getDrawable(shiny ? "gold" : seen ? "leaf" : "panel")
          .draw(batch, left + 1, top - HEIGHT + 1, width - 2, HEIGHT - 2);
      Texture texture =
          app.getAssets()
              .get("pokemon/" + (shiny ? "shiny" : "normal") + "/" + p.id + ".png", Texture.class);
      float imageSize = Math.min(48, width - 10);
      float scale = imageSize / Math.max(texture.getWidth(), texture.getHeight());
      if (!seen) batch.setColor(.23f, .30f, .24f, parentAlpha);
      batch.draw(
          texture,
          left + (width - texture.getWidth() * scale) / 2,
          top - 4 - texture.getHeight() * scale,
          texture.getWidth() * scale,
          texture.getHeight() * scale);
      batch.setColor(1, 1, 1, parentAlpha);
      font.setColor(skin.getColor("faded"));
      String number = "N.º " + p.id;
      glyph.setText(
          font,
          number,
          0,
          number.length(),
          skin.getColor("faded"),
          width - 6,
          com.badlogic.gdx.utils.Align.center,
          false,
          "…");
      font.draw(batch, glyph, left + 3, top - 59);
      font.setColor(ink);
      glyph.setText(
          font,
          p.name,
          0,
          p.name.length(),
          ink,
          width - 6,
          com.badlogic.gdx.utils.Align.center,
          false,
          "…");
      font.draw(batch, glyph, left + 3, top - 82);
    }
    font.setColor(Color.WHITE);
  }
}
