package io.github.poke_hatch;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Main extends Game {
  private AssetManager assets;

  public AssetManager getAssets() {
    return assets;
  }

  @Override
  public void create() {
    assets = new AssetManager();
    for (String entry : Gdx.files.internal("assets.txt").readString("UTF-8").split("\n")) {
      String path = entry.trim();
      if ((path.startsWith("pokemon/") || path.equals("art/meadow.png")
          || path.equals("art/star-piece.png")) && path.endsWith(".png")) {
        assets.load(path, Texture.class);
      }
      if (path.endsWith(".ogg")) assets.load(path, Sound.class);
    }
    assets.load("interface/ui.atlas", TextureAtlas.class);
    assets.finishLoading();
    setScreen(new FirstScreen(this));
  }

  @Override
  public void dispose() {
    super.dispose();
    if (getScreen() != null) getScreen().dispose();
    assets.dispose();
  }
}
