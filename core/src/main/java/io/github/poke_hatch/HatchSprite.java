package io.github.poke_hatch;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;

/** A slot has just one visible sprite throughout its reveal. Incubator time is independent. */
final class HatchSprite extends Image {
  private Drawable nextEgg;
  private float age = 1;
  private static final float END = .5f;

  HatchSprite(Drawable egg) {
    super(egg, Scaling.fit);
    nextEgg = egg;
  }

  void next(Drawable egg) {
    nextEgg = egg;
    if (!revealing()) setDrawable(egg);
  }

  boolean revealing() {
    return age < END;
  }

  void reveal(Drawable baby, float targetScale) {
    clearActions();
    setRotation(0);
    setOrigin(getWidth() / 2, 0);
    age = 0;
    setScale(.05f);
    setDrawable(baby);
    addAction(Actions.scaleTo(targetScale, targetScale, .5f, Interpolation.elasticOut));
  }

  @Override
  public void act(float dt) {
    super.act(dt);
    if (revealing()) {
      age += dt;
      if (!revealing()) {
        setScale(1);
        setDrawable(nextEgg);
      }
    }
  }
}
