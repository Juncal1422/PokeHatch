package io.github.poke_hatch;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

final class EggBurstActor extends Actor {
  private enum Shape {
    DOT,
    LINE,
    BUBBLE,
    FLOWER,
    DROP
  }

  private static final float TWO_PI = MathUtils.PI2;
  private static final Color BLACK = new Color(.08f, .08f, .08f, 1);
  private static final Color GREY = new Color(.68f, .7f, .74f, 1);
  private static final Color BROWN = new Color(.76f, .59f, .39f, 1);
  private static final Color BEIGE = new Color(.79f, .68f, .48f, 1);
  private static final Color PURPLE = new Color(.57f, .28f, .82f, 1);
  private static final Color DEEP_BLUE = new Color(.1f, .28f, .58f, 1);
  private static final Color[] FLOWER_COLORS = {
      new Color(1, .34f, .68f, 1),
      new Color(1, .78f, .24f, 1),
      new Color(1, .92f, .96f, 1),
      new Color(.42f, .86f, .43f, 1)
  };
  private static final Color[] DRAGON_COLORS = {
      new Color(1, .98f, .84f, 1),
      new Color(1, .72f, .2f, 1)
  };

  private final List<Particle> particles = new ArrayList<>();
  private final TextureRegion pixel, dot, bubble, flower, drop;
  private final float originX, originY;

  EggBurstActor(
      int group,
      float x,
      float y,
      TextureRegion pixel,
      TextureRegion dot,
      TextureRegion bubble,
      TextureRegion flower,
      TextureRegion drop) {
    this.originX = x;
    this.originY = y;
    this.pixel = pixel;
    this.dot = dot;
    this.bubble = bubble;
    this.flower = flower;
    this.drop = drop;
    setTouchable(Touchable.disabled);

    if (group == 8 || group == 1) {
      addCrackChains(group == 1);
    } else {
      int count = particleCount(group);
      for (int i = 0; i < count; i++) {
        Particle particle = createParticle(group);
        updatePosition(particle);
        particles.add(particle);
      }
    }
  }

  private int particleCount(int group) {
    switch (group) {
      case 3: return 34;
      case 4: return 18;
      case 5: return 18;
      case 7: return 24;
      case 9: return 28;
      case 11: return 18;
      case 12: return 16;
      case 2: return 38;
      case 14: return 22;
      default: return 24;
    }
  }

  private Particle createParticle(int group) {
    switch (group) {
      case 3:
        return mosquito();
      case 4:
        return fallingLine(GREY, 5, 15, 1.2f, 2.2f);
      case 5:
        return fallingLine(BROWN, 7, 19, 1.5f, 3);
      case 7:
        return flower();
      case 9:
        return bubble(DEEP_BLUE, .58f, 13, 23, 24, 65);
      case 11:
        return amorphousDrop();
      case 12:
        return bubble(new Color(.27f, .68f, .98f, 1), .42f, 12, 21, 20, 58);
      case 2:
        return dust();
      case 14:
        return dragonFlash();
      default:
        return dust();
    }
  }

  private Particle mosquito() {
    Particle p = base(Shape.DOT, BLACK, .9f, 1.5f);
    p.width = p.height = random(2.5f, 4.5f);
    radial(p, 28, 105);
    p.wiggleX = random(8, 24);
    p.wiggleY = random(8, 22);
    p.frequency = random(8, 15);
    return p;
  }

  private Particle fallingLine(
      Color color, float minWidth, float maxWidth, float minHeight, float maxHeight) {
    Particle p = base(Shape.LINE, color, 1, 1.7f);
    p.width = random(minWidth, maxWidth);
    p.height = random(minHeight, maxHeight);
    p.vx = random(-20, 20);
    p.vy = random(-48, -18);
    p.gravity = -28;
    p.wiggleX = random(7, 15);
    p.frequency = random(3, 6);
    p.rotation = random(-35, 35);
    p.rotationSpeed = random(-150, 150);
    return p;
  }

