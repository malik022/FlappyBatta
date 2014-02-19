package com.jucyzhang.flappybatta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

public class ViewUtil {

  private static final int[] STATE_PRESSED = new int[] { android.R.attr.state_pressed };
  private static final int[] STATE_FOCUSED = new int[] { android.R.attr.state_focused };
  private static final int[] STATE_NORMAL = new int[] {};

  private static int screenWidth;
  private static int screenHeight;
  private static float density;
  private static int densityDpi;

  private ViewUtil() {
  }

  public static int dipToPx(Context context, float dp) {
    return (int) (dp * ViewUtil.getScreenDensity(context) + 0.5f);
  }

  public static int dipResourceToPx(Context context, int resourceId) {
    return (int) context.getResources().getDimension(resourceId);
  }

  public static float dipResourceToFloat(Context context, int resourceId) {
    return context.getResources().getDimension(resourceId);
  }

  public static float dipFractionToFloat(Context context, int resourceId) {
    return context.getResources().getFraction(resourceId, 1, 1);
  }

  public static void moveToNextFocus(View view) {
    if (view != null) {
      View next = view.focusSearch(View.FOCUS_FORWARD);
      if (next != null) {
        next.requestFocus();
      }
    }
  }

  public static float getTextWidth(float textSize, String text) {
    TextPaint paint = new TextPaint();
    paint.setTextSize(textSize);
    return paint.measureText(text);
  }

  public static int getScreenDensityDpi(Context context) {
    if (densityDpi > 0) {
      return densityDpi;
    } else {
      initWindowProp(context);
      return densityDpi;
    }
  }

  public static float getScreenDensity(Context context) {
    if (density > 0) {
      return density;
    } else {
      initWindowProp(context);
      return density;
    }
  }

  public static int getScreenWidth(Context context) {
    if (screenWidth > 0) {
      return screenWidth;
    } else {
      initWindowProp(context);
      return screenWidth;
    }
  }

  public static int getScreenHeight(Context context) {
    if (screenHeight > 0) {
      return screenHeight;
    } else {
      initWindowProp(context);
      return screenHeight;
    }
  }

  public static Drawable generateStateListDrawable(Drawable normal,
      Drawable pressed) {
    StateListDrawable drawable = new StateListDrawable();
    drawable.addState(STATE_PRESSED, pressed);
    drawable.addState(STATE_FOCUSED, pressed);
    drawable.addState(STATE_NORMAL, normal);
    return drawable;
  }

