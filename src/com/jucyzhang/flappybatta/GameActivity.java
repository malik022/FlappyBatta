package com.jucyzhang.flappybatta;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import com.umeng.update.UmengUpdateAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;

public class GameActivity extends Activity implements Callback, OnClickListener {
  private SurfaceView surfaceView;
  private SurfaceHolder holder;
  private LinkedList<Sprite> sprites;
  private SoundPool soundPool;

  private static final String TAG = "GameActivity";
  private Drawable blockerUp;
  private Drawable blockerDown;
  private static final long GAP = 20;
  private static final long NEW_BLOCKER_COUNT = 60;

  private Paint globalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private boolean surfaceCreated = false;;
  private Thread drawingTheard;
  private int[] soundIds;
  private Dialog alertDialog;

  private static final int SOUND_DIE = 0;
  private static final int SOUND_HIT = 1;
  private static final int SOUND_POINT = 2;
  private static final int SOUND_SWOOSHING = 3;
  private static final int SOUND_WING = 4;

  private BattaSprite battaSprite;
  private ScoreSprite scoreSprite;
  private GroundSprite groundSprite;
  private SplashSprite splashSprite;

  private int blockerCount = 0;
  private volatile int currentPoint = 0;
  private volatile int currentStatus = Sprite.STATUS_NOT_STARTED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    surfaceView = (SurfaceView) findViewById(R.id.surface_view);
    surfaceView.setKeepScreenOn(true);
    holder = surfaceView.getHolder();
    surfaceView.setZOrderOnTop(true);
    surfaceView.setOnClickListener(this);
    holder.addCallback(this);
    holder.setFormat(PixelFormat.TRANSLUCENT);
    loadRes();
    UmengUpdateAgent.update(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    restart();
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopDrawingThread();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopDrawingThread();
    soundPool.release();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    surfaceCreated = true;
    startDrawingThread();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    surfaceCreated = false;
    stopDrawingThread();
  }

