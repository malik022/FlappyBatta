package com.jucyzhang.flappybatta;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class BattaSprite implements Sprite {

  private static final int FLY_COUNT = 6;
  private int count = 0;
  private Drawable birds[] = new Drawable[4];
  private final int X;
  private int width;
  private int height;
  private int currentHeight;
  private int birdHeight;
  private int birdWidth;
  private float currentSpeed;
  private final float acceleration;
  private final float tapSpeed;
  private int maxHeight;
  private int hitPaddingTop;
  private int hitPaddingBottom;
  private int hitPaddingRight;
  private int hitPaddingLeft;

  public BattaSprite(Context context) {
    Resources res = context.getResources();
    birds[0] = birds[2] = res.getDrawable(R.drawable.img_bird_1);
    birds[1] = res.getDrawable(R.drawable.img_bird_2);
    birds[3] = res.getDrawable(R.drawable.img_bird_3);
    birdHeight = ViewUtil.dipResourceToPx(context, R.dimen.bird_height);
    birdWidth = birdHeight * birds[0].getIntrinsicWidth()
        / birds[0].getIntrinsicHeight();
    width = ViewUtil.getScreenWidth(context);
    height = ViewUtil.getScreenHeight(context);
    int xPosition = ViewUtil.dipResourceToPx(context, R.dimen.bird_position_x);
    X = width / 2 - birdWidth / 2 - xPosition;
    currentHeight = height / 2 - birdHeight / 2;
    acceleration = ViewUtil.dipResourceToFloat(context,
        R.dimen.bird_acceleration);
    tapSpeed = ViewUtil.dipResourceToFloat(context, R.dimen.bird_tap_speed);
    maxHeight = height
        - ViewUtil.dipResourceToPx(context, R.dimen.ground_height);
    hitPaddingBottom = ViewUtil.dipResourceToPx(context,
        R.dimen.bird_hit_padding_bottom);
    hitPaddingTop = ViewUtil.dipResourceToPx(context,
        R.dimen.bird_hit_padding_top);
    hitPaddingLeft = ViewUtil.dipResourceToPx(context,
        R.dimen.bird_hit_padding_left);
    hitPaddingRight = ViewUtil.dipResourceToPx(context,
        R.dimen.bird_hit_padding_right);
    currentSpeed = 0;
  }

  public int getHitLeft() {
    return X + hitPaddingLeft;
  }

  public int getHitTop() {
    return currentHeight + hitPaddingTop;
  }

  public int getHitBottom() {
    return currentHeight + birdHeight - hitPaddingBottom;
  }

  public int getHitRight() {
    return X + birdWidth - hitPaddingRight;
  }

  @Override
  public void onDraw(Canvas canvas, Paint globalPaint, int status) {
    if (count >= 4 * FLY_COUNT) {
      count = 0;
    }
    if (status != Sprite.STATUS_NOT_STARTED) {
      currentHeight += currentSpeed;
      synchronized (this) {
        currentSpeed += acceleration;
      }
    }
    if (currentHeight <= 0) {
      currentHeight = 0;
    }
    if (currentHeight + birdHeight > maxHeight) {
      currentHeight = maxHeight - birdHeight;
    }
    Drawable bird = birds[(count++) / FLY_COUNT];
    Log.d(TAG, "X:" + X + " currentHeight:" + currentHeight + " birdWidth:"
        + birdWidth + " birdHeight:" + birdHeight);
    bird.setBounds(X, currentHeight, X + birdWidth, currentHeight + birdHeight);
    bird.draw(canvas);
  }

  @Override
  public boolean isAlive() {
    return true;
  }

  @Override
  public boolean isHit(Sprite sprite) {
    return currentHeight + birdHeight >= maxHeight;
  }

  public void onTap() {
    synchronized (this) {
      currentSpeed = tapSpeed;
    }
  }

}
