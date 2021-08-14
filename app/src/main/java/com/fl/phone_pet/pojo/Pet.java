package com.fl.phone_pet.pojo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.iflytek.cloud.SpeechRecognizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifDrawable;

import static android.view.View.VISIBLE;

public class Pet extends Handler {

    public static final int SPEECH_START = 10000;
    public static final int TIMER_START = 10004;
    public static final int TIMER_STOP = 10005;
    public static final int RUN_LEFT = 10006;
    public static final int RUN_RIGHT = 10007;
    public static final int SLEEP = 10008;
    public static final int FLY = 10009;
    public static final int CLIMB_UP = 10010;
    public static final int CLIMB_DOWN = 10011;
    public static final int TIMER_LEFT_START = 10012;
    public static final int TIMER_RIGHT_START = 10013;
    public static final int TIMER_TOP_START = 10014;
    public static final int SPEECH_STOP = 10019;
    public static final int CALL = 10021;
    public static final int FALL_TO_THE_GROUND = 10022;
    public static final int PROP = 10023;
    public static final int VOICE = 10024;
    public static final int COLLISION = 10025;
    public static final int LEFT_STAND = 10026;
    public static final int RIGHT_STAND = 10027;
    public static final int CLIMB_STAND = 10029;
    public static final int HIDDEN_CONTAINER = 10031;
    public static final int MOVE = 10032;
    public static final int HUG = 10033;
    public int BEFORE_MODE = FLY;
    public int CURRENT_ACTION = FLY;
    public String name;
    public static final String imageExt = ".gif";
    public static final String mscExt = ".mp3";
    public String direction = "left";
    Map<Integer, MediaPlayer> mp;
    private Integer funcPanelLayoutResId;

    public static final String WALK_LEFT = "run_left";
    public static final String WALK_RIGHT = "run_right";
    public static final String MOVE_LEFT = "move_left";
    public static final String MOVE_RIGHT = "move_right";
    public static final String MOVE_RIGHT_LIGHT = "move_right_light";
    public static final String MOVE_LEFT_LIGHT = "move_left_light";
    public static final String MOVE_RIGHT_MIDDLE = "move_right_middle";
    public static final String MOVE_LEFT_MIDDLE = "move_left_middle";
    public static final String MOVE_RIGHT_WEIGHT = "move_right_weight";
    public static final String MOVE_LEFT_WEIGHT = "move_left_weight";
    public static final String FLY_LEFT = "fly_left";
    public static final String FLY_RIGHT = "fly_right";
    public static final String CLIMB_LEFT_STAND = "left_stand";
    public static final String CLIMB_RIGHT_STAND = "right_stand";
    public static final String CLIMB_TOP_LEFT_STAND = "top_left_stand";
    public static final String CLIMB_TOP_RIGHT_STAND = "top_right_stand";
    public static final String FALL_TO_GROUND_LEFT = "fall_to_ground_left";
    public static final String FALL_TO_GROUND_RIGHT = "fall_to_ground_right";
    public static final String JUMP_LEFT = "jump_left";
    public static final String JUMP_RIGHT = "jump_right";
    public static final String STAND_LEFT = "stand_left";
    public static final String STAND_RIGHT = "stand_right";
    public WindowManager.LayoutParams params, speechParams, functionPanelParams;
    public View elfView, speechView, functionPanelView, mscView;
    public RelativeLayout downContainerView;
    public ImageView elfBody;
    public TextView speechBody;
    public ImageView functionPanelCallButton;
    public ImageView functionPanelPropButton;
    public ImageView functionPanelVoiceButton;
    public ImageView closeFuncPanelButton;
    Drawable flyLeftGifDrawable;
    Drawable flyRightGifDrawable;
    Drawable climbLeftUpGifDrawable;
    Drawable climbLeftStandGifDrawable;
    Drawable climbRightUpGifDrawable;
    Drawable climbRightStandGifDrawable;
    Drawable climbTopLeftStandGifDrawable;
    Drawable climbTopRightStandGifDrawable;
    Drawable moveLeftGifDrawable;
    Drawable moveRightGifDrawable;
    Drawable moveRightLightGifDrawable;
    Drawable moveLeftLightGifDrawable;
    Drawable moveRightWeightGifDrawable;
    Drawable moveLeftWeightGifDrawable;
    Drawable moveRightMiddleGifDrawable;
    Drawable moveLeftMiddleGifDrawable;
    Drawable fallToGroundLeftGifDrawable;
    Drawable fallToGroundRightGifDrawable;
    Drawable jumpLeftGifDrawable;
    Drawable jumpRightGifDrawable;
    Drawable standLeftGifDrawable;
    Drawable standRightGifDrawable;
    List<String> speechList;
    List<String> voiceIds;
    List<Integer> propList;
    Map<String, List<GifDrawable>> stayAnimations;
    Map<String, List<GifDrawable>> runAnimations;
    public SpeechRecognizer mIat;
    int sleepSize;
    int runSize;
    int currentSize;
    Point size;
    public int speed;
    List<String> callTexts;
    public int pngDeviation = -1;
    public double whRate;
    public int whDif;
    public boolean isOnceFly = true;
    final int distance = 55;
    final int climbTimeConst = 40;
    final double runDistanceMultiply = 50.0;
    final int downVy = 7;