  private Particle flower() {
    Particle p =
        base(
            Shape.FLOWER,
            FLOWER_COLORS[MathUtils.random(FLOWER_COLORS.length - 1)],
            1.1f,
            1.7f);
    p.width = p.height = random(12, 18);
    radial(p, 18, 68);
    p.wiggleX = random(3, 9);
    p.wiggleY = random(3, 8);
    p.frequency = random(2.5f, 5);
    p.rotationSpeed = random(-100, 100);
    return p;
  }

  private Particle bubble(
      Color color, float alpha, float minSize, float maxSize, float minRise, float maxRise) {
    Particle p = base(Shape.BUBBLE, color, 1.1f, 1.8f);
    p.width = p.height = random(minSize, maxSize);
    p.alpha = alpha;
    p.vx = random(-12, 12);
    p.vy = random(minRise, maxRise);
    p.wiggleX = random(6, 14);
    p.frequency = random(2, 4);
    return p;
  }

  private Particle dust() {
    Particle p = base(Shape.DOT, BEIGE, .9f, 1.45f);
    p.width = p.height = random(2.5f, 5);
    radial(p, 24, 118);
    p.wiggleX = random(1, 5);
    p.wiggleY = random(1, 4);
    p.frequency = random(3, 7);
    return p;
  }

  private void addCrackChains(boolean large) {
    int chainCount = large ? 4 : 3;
    int segmentsPerChain = large ? 4 : 3;
    float minLength = large ? 10 : 7;
    float maxLength = large ? 17 : 12;
    float thickness = large ? 1.8f : 1.2f;
    float minLife = large ? .55f : .4f;
    float maxLife = large ? .85f : .65f;

    for (int chain = 0; chain < chainCount; chain++) {
      float x = 0, y = 0;
      float angle = random(0, TWO_PI);
      for (int segment = 0; segment < segmentsPerChain; segment++) {
        if (segment > 0)
          angle += (segment % 2 == 0 ? -1 : 1) * random(.65f, 1.1f);
        float length = random(minLength, maxLength);
        float dx = MathUtils.cos(angle) * length;
        float dy = MathUtils.sin(angle) * length;
        Particle p = base(Shape.LINE, BLACK, minLife, maxLife);
        p.offsetX = x + dx / 2;
        p.offsetY = y + dy / 2;
        p.width = length + 1;
        p.height = thickness;
        p.rotation = angle * MathUtils.radiansToDegrees;
        updatePosition(p);
        particles.add(p);
        x += dx;
        y += dy;
      }
    }
  }

  private Particle amorphousDrop() {
    Particle p = base(Shape.DROP, PURPLE, .75f, 1.2f);
    p.width = random(5, 9);
    p.height = random(8, 14);
    p.offsetX = random(-10, 10);
    p.offsetY = random(-8, 8);
    p.vx = random(-13, 13);
    p.vy = random(-10, 18);
    p.gravity = -120;
    p.rotation = random(-20, 20);
    p.rotationSpeed = random(-55, 55);
    return p;
  }

  private Particle dragonFlash() {
    Particle p =
        base(
            Shape.LINE,
            DRAGON_COLORS[MathUtils.random(DRAGON_COLORS.length - 1)],
            .75f,
            1.2f);
    p.width = random(9, 16);
    p.height = random(2.5f, 4);
    p.widthGrowth = random(5, 10);
    p.heightGrowth = random(1.5f, 3);
    p.orbiting = true;
    p.angle = random(0, TWO_PI);
    p.radius = random(3, 18);
    p.radiusGrowth = random(18, 38);
    p.orbitSpeed = random(-5, 5);
    if (Math.abs(p.orbitSpeed) < 1)
      p.orbitSpeed = p.orbitSpeed < 0 ? -1 : 1;
    p.rotation = p.angle * MathUtils.radiansToDegrees;
    p.rotationSpeed = p.orbitSpeed * MathUtils.radiansToDegrees;
    return p;
  }

  private Particle base(Shape shape, Color color, float minLife, float maxLife) {
    Particle p = new Particle();
    p.shape = shape;
    p.color = new Color(color);
    p.life = random(minLife, maxLife);
    p.phase = random(0, TWO_PI);
    p.alpha = 1;
    return p;
  }

