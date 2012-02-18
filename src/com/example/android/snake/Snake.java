/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.snake;

import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Snake: a simple game that everyone can enjoy.
 *
 * This is an implementation of the classic Game "Snake", in which you control a
 * serpent roaming around the garden looking for apples. Be careful, though,
 * because when you catch one, not only will you become longer, but you'll move
 * faster. Running into yourself or the walls will end the game.
 *
 */
public class Snake extends Activity implements SensorEventListener {
   private SensorManager mSensorManager = null;
     private Sensor mAccelerometer = null;

    private SnakeView mSnakeView;

    private static String ICICLE_KEY = "snake-view";

    static Date downTime;
    static long clickTime;

    static int screenWidth;
    static int screenHeight;


    @Override

    public boolean onTouchEvent(MotionEvent event) {
      // TODO Auto-generated method stub
      if (SnakeView.READY == mSnakeView.getMode() && event.getAction() == MotionEvent.ACTION_UP)
      {
        mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_UP, new KeyEvent(KeyEvent.ACTION_DOWN,
              KeyEvent.KEYCODE_DPAD_UP));
      }
      else
      {
          if (event.getAction() == MotionEvent.ACTION_DOWN){
            downTime = new Date();
          }

          if (event.getAction() == MotionEvent.ACTION_UP){
               Date upTime = new Date();
               clickTime = upTime.getTime() - downTime.getTime();
               if (clickTime > 1000){
               togglePause();
               } else {
               navigateSnake(event);
               }
            }
      }
        return super.onTouchEvent(event);
    }

    private void navigateSnake(MotionEvent event){
      float y = event.getY();
      float x = event.getX();

      float ytop = y;
      float ybottom = screenHeight - y;
      float xleft = x;
      float xright = screenWidth- x;

      if (ytop < ybottom && ytop < xleft && ytop < xright){ //top
        mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_UP, new KeyEvent(KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_DPAD_UP));
      } else if (ybottom < ytop && ybottom < xleft && ybottom < xright){//bottom
        mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, new KeyEvent(KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_DPAD_DOWN));
      } else if (xleft < ytop && xleft < ybottom && xleft < xright){ //left
        mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT));
      } else  { //right
        mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT));
      }


    }

    private KeyEvent KeyEvent(int actionDown, int keycodeDpadUp) {
    // TODO Auto-generated method stub
    return null;
  }

  private void togglePause() {
      int currentMode = mSnakeView.getMode();

      if (currentMode == SnakeView.LOSE) {
         // do nothing
      } else if (currentMode == SnakeView.RUNNING ){
        mSnakeView.setMode(SnakeView.PAUSE);
      } else if(currentMode == SnakeView.PAUSE) {
        mSnakeView.setMode(SnakeView.RUNNING);
      }
    }

    /**
     * Called when Activity is first created. Turns off the title bar, sets up
     * the content views, and fires up the SnakeView.
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        getScreenDimensions();

        // No Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.snake_layout);

        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setTextView((TextView) findViewById(R.id.text));
        mSnakeView.setScoreView((TextView) findViewById(R.id.score));
        mSnakeView.setTweetButton((ImageButton) findViewById(R.id.tweet_button));

        mSnakeView.setMode(SnakeView.READY);

        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
              mSnakeView.setMode(SnakeView.READY);
            }
        }
    }
    private void getScreenDimensions(){
      Display display = getWindowManager().getDefaultDisplay();
      screenWidth = display.getWidth();
      screenHeight = display.getHeight();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        // Pause the game along with the activity

      int currentMode = mSnakeView.getMode();

      // Do not pause the game if we're on lose screen
        if (currentMode != SnakeView.LOSE) {
          mSnakeView.setMode(SnakeView.PAUSE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO Auto-generated method stub
  }

  float[] gravity = new float[3];
  long lastMeasureTime = 0;
  final int treshold = 600;
  public void onSensorChanged(SensorEvent event) {
    long currentMeasureTime = SystemClock.elapsedRealtime();
    int delta = (int) (currentMeasureTime - lastMeasureTime);
    lastMeasureTime = currentMeasureTime;

    delta = Math.min(delta, treshold);

    float[] linear_acceleration = new float[3];
    float alpha = (float)delta/(float)treshold;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];


        if (SnakeView.RUNNING == mSnakeView.getMode())
        {
            if (Math.abs(linear_acceleration[0]) > 0.5 || Math.abs(linear_acceleration[1]) > 0.5)
            {
                if (Math.abs(linear_acceleration[0]) < Math.abs(linear_acceleration[1]))
                {
                  if (linear_acceleration[1] > 0)
                  {
                    mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DPAD_DOWN));
                  }
                  else
                  {
                    mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_UP, new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DPAD_UP));
                  }
                }
                else
                {
                  if (linear_acceleration[0] > 0)
                  {
                    mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN,
                          KeyEvent.KEYCODE_DPAD_LEFT));
                  }
                  else
                  {
                    mSnakeView.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_DOWN,
                          KeyEvent.KEYCODE_DPAD_RIGHT));
                  }

                }
            }
        }


  }

}