    public static final float gConst = 1.088f;//9.8
    public static final float fsConst = 0.222f;//2
    public static float g;
    public static float fs;
    private float vX0, vY0;
    private final long moveMin = 10;
    private final float v0 = 1.2f;
    private Context ctx;
    private Queue speechStore = new LinkedBlockingQueue(10);
    private CopyOnWriteArrayList<CountDownLatch> downList;


    public Pet(Context ctx, String name, int currentSize, int normalMoveSpeed, Point size, Map<Integer, MediaPlayer> mp, View mscView, RelativeLayout downContainerView, CopyOnWriteArrayList downList) {
        super();
        this.name = name;
        this.currentSize = currentSize;
        this.speed = normalMoveSpeed;
        this.g = normalMoveSpeed * gConst;
        this.fs = normalMoveSpeed * fsConst;
        this.mp = mp;
        this.mscView = mscView;
        this.ctx = ctx;
        this.params = new WindowManager.LayoutParams();
        this.speechParams = new WindowManager.LayoutParams();
        this.functionPanelParams = new WindowManager.LayoutParams();
        this.elfView = LayoutInflater.from(ctx).inflate(R.layout.petelf, null);
        this.speechView = LayoutInflater.from(ctx).inflate(R.layout.speech, null);
        this.downContainerView = downContainerView;
        this.elfBody = elfView.findViewById(R.id.elfbody);
        this.speechBody = speechView.findViewById(R.id.speech);
        this.size = size;
        this.downList = downList;
        initGifDrawables();
        initPropList();
        initVoiceList();
        initSpeecList();
        initCallTexts();
        initWhRate();
        initFuncPanelLayoutResId();
        initParams();
        initView();
        initBindEvent();
    }