  @SuppressWarnings("deprecation")
  public static Drawable drawTextInside9Patch(String text, int textColor,
      float textSize, Drawable drawable) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextSize(textSize);
    paint.setColor(textColor);
    paint.setTextAlign(Align.CENTER);
    int textHeight = (int) (paint.descent() - paint.ascent() + 0.5f);
    int textWidth = (int) (paint.measureText(text, 0, text.length()) + 0.5f);
    Rect padding = new Rect();
    drawable.getPadding(padding);
    Rect bounds = new Rect(0, 0, padding.left + padding.right + textWidth,
        padding.top + padding.bottom + textHeight);
    Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(),
        Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(bounds);
    drawable.draw(canvas);
    canvas.drawText(text, padding.left + textWidth / 2f, padding.top
        + textHeight - paint.descent(), paint);
    drawable.getPadding(padding);
    return new BitmapDrawable(bitmap);
  }

  public static Bitmap drawTextInside9PatchB(String text, int textColor,
      float textSize, Drawable drawable) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextSize(textSize);
    paint.setColor(textColor);
    paint.setTextAlign(Align.CENTER);
    int textHeight = (int) (paint.descent() - paint.ascent() + 0.5f);
    int textWidth = (int) (paint.measureText(text, 0, text.length()) + 0.5f);
    Rect padding = new Rect();
    drawable.getPadding(padding);
    Rect bounds = new Rect(0, 0, padding.left + padding.right + textWidth,
        padding.top + padding.bottom + textHeight);
    Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(),
        Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(bounds);
    drawable.draw(canvas);
    canvas.drawText(text, padding.left + textWidth / 2f, padding.top
        + textHeight - paint.descent(), paint);
    drawable.getPadding(padding);
    return bitmap;
  }

  public static Bitmap toRoundBitmap(Bitmap bitmap) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    float roundPx;
    float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
    if (width <= height) {
      roundPx = width / 2;
      top = 0;
      bottom = width;
      left = 0;
      right = width;
      height = width;
      dst_left = 0;
      dst_top = 0;
      dst_right = width;
      dst_bottom = width;
    } else {
      roundPx = height / 2;
      float clip = (width - height) / 2;
      left = clip;
      right = width - clip;
      top = 0;
      bottom = height;
      width = height;
      dst_left = 0;
      dst_top = 0;
      dst_right = height;
      dst_bottom = height;
    }

    Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final int color = 0xff424242;
    final Paint paint = new Paint();
    final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
    final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right,
        (int) dst_bottom);
    final RectF rectF = new RectF(dst);

    paint.setAntiAlias(true);

    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(bitmap, src, dst, paint);
    return output;
  }

  public static Bitmap createRoundConnerPhoto(int x, int y, Bitmap image,
      float outerRadiusRat) {
    @SuppressWarnings("deprecation")
    Drawable imageDrawable = new BitmapDrawable(image);

    Bitmap output = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    RectF outerRect = new RectF(0, 0, x, y);

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.RED);
    canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    imageDrawable.setBounds(0, 0, x, y);
    canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
    imageDrawable.draw(canvas);
    canvas.restore();

    return output;
  }

  public static String getEditTextString(EditText et) {
    return et == null ? null : et.getText() == null ? null : et.getText()
        .toString().trim();
  }

  public static void showSoftInput(View view) {
    if (view == null) {
      return;
    }
    Context context = view.getContext();
    if (context == null) {
      return;
    }
    InputMethodManager manager = (InputMethodManager) context
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    manager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
  }

  public static void hideSoftInput(View view) {
    if (view == null) {
      return;
    }
    Context context = view.getContext();
    if (context == null) {
      return;
    }
    InputMethodManager manager = (InputMethodManager) context
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    manager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
  }

  public static void setupSpinnerSelection(Spinner s, String selection) {
    for (int i = 0; i < s.getCount(); i++) {
      if (selection.equals(s.getItemAtPosition(i))) {
        s.setSelection(i);
        break;
      }
    }
  }

  /**
   * 从view 得到图片
   * 
   * @param view
   * @return
   */
  public static Bitmap getBitmapFromView(View view, float scale) {
    view.setDrawingCacheEnabled(true);
    Bitmap bitmap = view.getDrawingCache(true);
    if (scale != 1.0f) {
      Bitmap result = Bitmap.createScaledBitmap(bitmap,
          (int) (bitmap.getWidth() * scale),
          (int) (bitmap.getHeight() * scale), false);
      return result;
    } else {
      return bitmap;
    }
  }

  private static void initWindowProp(Context context) {
    WindowManager manager = (WindowManager) context
        .getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics metric = new DisplayMetrics();
    manager.getDefaultDisplay().getMetrics(metric);
    screenWidth = metric.widthPixels;
    screenHeight = metric.heightPixels;
    density = metric.density;
    densityDpi = metric.densityDpi;
  }

  public static Bitmap getAcceptableBitmapFromFile(int maxWidth, String pathName) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(pathName, opts);
    System.gc();// gc before check memory
    opts.inSampleSize = computeSampleSize(opts, maxWidth);
    int actualWidth = opts.outWidth / opts.inSampleSize;
    int actualHeight = opts.outHeight / opts.inSampleSize;
    long bitmapSize = actualHeight * actualWidth * 4;
    Runtime runtime = Runtime.getRuntime();
    long freeMemory = runtime.freeMemory();
    opts.inJustDecodeBounds = false;
    opts.inPreferredConfig = bitmapSize > freeMemory ? Config.RGB_565
        : Config.ARGB_8888;
    return BitmapFactory.decodeFile(pathName, opts);
  }

  public static int computeAcceptableBitmapSampleSize(int maxWidth,
      String pathName) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(pathName, opts);
    return computeSampleSize(opts.outWidth, opts.outHeight, maxWidth);
  }

  private static int computeSampleSize(BitmapFactory.Options options,
      int maxWidth) {
    int width_tmp = options.outWidth, height_tmp = options.outHeight;
    return computeSampleSize(width_tmp, height_tmp, maxWidth);
  }

  private static int computeSampleSize(int width_tmp, int height_tmp,
      int maxWidth) {
    int scale = 1;
    while (true) {
      if (width_tmp / 2 < maxWidth || height_tmp / 2 < maxWidth)
        break;
      width_tmp /= 2;
      height_tmp /= 2;
      scale *= 2;
    }
    return scale;
  }
}
