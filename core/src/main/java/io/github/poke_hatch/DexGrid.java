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
  private static final float[][] GROUP_COLORS = {
    {.78f, .74f, .62f},
    {.82f, .51f, .39f},
    {.45f, .68f, .81f},
    {.57f, .79f, .49f},
    {.54f, .75f, .88f},
    {.79f, .66f, .43f},
    {.88f, .55f, .72f},
    {.49f, .72f, .45f},
    {.68f, .56f, .82f},
    {.38f, .58f, .8f},
    {.63f, .66f, .71f},
    {.64f, .53f, .72f},
    {.38f, .71f, .81f},
    {.82f, .56f, .46f},
    {.84f, .55f, .3f}
  };
  final Main app;
  final GameEngine game;
  final Skin skin;
  private final Vector2 edge = new Vector2();
  private final TextureRegion pixel;
  private final Texture shinyStar;
  private List<Catalog.Species> entries = Collections.emptyList();
  private final GlyphLayout glyph = new GlyphLayout();

  DexGrid(Main app, GameEngine game, Skin skin, Consumer<Catalog.Species> open) {
    this.app = app;
    this.game = game;
    this.skin = skin;
    TextureRegion panel = skin.getRegion("panel");
    pixel =
        new TextureRegion(
            panel.getTexture(),
            panel.getRegionX() + 8,
            panel.getRegionY() + 8,
            1,
            1);
    shinyStar = app.getAssets().get("art/star-piece.png", Texture.class);
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
    BitmapFont font = skin.getFont("GeneratedTerminal");
    Color ink = skin.getColor("ink");
    if (entries.isEmpty()) {
      font.setColor(ink);
      font.draw(batch, "Sin coincidencias", x + 8, y + height - 6);
      return;
    }
    for (int i = firstRow * COLUMNS; i < last; i++) {
      Catalog.Species p = entries.get(i);
      float left = x + (i % COLUMNS) * width, top = y + height - (i / COLUMNS) * HEIGHT;
      boolean shiny = game.s.shiny[p.id], seen = game.s.dex[p.id];
      float cardX = left + 1, cardY = top - HEIGHT + 1;
      float cardWidth = width - 2, cardHeight = HEIGHT - 2;
      drawGroupGradient(
          batch, p, cardX, cardY, cardWidth, cardHeight, seen, parentAlpha);
      if (shiny) drawShinyBorder(batch, cardX, cardY, cardWidth, cardHeight, parentAlpha);
      else {
        batch.setColor(seen ? .3f : .22f, seen ? .5f : .27f, seen ? .35f : .23f, parentAlpha);
        batch.draw(pixel, cardX, cardY + cardHeight - 2, cardWidth, 2);
      }
      batch.setColor(1, 1, 1, parentAlpha);
      Texture texture =
          app.getAssets()
              .get("pokemon/" + (shiny ? "shiny" : "normal") + "/" + p.id + ".png", Texture.class);
      float imageSize = Math.min(48, width - 10);
      float scale = imageSize / Math.max(texture.getWidth(), texture.getHeight());
      if (!seen) batch.setColor(0, 0, 0, parentAlpha);
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
      if (shiny) {
        float starSize = 15;
        float starScale = starSize / Math.max(shinyStar.getWidth(), shinyStar.getHeight());
        batch.setColor(1, .82f, .22f, parentAlpha);
        batch.draw(
            shinyStar,
            cardX + cardWidth - starSize - 3,
            cardY + cardHeight - starSize - 3,
            shinyStar.getWidth() * starScale,
            shinyStar.getHeight() * starScale);
        batch.setColor(1, 1, 1, parentAlpha);
      }
    }
    font.setColor(Color.WHITE);
  }

  private void drawShinyBorder(
      Batch batch, float x, float y, float width, float height, float alpha) {
    float thickness = 2.5f;
    batch.setColor(1, .76f, .18f, alpha);
    batch.draw(pixel, x, y, width, thickness);
    batch.draw(pixel, x, y + height - thickness, width, thickness);
    batch.draw(pixel, x, y + thickness, thickness, height - thickness * 2);
    batch.draw(pixel, x + width - thickness, y + thickness, thickness, height - thickness * 2);
  }

  private void drawGroupGradient(
      Batch batch,
      Catalog.Species species,
      float x,
      float y,
      float width,
      float height,
      boolean seen,
      float alpha) {
    int[] groups = species.groups;
    int stopCount = Math.max(1, groups == null ? 0 : groups.length);
    int segments = stopCount == 1 ? 1 : 16;
    float segmentWidth = width / segments;
    for (int i = 0; i < segments; i++) {
      float position = segments == 1 ? 0 : (i + .5f) * (stopCount - 1) / segments;
      int first = Math.min(stopCount - 1, (int) position);
      int second = Math.min(stopCount - 1, first + 1);
      float blend = position - first;
      float[] a = groupColor(groups, first);
      float[] b = groupColor(groups, second);
      float red = a[0] + (b[0] - a[0]) * blend;
      float green = a[1] + (b[1] - a[1]) * blend;
      float blue = a[2] + (b[2] - a[2]) * blend;
      if (!seen) {
        red *= .58f;
        green *= .58f;
        blue *= .58f;
      }
      batch.setColor(red, green, blue, alpha);
      batch.draw(pixel, x + i * segmentWidth, y, segmentWidth + .5f, height);
    }
  }

  private float[] groupColor(int[] groups, int index) {
    int id = groups != null && index < groups.length ? groups[index] : 0;
    return id > 0 && id < GROUP_COLORS.length ? GROUP_COLORS[id] : GROUP_COLORS[0];
  }
}