    private void initParams() {
        try {
            speechParams.height = (int) (currentSize * 7.5);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                speechParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                functionPanelParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                speechParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                functionPanelParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }


            params.format = PixelFormat.RGBA_8888;
            speechParams.format = PixelFormat.RGBA_8888;
            functionPanelParams.format = PixelFormat.RGBA_8888;


            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            speechParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            functionPanelParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            int petW = (int) (size.x * (currentSize / 100.0));

            params.width = whRate != 0 && whRate != 1 ? (int)(petW * whRate) : petW;
            params.height = petW;
            whDif = params.width - params.height;
            pngDeviation = 0;
            params.x = 0;
            params.y = -size.y / 2 + petW / 2 + 20;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        speechBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, currentSize / 2);
        elfView.setVisibility(VISIBLE);
        MyService.wm.addView(elfView, params);
        MyService.wm.addView(speechView, speechParams);
    }

    private void initBindEvent() {
        elfBody.setLongClickable(true);
        elfBody.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY, dx, dy, x0, y0, tempX, tempY;
            long downTime, upTime, moveTime;
            int replyFlag = 0;
            boolean isDown = false;
            Bitmap bitmap = null;
            int eventX = -1;
            int eventY = -1;
            long moveX = -1L;
            long moveY = -1L;
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        replyFlag = 0;
                        isDown = false;
                        bitmap = ((GifDrawable)(elfBody.getDrawable())).getCurrentFrame();
                        eventX = (int)(event.getX());
                        eventY = (int)(event.getY());
                        if(eventX < 0 || eventY < 0 || eventX >= bitmap.getWidth() || eventY >= bitmap.getHeight())return true;
                        if(bitmap.getPixel(eventX, eventY) == 0)return true;
                        if(CURRENT_ACTION == HUG){
                            voice(MyService.OSS_BASE + "lw/mscs/gunba.mp3");
                            return true;
                        }
                        CURRENT_ACTION = MOVE;
                        if(isOnceFly)isOnceFly = false;
                        isDown = true;
                        downTime = System.currentTimeMillis();
                        removeAllMessages();
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        x0 = lastX;
                        y0 = lastY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!isDown)return true;
                        moveTime = System.currentTimeMillis();

                        tempX = (int) (event.getRawX() < 0 ? 0 : event.getRawX() > size.x ? size.x : event.getRawX());
                        tempY = (int) (event.getRawY() < 0 ? 0 : event.getRawY() > size.y ? size.y : event.getRawY());
                        dx = tempX - lastX;
                        dy = tempY - lastY;

                        params.x = params.x + dx;
                        params.y = params.y + dy;
                        lastX = tempX;
                        lastY = tempY;

                        if (dx != 0 || dy != 0) {
                            if (dx > 0 && dx < 2)
                                elfBody.setImageDrawable(moveRightLightGifDrawable);
                            else if (dx >= 2 && dx < 5)
                                elfBody.setImageDrawable(moveRightMiddleGifDrawable);
                            else if (dx >= 5) elfBody.setImageDrawable(moveRightWeightGifDrawable);
                            else if (dx < 0 && dx > -2)
                                elfBody.setImageDrawable(moveLeftLightGifDrawable);
                            else if (dx <= -2 && dx > -5)
                                elfBody.setImageDrawable(moveLeftMiddleGifDrawable);
                            else if (dx <= -5) elfBody.setImageDrawable(moveLeftWeightGifDrawable);
//                            else
//                                elfBody.setImageDrawable(direction.equals("left") ? moveLeftGifDrawable : moveRightGifDrawable);
                        } else if (lastX - x0 == 0 && lastY - y0 == 0 && BEFORE_MODE != Pet.FLY && moveTime - downTime > 850) {
                            Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                            showFuncPanel();
                            return true;
                        }
                        MyService.wm.updateViewLayout(elfView, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!isDown)return true;
                        upTime = System.currentTimeMillis();
                        moveX = (long) event.getRawX() - x0;
                        moveY = (long) event.getRawY() - y0;
                        if (moveX == 0 && moveY == 0 && BEFORE_MODE != Pet.FLY && upTime - downTime <= 500) {
                            sendEmptyMessage(Pet.SPEECH_START);
                            return true;
                        } else if (moveX == 0 && moveY == 0 && BEFORE_MODE != Pet.FLY && upTime - downTime > 500 && upTime - downTime <= 850) {
                            sendEmptyMessage(BEFORE_MODE);
                            return true;
                        } else if (moveX == 0 && moveY == 0 && BEFORE_MODE != Pet.FLY && upTime - downTime > 850) {
                            return true;
                        }

                        Message msg = new Message();
                        Map<String, Long> data = new HashMap<>();
                        data.put("moveXDirection", (long)dx);
                        data.put("moveYDirection", (long)dy);
                        msg.what = Pet.FLY;
                        msg.obj = data;
                        sendMessage(msg);
                        x0 = 0;
                        y0 = 0;
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case TIMER_START:
                removeAllMessages();
                if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                int j = (int) (Math.random() * (6));
                switch (j) {
                    case 0:
                        sleep();
                        break;
                    case 1:
                        walkToRight();
                        break;
                    case 2:
                        walkToLeft();
                        break;
                    case 3:
                        standLeft();
                        break;
                    case 4:
                        standRight();
                        break;
                    case 5:
                        Message msg3 = new Message();
                        Map<String, Long> data0 = new HashMap<>();
                        data0.put("moveXDirection", direction.equals("left") ? -(long) (Math.random() * 40 + 30) : (long) (Math.random() * 40 + 50));
                        data0.put("moveYDirection", -(long) (Math.random() * 90 + 30));
                        data0.put("jump", 1L);
                        msg3.obj = data0;
                        msg3.what = FLY;
                        sendMessage(msg3);
                        break;
                }
                sendEmptyMessageDelayed(TIMER_START, 10000 + (int) Math.random() * 6000);
                break;
            case TIMER_TOP_START:
                if (BEFORE_MODE != TIMER_TOP_START) BEFORE_MODE = TIMER_TOP_START;
                removeAllMessages();
                int t = (int) (Math.random() * (5));
                switch (t) {
                    case 0:
                        Message msg1 = new Message();
                        Map<String, Long> data = new HashMap<>();
                        data.put("moveXDirection", (long) (Math.random() * 90 - 30));
                        data.put("moveYDirection", (long) (Math.random() * 80));
                        msg1.what = Pet.FLY;
                        msg1.obj = data;
                        sendMessage(msg1);
                        break;
                    case 1:
                        walkToRight();
                        break;
                    case 2:
                        walkToLeft();
                        break;
                    case 3:
                        standLeft();
                        break;
                    case 4:
                        standRight();
                }
                sendEmptyMessageDelayed(TIMER_TOP_START, 10000 + (int) Math.random() * 6000);
                break;
            case TIMER_LEFT_START:
                if(direction.equals("right"))direction = "left";
                if (BEFORE_MODE != TIMER_LEFT_START) BEFORE_MODE = TIMER_LEFT_START;
                removeAllMessages();
                int i = (int) (Math.random() * (4));
                switch (i) {
                    case 0:
                        Message msg1 = new Message();
                        Map<String, Long> data = new HashMap<>();
                        data.put("moveXDirection", (long) (Math.random() * 40 + 40));
                        data.put("moveYDirection", 0L);
                        msg1.what = Pet.FLY;
                        msg1.obj = data;
                        sendMessage(msg1);
                        break;
                    case 1:
                        climbToUp();
                        break;
                    case 2:
                        climbToDown();
                        break;
                    case 3:
                        climbStand();
                        break;
                }
                sendEmptyMessageDelayed(TIMER_LEFT_START, 10000 + (int) Math.random() * 6000);
                break;
            case TIMER_RIGHT_START:
                if(direction.equals("left"))direction = "right";
                if (BEFORE_MODE != TIMER_RIGHT_START) BEFORE_MODE = TIMER_RIGHT_START;
                removeAllMessages();
                int k = (int) (Math.random() * (4));
                switch (k) {
                    case 0:
                        Message msg1 = new Message();
                        Map<String, Long> data = new HashMap<>();
                        data.put("moveXDirection", -(long) (Math.random() * 40 + 40));
                        data.put("moveYDirection", 0L);
                        msg1.what = Pet.FLY;
                        msg1.obj = data;
                        sendMessage(msg1);
                        break;
                    case 1:
                        climbToUp();
                        break;
                    case 2:
                        climbToDown();
                        break;
                    case 3:
                        climbStand();
                        break;
                }
                sendEmptyMessageDelayed(TIMER_RIGHT_START, 10000 + (int) Math.random() * 6000);
                break;
            case TIMER_STOP:
                removeMessages(TIMER_START);
                removeMessages(TIMER_LEFT_START);
                removeMessages(TIMER_RIGHT_START);
                removeMessages(TIMER_TOP_START);
                removeMessages(RUN_LEFT);
                removeMessages(RUN_RIGHT);
                removeMessages(CLIMB_UP);
                removeMessages(CLIMB_DOWN);
                removeMessages(FLY);
                removeMessages(SLEEP);
                break;
            case RUN_LEFT:
                if (CURRENT_ACTION != RUN_LEFT) CURRENT_ACTION = RUN_LEFT;
                removeMessages(RUN_LEFT);

                if(params.x - params.width / 2  + whDif / 2< (-size.x / 2)){
                    removeAllMessages();

                    if(params.x != -size.x / 2 + params.width / 2 - whDif / 2){
                        params.x = -size.x / 2 + params.width / 2 - whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if(BEFORE_MODE == TIMER_TOP_START){
                        if (BEFORE_MODE != TIMER_LEFT_START) BEFORE_MODE = TIMER_LEFT_START;
                        climbToDown();
                        sendEmptyMessageDelayed(TIMER_LEFT_START, 6000 + (int) Math.random() * 6000);
                    }else {
                        if (BEFORE_MODE != TIMER_LEFT_START) BEFORE_MODE = TIMER_LEFT_START;
                        climbToUp();
                        sendEmptyMessageDelayed(TIMER_LEFT_START, 6000 + (int) Math.random() * 6000);
                    }
                }else {

                    if(BEFORE_MODE == TIMER_TOP_START){
                        int resId = ctx.getResources().getIdentifier(name + "_top_" + this.direction + "_climb", "drawable", ctx.getPackageName());
                        elfBody.setImageResource(resId);
                        AnimationDrawable ani = (AnimationDrawable)(elfBody.getDrawable());
                        ani.start();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(MyService.wm != null && CURRENT_ACTION == RUN_LEFT){
                                    params.x = params.x - distance;
                                    MyService.wm.updateViewLayout(elfView, params);
                                    sendEmptyMessageDelayed(RUN_LEFT, (long)(climbTimeConst * (625.0/Math.pow(speed, 2))));
                                }
                            }
                        }, 200);
                    }else {
                        params.x = (int)(params.x - distance * (this.speed/runDistanceMultiply));
                        MyService.wm.updateViewLayout(elfView, params);
                        sendEmptyMessageDelayed(RUN_LEFT, 100);
                    }

                }
                break;
            case RUN_RIGHT:
                if (CURRENT_ACTION != RUN_RIGHT) CURRENT_ACTION = RUN_RIGHT;
                removeMessages(RUN_RIGHT);

                if(params.x + params.width / 2 - whDif / 2 > (size.x / 2)){
                    removeAllMessages();

                    if(params.x != size.x / 2 - params.width / 2 + whDif / 2){
                        params.x = size.x / 2 - params.width / 2 + whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE == TIMER_TOP_START){
                        if (BEFORE_MODE != TIMER_RIGHT_START) BEFORE_MODE = TIMER_RIGHT_START;
                        climbToDown();
                        sendEmptyMessageDelayed(TIMER_RIGHT_START, 6000 + (int) Math.random() * 6000);
                    }else{
                        if (BEFORE_MODE != TIMER_RIGHT_START) BEFORE_MODE = TIMER_RIGHT_START;
                        climbToUp();
                        sendEmptyMessageDelayed(TIMER_RIGHT_START, 6000 + (int) Math.random() * 6000);
                    }
                }else {

                    if(BEFORE_MODE == TIMER_TOP_START){
                        int resId = ctx.getResources().getIdentifier(name + "_top_" + this.direction + "_climb", "drawable", ctx.getPackageName());
                        elfBody.setImageResource(resId);
                        AnimationDrawable ani = (AnimationDrawable)(elfBody.getDrawable());
                        ani.start();
                        postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               if(MyService.wm != null && CURRENT_ACTION == RUN_RIGHT){
                                   params.x = params.x + distance;
                                   MyService.wm.updateViewLayout(elfView, params);
                                   sendEmptyMessageDelayed(RUN_RIGHT, (long)(climbTimeConst * (625.0/Math.pow(speed, 2))));
                               }
                           }
                        }, 200);
                    }else {
                        params.x = (int)(params.x + distance * (this.speed/runDistanceMultiply));
                        MyService.wm.updateViewLayout(elfView, params);
                        sendEmptyMessageDelayed(RUN_RIGHT, 100);
                    }


                }

                break;
            case CLIMB_UP:
                if (CURRENT_ACTION != CLIMB_UP) CURRENT_ACTION = CLIMB_UP;
                removeMessages(CLIMB_UP);
                if (params.y - params.height / 2 < -size.y / 2) {
                    removeAllMessages();
                    if(params.y != -size.y / 2 + params.height / 2){
                        params.y = -size.y / 2 + params.height / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE == TIMER_LEFT_START) {
                        if (BEFORE_MODE != TIMER_TOP_START) BEFORE_MODE = TIMER_TOP_START;
                        walkToRight();
                        sendEmptyMessageDelayed(TIMER_TOP_START, 6000 + (int) Math.random() * 6000);
                    } else {
                        if (BEFORE_MODE != TIMER_TOP_START) BEFORE_MODE = TIMER_TOP_START;
                        walkToLeft();
                        sendEmptyMessageDelayed(TIMER_TOP_START, 6000 + (int) Math.random() * 6000);
                    }
                } else {
                    String direction1 = BEFORE_MODE == TIMER_LEFT_START ? "left" : "right";
                    int resId = ctx.getResources().getIdentifier(name + "_" + direction1 + "_climb", "drawable", ctx.getPackageName());
                    elfBody.setImageResource(resId);
                    AnimationDrawable ani = (AnimationDrawable)(elfBody.getDrawable());
                    ani.start();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(MyService.wm != null && CURRENT_ACTION == CLIMB_UP){
                                params.y = params.y - distance;
                                MyService.wm.updateViewLayout(elfView, params);
                                sendEmptyMessageDelayed(CLIMB_UP, (long)(climbTimeConst * (625.0/Math.pow(speed, 2))));
                            }
                        }
                    }, 200);
                }
                break;
            case CLIMB_DOWN:
                if (CURRENT_ACTION != CLIMB_DOWN) CURRENT_ACTION = CLIMB_DOWN;
                removeMessages(CLIMB_DOWN);
                if (params.y + params.height / 2 + MyService.deviation > size.y / 2) {
                    removeAllMessages();
                    if(params.y != size.y / 2 - params.height / 2 - MyService.deviation){
                        params.y = size.y / 2 - params.height / 2 - MyService.deviation;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE == TIMER_LEFT_START) {
                        if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                        walkToRight();
                        sendEmptyMessageDelayed(TIMER_START, 6000 + (int) Math.random() * 6000);
                    } else {
                        if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                        walkToLeft();
                        sendEmptyMessageDelayed(TIMER_START, 6000 + (int) Math.random() * 6000);
                    }
                } else {
                    String direction1 = BEFORE_MODE == TIMER_LEFT_START ? "left" : "right";
                    int resId = ctx.getResources().getIdentifier(name + "_" + direction1 + "_climb", "drawable", ctx.getPackageName());
                    elfBody.setImageResource(resId);
                    AnimationDrawable ani = (AnimationDrawable)(elfBody.getDrawable());
                    ani.start();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(MyService.wm != null && CURRENT_ACTION == CLIMB_DOWN){
                                params.y = params.y + distance;
                                MyService.wm.updateViewLayout(elfView, params);
                                sendEmptyMessageDelayed(CLIMB_DOWN, (long)(climbTimeConst * (625.0/Math.pow(speed, 2))));
                            }
                        }
                    }, 200);
                }
                break;
            case SPEECH_START:
                removeAllMessages();
                Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                String text = null;
                if (!speechStore.isEmpty()) text = (String) speechStore.poll();
                else {
                    int index = new Random().nextInt(speechList.size());
                    String[] texts = speechList.get(index).split(" ");
                    text = texts[0];
                    if (texts.length > 1)
                        speechStore.addAll(Arrays.asList(Arrays.copyOfRange(texts, 1, texts.length)));
                }

                speechBody.setText(text);
                speechParams.width = (int) (speechBody.getPaint().measureText(speechBody.getText().toString()) + 120);
                switch (BEFORE_MODE) {
                    case TIMER_START:
                        speechParams.x = params.x;
                        speechParams.y = params.y - params.height / 2 - speechParams.height / 2 - 20;
                        speechBody.setBackgroundResource(R.drawable.speech_bottom);
                        break;
                    case TIMER_TOP_START:
                        speechParams.x = params.x;
                        speechParams.y = (int)(params.y + params.height / 2 + speechParams.height / 2 - params.height * 0.2);
                        speechBody.setBackgroundResource(R.drawable.speech_top);
                        break;
                    case TIMER_LEFT_START:
                        speechParams.x = (int)(params.x + params.width / 2 + speechParams.width / 2 - params.width * 0.4 - this.whDif/2);
                        speechParams.y = params.y;
                        speechBody.setBackgroundResource(R.drawable.speech_left);
                        break;
                    case TIMER_RIGHT_START:
                        speechParams.x = (int)(params.x - params.width / 2 - speechParams.width / 2 + params.width * 0.4 + this.whDif/2);
                        speechParams.y = params.y;
                        speechBody.setBackgroundResource(R.drawable.speech_right);
                        break;
                    default:
                        return;
                }
                if (speechView.getVisibility() != View.VISIBLE)
                    speechView.setVisibility(View.VISIBLE);
                MyService.wm.updateViewLayout(speechView, speechParams);
                sendEmptyMessageDelayed(SPEECH_STOP, 3000);
                break;
            case SPEECH_STOP:
                sendEmptyMessage(BEFORE_MODE);
                break;
            case SLEEP:
                if (CURRENT_ACTION != SLEEP) CURRENT_ACTION = SLEEP;
                removeMessages(RUN_LEFT);
                removeMessages(RUN_RIGHT);
                break;
            case CALL:
                removeMessages(CALL);
                callFunction();
                break;
            case PROP:
                removeMessages(PROP);
                propFunction();
                break;
            case VOICE:
                removeMessages(VOICE);
                voiceFunction();
                break;
            case FALL_TO_THE_GROUND:
                removeAllMessages();
                elfBody.setImageDrawable(direction.equals("left") ? fallToGroundLeftGifDrawable : fallToGroundRightGifDrawable);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                        if (direction.equals("left")) standLeft();
                        else standRight();
                    }
                }, 200);
                sendEmptyMessageDelayed(TIMER_START, 3000);
                break;
            case FLY:
                if (BEFORE_MODE != FLY) BEFORE_MODE = FLY;
                if (CURRENT_ACTION != FLY) CURRENT_ACTION = FLY;
                removeAllMessages();
                Map<String, Long> data = (Map<String, Long>) msg.obj;
                if (data != null) {

                    vX0 = 0;
                    vY0 = 0;
                    long moveXDirection = data.get("moveXDirection");
                    long moveYDirection = data.get("moveYDirection");

                    direction = Math.abs(moveXDirection) > moveMin ? moveXDirection > 0 ? "right" : "left" : direction;

                    if (data.get("jump") != null) jump();
                    else fly();

                    if(Math.abs(moveXDirection) > moveMin)vX0 = moveXDirection * v0;
                    if(Math.abs(moveYDirection) > moveMin)vY0 = moveYDirection * v0;

                }
                params.y = (int) (params.y + vY0 + (1 / 2) * g);
                fs = vX0 == 0 ? 0 : vX0 < 0 ? fs : -fs;
                params.x = (int) (params.x + vX0 + (1 / 2) * fs);
                vX0 = vX0 + fs;
                vY0 = vY0 + g;
                int flag = -1;

                if(vY0 > downVy * g * 0.7 && !isOnceFly){
                    flag = 0;
                }

                if (params.y + params.height / 2 + MyService.deviation > size.y / 2) {
                    params.y = size.y / 2 - params.height / 2 - MyService.deviation;
                    flag = 0;
                } else if (params.y - params.height / 2 < -size.y / 2) {
                    params.y = -size.y / 2 + params.height / 2;
                    if(flag == -1)flag = 1;
                }

                if (params.x - params.width / 2 < -size.x / 2) {
                    params.x = -size.x / 2 + params.width / 2 - whDif / 2;
                    if(flag == -1)flag = 2;
                } else if (params.x + params.width / 2> size.x / 2) {
                    params.x = size.x / 2 - params.width / 2 + whDif / 2;
                    if(flag == -1)flag = 3;
                }
                switch (flag){
                    case 0:
                        if(isOnceFly)isOnceFly = false;
                        sendEmptyMessage(FALL_TO_THE_GROUND);
                        break;
                    case 1:sendEmptyMessage(TIMER_TOP_START);
                        break;
                    case 2:sendEmptyMessage(TIMER_LEFT_START);
                        break;
                    case 3:sendEmptyMessage(TIMER_RIGHT_START);
                        break;
                }

                MyService.wm.updateViewLayout(elfView, params);
                sendEmptyMessageDelayed(FLY, 50);
                break;
            case HIDDEN_CONTAINER:
                removeMessages(HIDDEN_CONTAINER);
                if(downContainerView.getVisibility() == VISIBLE){
                    downContainerView.setVisibility(View.GONE);
                    this.downContainerView.removeAllViews();
                }
                break;
        }
    }

    public void removeAllMessages() {
        if(elfView.getVisibility() != VISIBLE)elfView.setVisibility(VISIBLE);
        removeMessages(TIMER_START);
        removeMessages(TIMER_LEFT_START);
        removeMessages(TIMER_RIGHT_START);
        removeMessages(TIMER_TOP_START);
        removeMessages(RUN_LEFT);
        removeMessages(RUN_RIGHT);
        removeMessages(CLIMB_UP);
        removeMessages(CLIMB_DOWN);
        removeMessages(FLY);
        removeMessages(SLEEP);
        removeMessages(SPEECH_START);
        removeMessages(SPEECH_STOP);
        removeMessages(FALL_TO_THE_GROUND);
        removeMessages(LEFT_STAND);
        removeMessages(RIGHT_STAND);
        speechView.setVisibility(View.GONE);
    }

    private void sleep() {
        int radom = new Random().nextInt(sleepSize);
        elfBody.setImageDrawable(this.stayAnimations.get(direction).get(radom));
        sendEmptyMessage(SLEEP);
    }

    private void fly() {
        elfBody.setImageDrawable(direction.equals("left") ? flyLeftGifDrawable : flyRightGifDrawable);
    }

    private void jump() {
        elfBody.setImageDrawable(direction.equals("left") ? jumpLeftGifDrawable : jumpRightGifDrawable);
    }

    private void walkToLeft() {
        direction = "left";
        if(BEFORE_MODE == TIMER_START)elfBody.setImageDrawable(runAnimations.get("left").get(new Random().nextInt(runSize)));
        sendEmptyMessage(RUN_LEFT);

    }

    private void walkToRight() {
        direction = "right";
        if(BEFORE_MODE == TIMER_START)elfBody.setImageDrawable(runAnimations.get("right").get(new Random().nextInt(runSize)));
        sendEmptyMessage(RUN_RIGHT);
    }

    private void standLeft() {
        direction = "left";
        if (CURRENT_ACTION != LEFT_STAND) CURRENT_ACTION = LEFT_STAND;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_START ? standLeftGifDrawable : climbTopLeftStandGifDrawable);
    }

    private void standRight() {
        direction = "right";
        if (CURRENT_ACTION != RIGHT_STAND) CURRENT_ACTION = RIGHT_STAND;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_START ? standRightGifDrawable : climbTopRightStandGifDrawable);
    }

    private void climbToUp() {
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_LEFT_START ? climbLeftUpGifDrawable :
                climbRightUpGifDrawable);
        sendEmptyMessage(CLIMB_UP);

    }

    private void climbToDown() {
        sendEmptyMessage(CLIMB_DOWN);
    }

    private void climbStand() {
        if (CURRENT_ACTION != CLIMB_STAND) CURRENT_ACTION = CLIMB_STAND;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_LEFT_START ? climbLeftStandGifDrawable : climbRightStandGifDrawable);
    }

    public void go() {
        Message msg = new Message();
        Map<String, Long> data = new HashMap<>();
        data.put("moveXDirection", 0L);
        data.put("moveYDirection", 0L);
        msg.what = Pet.FLY;
        msg.obj = data;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                elfView.setVisibility(View.VISIBLE);
            }
        }, 500);
        sendMessage(msg);
    }

    public void callFunction() {

        if(this.downContainerView.getVisibility() != VISIBLE)this.downContainerView.setVisibility(VISIBLE);
        int randomCount = new Random().nextInt(50) + 180;
        int randomIndex;
        int i = 0;
        CountDownLatch cdl = new CountDownLatch(randomCount);
        this.downList.add(cdl);
        while (i < randomCount) {
            randomIndex = new Random().nextInt(this.callTexts.size());
            CallMsg cm = new CallMsg(ctx, size, this.callTexts.get(randomIndex), cdl, name);
            this.downContainerView.addView(cm.callView, cm.callParams);
            i++;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
                    downList.remove(cdl);
                    if(downList.size() <= 0)sendEmptyMessage(HIDDEN_CONTAINER);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void propFunction() {

        if(this.downContainerView.getVisibility() != VISIBLE)this.downContainerView.setVisibility(VISIBLE);
        int randomCount = new Random().nextInt(50) + 180;
        int randomIndex;
        int i = 0;
        CountDownLatch cdl = new CountDownLatch(randomCount);
        this.downList.add(cdl);
        while (i < randomCount) {
            randomIndex = new Random().nextInt(this.propList.size());
            PropMsg pm = new PropMsg(ctx, size, this.propList.get(randomIndex), cdl, -1);
            this.downContainerView.addView(pm.propView, pm.propParams);
            i++;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
                    downList.remove(cdl);
                    if(downList.size() <= 0)sendEmptyMessage(HIDDEN_CONTAINER);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void voiceFunction() {
        if (this.voiceIds == null || voiceIds.isEmpty()) return;
        playVoice("voices/" + this.voiceIds.get(new Random().nextInt(this.voiceIds.size())));
    }

    private void playVoice(String resId){
        voice(MyService.OSS_BASE + name + "/" + resId + mscExt);
    }

    synchronized private void voice(String resId) {
        if (this.mp.get(1) != null) {
            if (this.mp.get(1).isPlaying()) this.mp.get(1).stop();
            this.mp.get(1).release();
        }
        MediaPlayer mp1 = new MediaPlayer();
        mp1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        try {
            this.mp.put(1, mp1);
            mp1.setDataSource(resId);
            mp1.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.mp.get(1).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                mp1.stop();
                mp1.release();
                mp.put(1, null);
            }
        });
    }

    private void showFuncPanel() {
        if(this.functionPanelView != null)return;
        this.functionPanelView = LayoutInflater.from(ctx).inflate(funcPanelLayoutResId, null);
        this.functionPanelCallButton = functionPanelView.findViewById(R.id.call);
        this.functionPanelPropButton = functionPanelView.findViewById(R.id.prop);
        this.functionPanelVoiceButton = functionPanelView.findViewById(R.id.voice);
        this.closeFuncPanelButton = functionPanelView.findViewById(R.id.close_func_panel);

        closeFuncPanelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFuncPanel();
                sendEmptyMessage(BEFORE_MODE);
            }
        });
        functionPanelCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmptyMessage(CALL);
                hideFuncPanel();
                sendEmptyMessage(BEFORE_MODE);
            }
        });
        functionPanelPropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmptyMessage(PROP);
                hideFuncPanel();
                sendEmptyMessage(BEFORE_MODE);
            }
        });
        functionPanelVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmptyMessage(VOICE);
                hideFuncPanel();
                sendEmptyMessage(BEFORE_MODE);
            }
        });


        functionPanelParams.width = (int)(200 * ctx.getResources().getDisplayMetrics().density + 0.5f);
        functionPanelParams.height = (int)(200 * ctx.getResources().getDisplayMetrics().density + 0.5f);
        switch (BEFORE_MODE) {
            case TIMER_START:
                functionPanelParams.x = params.x;
                functionPanelParams.y = params.y - params.height / 2 - functionPanelParams.height / 2;
                break;
            case TIMER_TOP_START:
                functionPanelParams.x = params.x;
                functionPanelParams.y = params.y + params.height / 2 + functionPanelParams.height / 4;
                break;
            case TIMER_LEFT_START:
                functionPanelParams.x = params.x + params.width / 2 + functionPanelParams.width / 5;
                functionPanelParams.y = params.y;
                break;
            case TIMER_RIGHT_START:
                functionPanelParams.x = params.x - params.width / 2 - functionPanelParams.width / 5;
                functionPanelParams.y = params.y;
                break;
        }
        MyService.wm.addView(functionPanelView, functionPanelParams);
    }

    private void initGifDrawables() {
        try {
            this.moveLeftGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_LEFT + imageExt);
            this.moveRightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_RIGHT + imageExt);
            this.moveRightLightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_RIGHT_LIGHT + imageExt);
            this.moveLeftLightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_LEFT_LIGHT + imageExt);
            this.moveLeftMiddleGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_LEFT_MIDDLE + imageExt);
            this.moveRightMiddleGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_RIGHT_MIDDLE + imageExt);
            this.moveLeftWeightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_LEFT_WEIGHT + imageExt);
            this.moveRightWeightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + MOVE_RIGHT_WEIGHT + imageExt);
            this.climbLeftStandGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + CLIMB_LEFT_STAND + imageExt);
            this.climbRightStandGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + CLIMB_RIGHT_STAND + imageExt);
            this.climbTopLeftStandGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + CLIMB_TOP_LEFT_STAND + imageExt);
            this.climbTopRightStandGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + CLIMB_TOP_RIGHT_STAND + imageExt);
            this.flyLeftGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + FLY_LEFT + imageExt);
            this.flyRightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + FLY_RIGHT + imageExt);
            this.fallToGroundLeftGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + FALL_TO_GROUND_LEFT + imageExt);
            this.fallToGroundRightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + FALL_TO_GROUND_RIGHT + imageExt);
            this.jumpLeftGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + JUMP_LEFT + imageExt);
            this.jumpRightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + JUMP_RIGHT + imageExt);
            this.standLeftGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + STAND_LEFT + imageExt);
            this.standRightGifDrawable = new GifDrawable(ctx.getAssets(), name + "/" + STAND_RIGHT + imageExt);

            int resId = ctx.getResources().getIdentifier(name + "_sleeps", "array", ctx.getPackageName());
            String[] sleepGifDrawableStrs = ctx.getResources().getStringArray(resId);
            sleepSize = sleepGifDrawableStrs.length;
            if (this.stayAnimations == null) this.stayAnimations = new HashMap<>();
            List<GifDrawable> leftSleeps = new LinkedList<>();
            List<GifDrawable> rightSleeps = new LinkedList<>();
            for (int k = 0; k < sleepSize; k++) {
                leftSleeps.add(new GifDrawable(ctx.getAssets(), name + "/" + sleepGifDrawableStrs[k] + "_left" + imageExt));
                rightSleeps.add(new GifDrawable(ctx.getAssets(), name + "/" + sleepGifDrawableStrs[k] + "_right" + imageExt));
            }
            this.stayAnimations.put("left", leftSleeps);
            this.stayAnimations.put("right", rightSleeps);


            if (this.runAnimations == null) this.runAnimations = new HashMap<>();
            List<GifDrawable> leftRuns = new LinkedList<>();
            List<GifDrawable> rightRuns = new LinkedList<>();
            leftRuns.add(new GifDrawable(ctx.getAssets(), name + "/" + WALK_LEFT + imageExt));
            rightRuns.add(new GifDrawable(ctx.getAssets(), name + "/" + WALK_RIGHT + imageExt));
            runSize = 1;
            int resId2 = ctx.getResources().getIdentifier(name + "_runs", "array", ctx.getPackageName());
            if (resId2 != 0) {
                String[] runGifDrawableStrs = ctx.getResources().getStringArray(resId2);
                runSize = runGifDrawableStrs.length;
                for (int k = 0; k < runSize; k++) {
                    leftRuns.add(new GifDrawable(ctx.getAssets(), name + "/" + runGifDrawableStrs[k] + "_left" + imageExt));
                    rightRuns.add(new GifDrawable(ctx.getAssets(), name + "/" + runGifDrawableStrs[k] + "_right" + imageExt));
                }
                runSize++;
            }
            this.runAnimations.put("left", leftRuns);
            this.runAnimations.put("right", rightRuns);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSpeecList() {
        int resId = ctx.getResources().getIdentifier(name + "_speechs", "array", ctx.getPackageName());
        this.speechList = new LinkedList<String>(Arrays.asList(ctx.getResources().getStringArray(resId)));
    }

    private void initCallTexts() {
        int resId = ctx.getResources().getIdentifier(name + "_calls", "array", ctx.getPackageName());
        this.callTexts = new LinkedList<String>(Arrays.asList(ctx.getResources().getStringArray(resId)));
    }

    private void initPropList() {
        int resId = ctx.getResources().getIdentifier(name + "_props", "array", ctx.getPackageName());
        String[] propDrawableStrs = ctx.getResources().getStringArray(resId);
        if (this.propList == null) this.propList = new LinkedList<Integer>();
        int id;
        for (String str : propDrawableStrs) {
            id = ctx.getResources().getIdentifier(str, "drawable", ctx.getPackageName());
            this.propList.add(id);
        }
    }

    private void initVoiceList() {
        int resId = ctx.getResources().getIdentifier(name + "_voices", "array", ctx.getPackageName());
        if (resId != 0) {
            String[] voiceStrs = ctx.getResources().getStringArray(resId);
            if (this.voiceIds == null) this.voiceIds = new LinkedList<>();
            this.voiceIds.addAll(Arrays.asList(voiceStrs));
        }


    }

    private void initWhRate(){
        int resId = ctx.getResources().getIdentifier(name + "_wh_rate", "string", ctx.getPackageName());
        if(resId != 0){
            this.whRate = Double.valueOf(ctx.getResources().getString(resId));
        }
    }

    private void initFuncPanelLayoutResId(){
        this.funcPanelLayoutResId = ctx.getResources().getIdentifier(name + "_func_panel_layout", "layout", ctx.getPackageName());
        if(this.funcPanelLayoutResId == 0)this.funcPanelLayoutResId = R.layout.function_panel;

    }


    public void hideFuncPanel(){
        if(this.functionPanelView != null){
            MyService.wm.removeView(this.functionPanelView);
            this.functionPanelView = null;
        }
    }

    public void updateSpeed(int speed){
        this.speed = speed;
        this.g = speed * gConst;
        this.fs = speed * fsConst;
    }
}

