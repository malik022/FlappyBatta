package com.jucyzhang.flappybatta;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class GroundSprite implements Sprite {

  private Drawable ground;
  private int groundHeight;
  private int groundWidth;
  private int width;

  public GroundSprite(Context context) {
    width = ViewUtil.getScreenWidth(context);
  }

  @Override
  public void onDraw(Canvas canvas, Paint globalPaint, int status) {

  }

  @Override
  public boolean isAlive() {
    return true;
  }

  @Override
  public boolean isHit(Sprite sprite) {
    return false;
  }

  @Override
  public int getScore() {
    return 0;
  }

}