  public void showRestartDialog() {
    int highest = PrefUtil.getHighestScore(this);
    String text = null;
    if (currentPoint > highest) {
      highest = currentPoint;
      PrefUtil.setHighestScore(this, currentPoint);
    } else {
    }
    text = "本次分数:" + currentPoint + "\n历史高分:" + highest;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Game Over");
    builder.setMessage(text);
    builder.setPositiveButton("再玩一次", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        playSwooshing();
        restart();
      }
    });
    builder.setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        playSwooshing();
        finish();
      }
    });
    builder.setCancelable(false);
    alertDialog = builder.show();
  }

  private void restart() {
    if (!isFinishing()) {
      soundPool.play(soundIds[SOUND_SWOOSHING], 0.5f, 0.5f, 1, 0, 1);
      sprites = new LinkedList<Sprite>();
      battaSprite = new BattaSprite(this);
      scoreSprite = new ScoreSprite(this);
      groundSprite = new GroundSprite(this);
      splashSprite = null;
      sprites.add(scoreSprite);
      sprites.add(groundSprite);
      sprites.add(battaSprite);
      HintSprite hintSprite = new HintSprite(this);
      sprites.add(hintSprite);
      blockerCount = 0;
      currentPoint = 0;
      currentStatus = Sprite.STATUS_NOT_STARTED;
      if (alertDialog != null && alertDialog.isShowing()) {
        alertDialog.dismiss();
        alertDialog = null;
      }
      if (surfaceCreated) {
        startDrawingThread();
      }
    }
  }

  private void loadRes() {
    Resources res = getResources();
    blockerUp = res.getDrawable(R.drawable.img_block_up);
    blockerDown = res.getDrawable(R.drawable.img_block_down);
    soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    AssetManager assetManager = res.getAssets();
    soundIds = new int[5];
    try {
      soundIds[SOUND_DIE] = soundPool.load(assetManager.openFd("sfx_die.ogg"),
          1);
      soundIds[SOUND_HIT] = soundPool.load(assetManager.openFd("sfx_hit.ogg"),
          1);
      soundIds[SOUND_POINT] = soundPool.load(
          assetManager.openFd("sfx_point.ogg"), 1);
      soundIds[SOUND_SWOOSHING] = soundPool.load(
          assetManager.openFd("sfx_swooshing.ogg"), 1);
      soundIds[SOUND_WING] = soundPool.load(
          assetManager.openFd("sfx_wing.ogg"), 1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void startDrawingThread() {
    stopDrawingThread();
    drawingTheard = new DrawingThread();
    drawingTheard.start();
  }

  private void stopDrawingThread() {
    if (drawingTheard != null) {
      drawingTheard.interrupt();
      try {
        drawingTheard.join();
      } catch (InterruptedException e) {
      }
      drawingTheard = null;
    }
  }

  @SuppressLint("WrongCall")
  private class DrawingThread extends Thread {
    @Override
    public void run() {
      super.run();
      while (!Thread.interrupted()) {
        Canvas canvas = holder.lockCanvas();
        try {
          cleanCanvas(canvas);
          Iterator<Sprite> iterator = sprites.iterator();
          while (iterator.hasNext()) {
            Sprite sprite = iterator.next();
            if (sprite.isAlive()) {
              sprite.onDraw(canvas, globalPaint, currentStatus);
            } else {
              iterator.remove();
              Log.d(TAG, "remove sprite");
            }
          }
        } finally {
          holder.unlockCanvasAndPost(canvas);
        }
        try {
          sleep(GAP);
        } catch (Exception e) {
          break;
        }
        if (currentStatus == Sprite.STATUS_NOT_STARTED) {
          continue;
        }
        if (currentStatus == Sprite.STATUS_GAME_OVER) {
          if (battaSprite.isHit(battaSprite) && !splashSprite.isAlive()) {
            onGameOver();
            break;
          } else {
            continue;
          }
        }
        boolean hit = false;
        for (Sprite sprite : sprites) {
          if (sprite.isHit(battaSprite)) {
            onHit();
            hit = true;
            break;
          }
        }
        if (hit) {
          sprites.addLast(splashSprite = new SplashSprite());
          currentStatus = Sprite.STATUS_GAME_OVER;
          continue;
        }
        if (blockerCount > NEW_BLOCKER_COUNT) {
          blockerCount = 0;
          BlockerSprite sprite = BlockerSprite.obtainRandom(getBaseContext(),
              blockerUp, blockerDown, battaSprite.getX());
          sprites.addFirst(sprite);
          Log.d(TAG, "new sprite");
        } else {
          blockerCount++;
        }
        for (Sprite sprite : sprites) {
          int point = sprite.getScore();
          if (point > 0) {
            onGetPoint(point);
          }
        }
      }
      Log.d("DrawingThread", "quit");
    }
  }

  private void onGetPoint(int point) {
    currentPoint += point;
    scoreSprite.setCurrentScore(currentPoint);
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (!isFinishing()) {
          soundPool.play(soundIds[SOUND_POINT], 0.5f, 0.5f, 1, 0, 1);
        }
      }
    });
  }

  private void onGameOver() {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (!isFinishing()) {
          soundPool.play(soundIds[SOUND_DIE], 0.5f, 0.5f, 1, 0, 1);
          showRestartDialog();
        }
      }
    });
  }

  private void onHit() {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (!isFinishing()) {
          soundPool.play(soundIds[SOUND_HIT], 0.5f, 0.5f, 1, 0, 1);
        }
      }
    });
  }

  private void playSwooshing() {
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (!isFinishing()) {
          soundPool.play(soundIds[SOUND_SWOOSHING], 0.5f, 0.5f, 1, 0, 1);
        }
      }
    });
  }

  private void cleanCanvas(Canvas canvas) {
    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
  }

  @Override
  public void onClick(View v) {
    switch (currentStatus) {
    case Sprite.STATUS_NOT_STARTED:
      currentStatus = Sprite.STATUS_NORMAL;
    case Sprite.STATUS_NORMAL:
      battaSprite.onTap();
      soundPool.play(soundIds[SOUND_WING], 0.5f, 0.5f, 1, 0, 1);
      break;

    default:
      break;
    }
  }
}