  private void radial(Particle p, float minSpeed, float maxSpeed) {
    float angle = random(0, TWO_PI);
    float speed = random(minSpeed, maxSpeed);
    p.vx = MathUtils.cos(angle) * speed;
    p.vy = MathUtils.sin(angle) * speed;
  }

  private float random(float min, float max) {
    return MathUtils.random(min, max);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    boolean complete = true;
    for (Particle p : particles) {
      if (p.age >= p.life)
        continue;
      p.age = Math.min(p.life, p.age + delta);
      updatePosition(p);
      if (p.age < p.life)
        complete = false;
    }
    if (complete)
      remove();
  }

  private void updatePosition(Particle p) {
    float t = p.age;
    if (p.orbiting) {
      float radius = p.radius + p.radiusGrowth * t;
      p.currentRadius = radius;
      float angle = p.angle + p.orbitSpeed * t;
      p.x = originX + MathUtils.cos(angle) * radius;
      p.y = originY + MathUtils.sin(angle) * radius;
    } else {
      float waveX = MathUtils.sin(p.frequency * t + p.phase)
          + .35f * MathUtils.sin(p.frequency * 2.7f * t + p.phase * 1.7f);
      float waveY = MathUtils.sin(p.frequency * .83f * t + p.phase * 1.3f)
          + .3f * MathUtils.sin(p.frequency * 2.1f * t + p.phase * .7f);
      float startWaveX = MathUtils.sin(p.phase) + .35f * MathUtils.sin(p.phase * 1.7f);
      float startWaveY = MathUtils.sin(p.phase * 1.3f) + .3f * MathUtils.sin(p.phase * .7f);
      p.x =
          originX + p.offsetX + p.vx * t + p.wiggleX * (waveX - startWaveX);
      p.y =
          originY
              + p.offsetY
              + p.vy * t
              + .5f * p.gravity * t * t
              + p.wiggleY * (waveY - startWaveY);
      p.currentRadius = 0;
    }
    p.drawWidth = p.width + p.widthGrowth * t;
    p.drawHeight = p.height + p.heightGrowth * t;
    p.currentRotation = p.rotation + p.rotationSpeed * t;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    Color batchColor = batch.getColor();
    float red = batchColor.r, green = batchColor.g, blue = batchColor.b, alpha = batchColor.a;
    for (Particle p : particles) {
      if (p.age >= p.life)
        continue;
      float fade = 1 - p.age / p.life;
      if (p.orbiting) {
        float distanceFromStart = p.currentRadius - p.radius;
        fade *= MathUtils.clamp(
            1 - distanceFromStart / (p.radiusGrowth * p.life), 0, 1);
      }
      batch.setColor(
          red * p.color.r,
          green * p.color.g,
          blue * p.color.b,
          alpha * parentAlpha * p.alpha * fade);
      TextureRegion sprite = sprite(p.shape);
      batch.draw(
          sprite,
          p.x - p.drawWidth / 2,
          p.y - p.drawHeight / 2,
          p.drawWidth / 2,
          p.drawHeight / 2,
          p.drawWidth,
          p.drawHeight,
          1,
          1,
          p.currentRotation);
    }
    batch.setColor(red, green, blue, alpha);
  }

  private TextureRegion sprite(Shape shape) {
    switch (shape) {
      case DOT: return dot;
      case BUBBLE: return bubble;
      case FLOWER: return flower;
      case DROP: return drop;
      default: return pixel;
    }
  }

  private static final class Particle {
    Shape shape;
    Color color;
    float age, life, alpha, width, height, drawWidth, drawHeight, x, y;
    float offsetX, offsetY, vx, vy, gravity, wiggleX, wiggleY, frequency, phase;
    float rotation, rotationSpeed, currentRotation, widthGrowth, heightGrowth, currentRadius;
    boolean orbiting;
    float angle, radius, radiusGrowth, orbitSpeed;
  }
}
