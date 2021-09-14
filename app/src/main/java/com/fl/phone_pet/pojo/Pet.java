package com.fl.phone_pet.pojo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fl.phone_pet.MainActivity;
import com.fl.phone_pet.MyService;
import com.fl.phone_pet.MyService2;
import com.fl.phone_pet.R;
import com.fl.phone_pet.utils.CloseWindowUtils;
import com.fl.phone_pet.utils.SensorUtils;
import com.fl.phone_pet.utils.SpeedUtils;
import com.fl.phone_pet.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.SortedSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static android.view.View.VISIBLE;

import androidx.recyclerview.widget.SortedList;

public class Pet extends Handler implements Comparable<Pet>{

    public static final int SPEECH_START = 10000;
    public static final int TIMER_START = 10004;
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
    public static final int MOVE = 10032;
    public static final int HUG = 10033;
    public static final int FALL_TO_GROUND_STAND = 10034;
    public static final int HUG_END = 10035;
    public static final int G_SENSOR_X = 10036;
    public static final int G_SENSOR_XY = 10037;
    public static final int L_SENSOR = 10038;
    public static final int BOOM_CLOSE_WINDOW = 10039;
    public static final int FOLD = 10040;

    public int BEFORE_MODE = FLY;
    public int CURRENT_ACTION = FLY;
    public String name;
    public long id;
    public static final String imageExt = ".png";
    public static final String mscExt = ".mp3";
    public String direction = "left";
    private Integer funcPanelLayoutResId;

    public static final String WALK_LEFT = "run_left";
    public static final String WALK_RIGHT = "run_right";
    public static final String LEFT_CLIMB = "left_climb";
    public static final String RIGHT_CLIMB = "right_climb";
    public static final String TOP_LEFT_CLIMB = "top_left_climb";
    public static final String TOP_RIGHT_CLIMB = "top_right_climb";
    public static final String MOVE_LEFT = "stand_left";
    public static final String MOVE_RIGHT = "stand_right";
    public static final String MOVE_RIGHT_LIGHT = "move_right_light";
    public static final String MOVE_LEFT_LIGHT = "move_left_light";
    public static final String MOVE_RIGHT_MIDDLE = "move_right_middle";
    public static final String MOVE_LEFT_MIDDLE = "move_left_middle";
    public static final String MOVE_RIGHT_WEIGHT = "move_right_weight";
    public static final String MOVE_LEFT_WEIGHT = "move_left_weight";
    public static final String FLY_LEFT = "fall_to_ground_left1";
    public static final String FLY_RIGHT = "fall_to_ground_right1";
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
    public View elfView, speechView, functionPanelView;
    public ImageView elfBody;
    public TextView speechBody;
    public ImageView functionPanelCallButton;
    public ImageView functionPanelPropButton;
    public ImageView functionPanelVoiceButton;
    public ImageView closeFuncPanelButton;
    Drawable flyLeftGifDrawable;
    Drawable flyRightGifDrawable;
    Drawable climbLeftGifDrawable;
    Drawable climbLeftStandGifDrawable;
    Drawable climbRightGifDrawable;
    Drawable climbRightStandGifDrawable;
    Drawable climbTopLeftGifDrawable;
    Drawable climbTopRightGifDrawable;
    Drawable climbTopLeftStandGifDrawable;
    Drawable climbTopRightStandGifDrawable;
    public Drawable moveLeftGifDrawable;
    public Drawable moveRightGifDrawable;
    public Drawable moveRightLightGifDrawable;
    public Drawable moveLeftLightGifDrawable;
    public Drawable moveRightWeightGifDrawable;
    public Drawable moveLeftWeightGifDrawable;
    public Drawable moveRightMiddleGifDrawable;
    public Drawable moveLeftMiddleGifDrawable;
    Drawable fallToGroundLeftGifDrawable;
    Drawable fallToGroundRightGifDrawable;
    Drawable jumpLeftGifDrawable;
    Drawable jumpRightGifDrawable;
    Drawable standLeftGifDrawable;
    Drawable standRightGifDrawable;
    List<String> speechList;
    List<String> voiceIds;
    List<Integer> propList;
    Map<String, List<Drawable>> stayAnimations;
    Map<String, List<Drawable>> runAnimations;
    Map<String, List<Drawable>> hugEndAnimations;
    int sleepSize;
    int runSize;
    int hugEndSize;
    List<String> callTexts;
    public double whRate;
    public int whDif;
    public boolean isOnceFly = true;
    public boolean isMoveFly = false;
    public int pngDev;
    public CountDownLatch lSensorCdl;
    final int distance = 30;
    final int downVy = 7;

    public static float g = 9.8f;
    public static float fs = 2f;
    private float vX0, vY0;
    private long lheight;
    private final long moveMin = 10;
    private final float v0 = 1.2f;
    private Context ctx;
    private Queue speechStore = new LinkedBlockingQueue(10);
    public Pet hugPet;
    public RelativeLayout aiXinContainer;
    public WindowManager.LayoutParams aiXinContainerParams;

    List<Integer> runStateCounts;
    List<Integer> sleepStateCounts;
    List<Integer> hugEndStateCounts;
    int stateIndex;
    int stateCount;
    long lastClickTime;


    public Pet(Context ctx, long id, String name) {
        super();
        this.id = id;
        this.name = name;
        this.ctx = ctx;
        this.params = new WindowManager.LayoutParams();
        this.speechParams = new WindowManager.LayoutParams();
        this.functionPanelParams = new WindowManager.LayoutParams();
        this.elfView = LayoutInflater.from(ctx).inflate(R.layout.petelf, null);
        this.speechView = LayoutInflater.from(ctx).inflate(R.layout.speech, null);
        this.elfBody = elfView.findViewById(R.id.elfbody);
        this.speechBody = speechView.findViewById(R.id.speech);
        initStateRes();
        initPropList();
        initVoiceList();
        initSpeechList();
        initCallTexts();
        initWhRate();
        initFuncPanelLayoutResId();
        initParams();
        initView();
        initBindEvent();
    }


    private void initParams() {
        try {
            speechParams.height = (int) (MyService.currentSize * 7.5);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//6.0
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


            params.flags = Utils.getNormalFlags();
            if(!MyService.isEnableTouch)params.flags = Utils.getNormalFlags() | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

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
            int petW = (int) (MyService.size.x * (MyService.currentSize / 100.0));

            params.width = whRate != 0 && whRate != 1 ? (int)(petW * whRate) : petW;
            //params.width = 0;
            pngDev = (int)(0.08 * params.width);
            params.height = petW;

            whDif = params.width - params.height + 2;
//            whDif = 0;

            params.x = 0;
            params.y = -MyService.size.y / 2 + petW / 2 + 20 + MyService.statusBarHeight;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        speechBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, MyService.currentSize / 2);
        elfView.setVisibility(VISIBLE);
        MyService.wm.addView(elfView, params);
        MyService.wm.addView(speechView, speechParams);
    }

    private void initBindEvent() {
        Pet me = this;
        elfBody.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY, dx, dy, x0, y0, tempX, tempY;
            long downTime, upTime, moveTime;
            boolean isDown = false;
            Bitmap bitmap = null;
            int eventX = -1;
            int eventY = -1;
            long moveX = -1L;
            long moveY = -1L;
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDown = false;
                        bitmap = ((BitmapDrawable)(elfBody.getDrawable().getCurrent())).getBitmap();
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        eventX = (int)(event.getX());
                        eventY = (int)(event.getY());
                        x0 = lastX;
                        y0 = lastY;
                        Matrix matrix = new Matrix();
                        float scale = (float)((elfBody.getHeight() * 1.0)/bitmap.getHeight());
                        matrix.postScale(scale, scale);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        eventX = eventX - (elfView.getWidth() - bitmap.getWidth()) / 2;
                        if(eventX < 0 || eventY < 0 || eventX >= bitmap.getWidth() || eventY >= bitmap.getHeight() || bitmap.getPixel(eventX, eventY) == 0){
                            chooseDownPet(event);
                            dispatchEvent(event);
                            //if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty())pp((int)(event.getRawX()), (int)(event.getRawY()));
                            return true;
                        }
                        //if(CURRENT_ACTION == HUG){
                            //Utils.voice(MyService.OSS_BASE + "lw/mscs/gunba.mp3");
                            //return false;
                        //}

                        if(CURRENT_ACTION == FOLD){
                            CloseWindowUtils.foldPets.remove(me);
                            if(CloseWindowUtils.foldPets.isEmpty()){
                                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(CloseWindowUtils.foldLayout, "factor", 0);
                                objectAnimator.setDuration(SpeedUtils.getCurrentSpeedTime() * 5);
                                objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                                objectAnimator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        CloseWindowUtils.endFold();
                                    }
                                });
                                objectAnimator.start();
                            }

                        }

                        if(CURRENT_ACTION != HUG){
                            CURRENT_ACTION = MOVE;
                            if(MyService.isLSensor && hugPet != null && SensorUtils.isInCouple(me)){
                                hugPet.hugPet = null;
                                hugPet = null;
                                if(lSensorCdl != null){
                                    lSensorCdl.countDown();
                                    lSensorCdl.countDown();
                                }
                            }
                        }


                        if(isOnceFly)isOnceFly = false;
                        isDown = true;
                        downTime = System.currentTimeMillis();
                        removeAllMessages();

                        if(downTime - lastClickTime < 300 && CURRENT_ACTION != HUG){
                            removeMessages(SPEECH_START);
                            speechView.setVisibility(View.GONE);
                            if(MyService.isVibrator){
                                Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
                                vibrator.vibrate(100);
                            }
                            showFuncPanel();
                        }
                        lastClickTime = downTime;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        tempX = (int) (event.getRawX() < 0 ? 0 : event.getRawX() > MyService.size.x ? MyService.size.x : event.getRawX());
                        tempY = (int) (event.getRawY() < 0 ? 0 : event.getRawY() > MyService.size.y ? MyService.size.y : event.getRawY());
                        if(!isDown){
                            dispatchEvent(event);
                            //if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty())gg(lastX, lastY, tempX, tempY);
                            return false;
                        }
                        moveTime = System.currentTimeMillis();

                        dx = tempX - lastX;
                        dy = tempY - lastY;

                        params.x = params.x + dx;
                        params.y = params.y + dy;
                        if(CURRENT_ACTION == HUG && hugPet != null){
                            hugPet.params.x = hugPet.params.x + dx;
                            hugPet.params.y = hugPet.params.y + dy;
                            if(aiXinContainer != null){
                                aiXinContainerParams.x = aiXinContainerParams.x + dx;
                                aiXinContainerParams.y = aiXinContainerParams.y + dy;
                            }
                        }

                        lastX = tempX;
                        lastY = tempY;

                        if (CURRENT_ACTION != HUG && (dx != 0 || dy != 0)) {
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
                            else
                                elfBody.setImageDrawable(direction.equals("left") ? moveLeftGifDrawable : moveRightGifDrawable);
                            elfBody.setImageLevel(0);
//                            getMaxWidth();

                          }
                        else if (CURRENT_ACTION != HUG && lastX - x0 == 0 && lastY - y0 == 0 && BEFORE_MODE != Pet.FLY && moveTime - downTime > 850) {
                            if(MyService.isVibrator){
                                Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
                                vibrator.vibrate(100);
                            }
                            showFuncPanel();
                            return true;
                        }
                        MyService.wm.updateViewLayout(elfView, params);
                        if(CURRENT_ACTION == HUG && hugPet != null){
                            MyService.wm.updateViewLayout(hugPet.elfView, hugPet.params);
                            if(aiXinContainer != null)MyService.wm.updateViewLayout(aiXinContainer, aiXinContainerParams);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        moveX = (long) event.getRawX() - x0;
                        moveY = (long) event.getRawY() - y0;
                        if(!isDown){
                            dispatchEvent(event);
                            if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty() && moveX == 0 && moveY == 0)pp((int)(event.getRawX()), (int)(event.getRawY()));
//                            if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty() && (moveX != 0 || moveY != 0)){
//                                Log.i("-----x0------", String.valueOf(x0));
//                                Log.i("-----y0------", String.valueOf(y0));
//                                Log.i("-----ex0------", String.valueOf(event.getRawX()));
//                                Log.i("-----ey0------", String.valueOf(event.getRawY()));
//                                gg(x0, y0, (int)(event.getRawX()), (int)(event.getRawY()));
//                            }

                            if(MyService.pets != null)MyService.pets = null;
                            if(MyService.downPet != null)MyService.downPet = null;
                            if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty()){
                                List<Pet> choosedPets = MyService.choosedPets;
                                postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(MyService.wm == null || !MyService.isEnableTouch || Utils.isGroupPetsEmpty())return;
                                        for(Pet pet : choosedPets){
                                           pet.params.flags = Utils.getNormalFlags();
                                           MyService.wm.updateViewLayout(pet.elfView, pet.params);
                                        }

                                    }
                                }, 200);
                                MyService.choosedPets = null;

                            }
                            return false;
                        }
                        upTime = System.currentTimeMillis();

                        if (CURRENT_ACTION != HUG && functionPanelView == null && moveX == 0 && moveY == 0 && BEFORE_MODE != FLY && upTime - downTime <= 500) {
                            sendEmptyMessageDelayed(SPEECH_START, 50);
                            return true;
                        } else if (CURRENT_ACTION != HUG && moveX == 0 && moveY == 0 && BEFORE_MODE != FLY && upTime - downTime > 500 && upTime - downTime <= 850) {
                            sendEmptyMessage(BEFORE_MODE);
                            return true;
                        }
                        else if (CURRENT_ACTION == HUG || (moveX == 0 && moveY == 0 && BEFORE_MODE != FLY && upTime - downTime > 850)) {
                            return true;
                        }
                        Message msg = new Message();
                        Map<String, Long> data = new HashMap<>();
                        data.put("moveXDirection", (long)dx);
                        data.put("moveYDirection", (long)dy);
                        data.put("isMoveFly", 1L);
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
        elfBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long currentTime = System.currentTimeMillis();
                if(currentTime - lastClickTime < 1000){
                    if(MyService.isVibrator){
                        Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
                        vibrator.vibrate(100);
                    }
                    showFuncPanel();
                }
                lastClickTime = currentTime;
            }
        });

    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case TIMER_START:
                removeAllMessages();
                if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                int j = (int) (Math.random() * (7));
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
                        msg3.what
                        = FLY;
                        sendMessage(msg3);
                        break;
                    case 6:
                        if(MyService.isCloseWindowEnabled && MyService2.as != null && !CloseWindowUtils.isClosing && new Random().nextInt(20) < 4) boomCloseWindow();
                        break;

                }
                sendEmptyMessageDelayed(TIMER_START, SpeedUtils.getCurrentFrequestTime());
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
                sendEmptyMessageDelayed(TIMER_TOP_START, SpeedUtils.getCurrentFrequestTime());
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
                        int currentWidth = (int)(params.height * (this.flyLeftGifDrawable.getCurrent().getIntrinsicWidth() * 1.0 / this.flyLeftGifDrawable.getCurrent().getIntrinsicHeight()));
                        params.x = -MyService.size.x/2 + currentWidth / 2;
                        MyService.wm.updateViewLayout(elfView, params);
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
                sendEmptyMessageDelayed(TIMER_LEFT_START, SpeedUtils.getCurrentFrequestTime());
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
                        int currentWidth = (int)(params.height * (this.flyLeftGifDrawable.getCurrent().getIntrinsicWidth() * 1.0 / this.flyLeftGifDrawable.getCurrent().getIntrinsicHeight()));
                        params.x = MyService.size.x/2 - currentWidth / 2;
                        MyService.wm.updateViewLayout(elfView, params);
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
                sendEmptyMessageDelayed(TIMER_RIGHT_START, SpeedUtils.getCurrentFrequestTime());
                break;
            case RUN_LEFT:
                if (CURRENT_ACTION != RUN_LEFT) CURRENT_ACTION = RUN_LEFT;
                removeMessages(RUN_LEFT);
                changeStateLevel();
                if(params.x - params.width / 2 + whDif / 2 + pngDev < (-MyService.size.x / 2)){
                    removeAllMessages();

                    if(params.x != -MyService.size.x / 2 + params.width / 2 - whDif / 2){
                        params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if(BEFORE_MODE == TIMER_TOP_START){
                        if (BEFORE_MODE != TIMER_LEFT_START) BEFORE_MODE = TIMER_LEFT_START;
                        climbToDown();
                        sendEmptyMessageDelayed(TIMER_LEFT_START, SpeedUtils.getCurrentFrequestTime());
                    }else {
                        foldWindow();
//                        if(MyService.isCloseWindowEnabled && MyService2.as != null && !CloseWindowUtils.isClosing && new Random().nextInt(20) < 4){
//                            removeAllMessages();
//                            foldWindow();
//                        }else{
//                            if (BEFORE_MODE != TIMER_LEFT_START) BEFORE_MODE = TIMER_LEFT_START;
//                            climbToUp();
//                            sendEmptyMessageDelayed(TIMER_LEFT_START, SpeedUtils.getCurrentFrequestTime());
//                        }

                    }
                }else if(CloseWindowUtils.isClosing && !CloseWindowUtils.foldPets.isEmpty() && Math.abs( -MyService.size.x/2 + CloseWindowUtils.foldLayout.getWidth() - params.x) <= 10){
                    removeAllMessages();
                    foldWindow();
                }else {
                    params.x = params.x - distance;
                    MyService.wm.updateViewLayout(elfView, params);
                    sendEmptyMessageDelayed(RUN_LEFT, SpeedUtils.getCurrentSpeedTime());

                }
                break;
            case RUN_RIGHT:
                if (CURRENT_ACTION != RUN_RIGHT) CURRENT_ACTION = RUN_RIGHT;
                removeMessages(RUN_RIGHT);
                changeStateLevel();
                if(params.x + params.width / 2 - whDif / 2 - pngDev> (MyService.size.x / 2)){
                    removeAllMessages();

                    if(params.x != MyService.size.x / 2 - params.width / 2 + whDif / 2){
                        params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE == TIMER_TOP_START){
                        if (BEFORE_MODE != TIMER_RIGHT_START) BEFORE_MODE = TIMER_RIGHT_START;
                        climbToDown();
                        sendEmptyMessageDelayed(TIMER_RIGHT_START, SpeedUtils.getCurrentFrequestTime());
                    }else{
                        if (BEFORE_MODE != TIMER_RIGHT_START) BEFORE_MODE = TIMER_RIGHT_START;
                        climbToUp();
                        sendEmptyMessageDelayed(TIMER_RIGHT_START, SpeedUtils.getCurrentFrequestTime());
                    }
                }else {
                    params.x = params.x + distance;
                    MyService.wm.updateViewLayout(elfView, params);
                    sendEmptyMessageDelayed(RUN_RIGHT, SpeedUtils.getCurrentSpeedTime());
                }

                break;
            case CLIMB_UP:
                if (CURRENT_ACTION != CLIMB_UP) CURRENT_ACTION = CLIMB_UP;
                removeMessages(CLIMB_UP);
                changeStateLevel();
                if (params.y - params.height / 2 < -MyService.size.y / 2 + MyService.statusBarHeight) {
                    removeAllMessages();
                    if(params.y != -MyService.size.y / 2 + params.height / 2 + MyService.statusBarHeight){
                        params.y = -MyService.size.y / 2 + params.height / 2 + MyService.statusBarHeight;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE == TIMER_LEFT_START) {
                        if (BEFORE_MODE != TIMER_TOP_START) BEFORE_MODE = TIMER_TOP_START;
                        params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                        walkToRight();
//                        int currentWidth = getMaxWidth();
//                        if(params.x != -MyService.size.x / 2 + currentWidth / 2){
//                            params.x = -MyService.size.x / 2 + currentWidth / 2;
//                            MyService.wm.updateViewLayout(elfView, params);
//                        }
                        sendEmptyMessageDelayed(TIMER_TOP_START, SpeedUtils.getCurrentFrequestTime());
                    } else {
                        if (BEFORE_MODE != TIMER_TOP_START) BEFORE_MODE = TIMER_TOP_START;
                        params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                        walkToLeft();
//                        int currentWidth = getMaxWidth();
//                        if(params.x != MyService.size.x / 2 - currentWidth / 2){
//                            params.x = MyService.size.x / 2 - currentWidth / 2;
//                            MyService.wm.updateViewLayout(elfView, params);
//                        }
                        sendEmptyMessageDelayed(TIMER_TOP_START, SpeedUtils.getCurrentFrequestTime());
                    }
                } else {
                    params.y = params.y - distance;
                    MyService.wm.updateViewLayout(elfView, params);
                    sendEmptyMessageDelayed(CLIMB_UP, SpeedUtils.getCurrentSpeedTime());
                }
                break;
            case CLIMB_DOWN:
                if (CURRENT_ACTION != CLIMB_DOWN) CURRENT_ACTION = CLIMB_DOWN;
                removeMessages(CLIMB_DOWN);
                changeStateLevel();
                if (params.y + params.height / 2 > MyService.size.y / 2) {
                    removeAllMessages();
                    if(params.y != MyService.size.y / 2 - params.height / 2){
                        params.y = MyService.size.y / 2 - params.height / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE == TIMER_LEFT_START) {
                        if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                        walkToRight();
//                        int currentWidth = getMaxWidth();
//                        if(params.x != -MyService.size.x / 2 + currentWidth / 2){
//                            params.x = -MyService.size.x / 2 + currentWidth / 2;
//                            MyService.wm.updateViewLayout(elfView, params);
//                        }
                        params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2 - pngDev;
                        MyService.wm.updateViewLayout(elfView, params);
                        sendEmptyMessageDelayed(TIMER_START, SpeedUtils.getCurrentFrequestTime());
                    } else {
                        if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                        params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2 + pngDev;
                        MyService.wm.updateViewLayout(elfView, params);
                        walkToLeft();
//                        int currentWidth = getMaxWidth();
//                        if(params.x != MyService.size.x / 2 - currentWidth / 2){
//                            params.x = MyService.size.x / 2 - currentWidth / 2;
//                            MyService.wm.updateViewLayout(elfView, params);
//                        }
                        sendEmptyMessageDelayed(TIMER_START, SpeedUtils.getCurrentFrequestTime());
                    }
                } else {
                    params.y = params.y + distance;
//                    if(params.y + params.height / 2 > MyService.size.y / 2)getMaxWidth();
                    MyService.wm.updateViewLayout(elfView, params);
                    sendEmptyMessageDelayed(CLIMB_DOWN, SpeedUtils.getCurrentSpeedTime());
                }
                break;
            case FALL_TO_GROUND_STAND:
                removeMessages(FALL_TO_GROUND_STAND);
                if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                if (direction.equals("left")) groundStandLeft();
                else groundStandRight();
                break;
            case SPEECH_START:
                removeAllMessages();
                if(MyService.isVibrator){
                    Vibrator vibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                }
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
                        speechParams.x = (int)(params.x + params.width / 2 + speechParams.width / 2 - params.width * 0.4);
                        speechParams.y = params.y;
                        speechBody.setBackgroundResource(R.drawable.speech_left);
                        break;
                    case TIMER_RIGHT_START:
                        speechParams.x = (int)(params.x - params.width / 2 - speechParams.width / 2 + params.width * 0.4);
                        speechParams.y = params.y;
                        speechBody.setBackgroundResource(R.drawable.speech_right);
                        break;
                    default:
                        return;
                }
                if (speechView.getVisibility() != View.VISIBLE)
                    speechView.setVisibility(View.VISIBLE);
                MyService.wm.updateViewLayout(speechView, speechParams);
                sendEmptyMessageDelayed(BEFORE_MODE, 6 * SpeedUtils.getCurrentSpeedTime());
                break;
            case SPEECH_STOP:
                sendEmptyMessage(BEFORE_MODE);
                break;
            case SLEEP:
                removeMessages(SLEEP);
                if (CURRENT_ACTION != SLEEP) CURRENT_ACTION = SLEEP;
                changeStateLevel();
                sendEmptyMessageDelayed(SLEEP, SpeedUtils.getCurrentSpeedTime());
                break;
            case HUG:
                removeMessages(HUG);

                break;
            case HUG_END:
                removeMessages(HUG_END);
                if(CURRENT_ACTION != HUG_END)CURRENT_ACTION = HUG_END;
                if(hugPet != null)hugPet = null;
                if(stateIndex + 1 > stateCount){
                    sendEmptyMessage(TIMER_START);
                    return;
                }else{
                    changeStateLevel();
                    params.x = (int)(direction.equals("left") ? params.x - distance * 1.2 : params.x + distance * 1.2);
                    MyService.wm.updateViewLayout(elfView, params);
                    sendEmptyMessageDelayed(HUG_END, SpeedUtils.getCurrentSpeedTime());
                }

                if(direction.equals("right") && params.x + params.width / 2 - whDif / 2 - pngDev> (MyService.size.x / 2)){
                    removeAllMessages();

                    if(params.x != MyService.size.x / 2 - params.width / 2 + whDif / 2){
                        params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE != TIMER_RIGHT_START) BEFORE_MODE = TIMER_RIGHT_START;
                    climbToUp();
                    sendEmptyMessageDelayed(TIMER_RIGHT_START, SpeedUtils.getCurrentFrequestTime());
                }else if(direction.equals("left") && params.x - params.width / 2 + whDif / 2 + pngDev< (-MyService.size.x / 2)){
                    removeAllMessages();

                    if(params.x != -MyService.size.x / 2 + params.width / 2 - whDif / 2){
                        params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2;
                        MyService.wm.updateViewLayout(elfView, params);
                    }

                    if (BEFORE_MODE != TIMER_LEFT_START) BEFORE_MODE = TIMER_LEFT_START;
                    climbToUp();
                    sendEmptyMessageDelayed(TIMER_LEFT_START, SpeedUtils.getCurrentFrequestTime());
                }
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
                if(elfBody.getDrawable() != fallToGroundLeftGifDrawable && elfBody.getDrawable() != fallToGroundRightGifDrawable){
                    removeAllMessages();
                    elfBody.setImageDrawable(direction.equals("left") ? fallToGroundLeftGifDrawable : fallToGroundRightGifDrawable);
                    stateIndex = 0;
                    stateCount = 3;
                }

                if(stateIndex + 1 > 3){
                    if(CURRENT_ACTION == L_SENSOR){
                        if (BEFORE_MODE != TIMER_START) BEFORE_MODE = TIMER_START;
                        if(direction.equals("left"))walkToLeft();
                        else walkToRight();
                    }
                    else{
                        sendEmptyMessage(FALL_TO_GROUND_STAND);
                        sendEmptyMessageDelayed(TIMER_START, SpeedUtils.getCurrentFrequestTime());
                    }
                }else{
                    changeStateLevel();
                    sendEmptyMessageDelayed(FALL_TO_THE_GROUND, (long)(0.6 * SpeedUtils.getCurrentSpeedTime()));
                }

                break;
            case FLY:
                if (BEFORE_MODE != FLY) BEFORE_MODE = FLY;
                if (CURRENT_ACTION != FLY && CURRENT_ACTION != G_SENSOR_XY) CURRENT_ACTION = FLY;
                removeAllMessages();
                Map<String, Long> data = (Map<String, Long>) msg.obj;
                if (data != null) {

                    vX0 = 0;
                    vY0 = 0;
                    long moveXDirection = data.get("moveXDirection");
                    long moveYDirection = data.get("moveYDirection");

                    isMoveFly = data.get("isMoveFly") == null ? false : data.get("isMoveFly") > 0 ? true : false;

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


                if (params.y + params.height / 2 > MyService.size.y / 2) {
                    params.y = MyService.size.y / 2 - params.height / 2;
                    flag = 0;
                } else if (params.y - params.height / 2 < -MyService.size.y / 2 + MyService.statusBarHeight) {
                    params.y = -MyService.size.y / 2 + params.height / 2 + MyService.statusBarHeight;
                    if(flag == -1)flag = 1;
                }

                if (params.x - params.width / 2 + whDif / 2 < -MyService.size.x / 2) {
                    params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2;
                    if(flag == -1)flag = 2;
                } else if (params.x + params.width / 2 - whDif / 2>MyService.size.x / 2) {
                    params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2;
                    if(flag == -1)flag = 3;
                }

                if(!MyService.isGSensorEnabled){
                    int random = new Random().nextInt(20);
                    if(random > 6 && flag == -1 && vY0 > downVy * g * 0.8 && !isOnceFly && !isMoveFly && params.y % MyService.divisionArg == 0){
                        flag = 0;
                    }

                    if((flag == -1 && vY0 >= 0 && isMoveFly && !isOnceFly) || (random <= 6 && flag == -1 && vY0 > downVy * g * 0.8 && !isOnceFly && !isMoveFly)){
                        if(params.y % MyService.divisionArg != 0)params.y = (params.y / MyService.divisionArg + 1) * MyService.divisionArg;
                        params.y = params.y > MyService.size.y/2 - params.height/2 ? MyService.size.y/2 - params.height/2 : params.y;
                        flag = 0;
                    }
                }


                MyService.wm.updateViewLayout(elfView, params);

                sendEmptyMessageDelayed(FLY, (long)(0.18 * SpeedUtils.getCurrentSpeedTime()));

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

                break;
            case L_SENSOR:
                if (BEFORE_MODE != FLY) BEFORE_MODE = FLY;
                if (CURRENT_ACTION != L_SENSOR) CURRENT_ACTION = L_SENSOR;
                removeAllMessages();
                Map<String, Long> data1 = (Map<String, Long>) msg.obj;
                if (data1 != null) {
                    vX0 = 0;
                    vY0 = 0;
                    long moveXDirection = data1.get("moveXDirection");
                    long moveYDirection = data1.get("moveYDirection");
                    lheight = data1.get("lheight");
                    //isMoveFly = data.get("isMoveFly") == null ? false : data.get("isMoveFly") > 0 ? true : false;

                    direction = moveXDirection > 0 ? "right" : "left";

                    fly();

                    if(Math.abs(moveXDirection) > moveMin)vX0 = moveXDirection * v0;
                    if(Math.abs(moveYDirection) > moveMin)vY0 = moveYDirection * v0;

                }
                params.y = (int) (params.y + vY0 + (1 / 2) * g);
                fs = vX0 == 0 ? 0 : vX0 < 0 ? fs : -fs;
                params.x = (int) (params.x + vX0 + (1 / 2) * fs);
                vX0 = vX0 + fs;
                vY0 = vY0 + g;

                if(vY0 < 0 && params.y <= lheight || vY0 >= 0 && params.y >= lheight){
                    params.y = (int)lheight;
                    MyService.wm.updateViewLayout(elfView, params);
                    sendEmptyMessage(FALL_TO_THE_GROUND);

                }else{
                    sendEmptyMessageDelayed(L_SENSOR, (long)(0.18 * SpeedUtils.getCurrentSpeedTime()));
                    MyService.wm.updateViewLayout(elfView, params);
                }
                break;
            case BOOM_CLOSE_WINDOW:
                removeMessages(BOOM_CLOSE_WINDOW);
                int boomCount = msg.arg2;

                if(boomCount == 0 && stateIndex + 1 > 4){
                    boomCount = 1;
                    Map<String, Object> datas = new HashMap<>();
                    datas.put("boomImg", elfBody.getDrawable().getCurrent());
                    CloseWindowUtils.start(ctx, CloseWindowUtils.BOOM, datas);
                }
                else if(boomCount == 1 && stateIndex + 1 > 4){
                    sendEmptyMessageDelayed(TIMER_START, (long)(2.6 * SpeedUtils.getCurrentSpeedTime()));
                    return;
                }

                changeStateLevel();
                Message msg4 = new Message();
                msg4.what = BOOM_CLOSE_WINDOW;
                msg4.arg2 = boomCount;
                sendMessageDelayed(msg4, (long)(0.6 * SpeedUtils.getCurrentSpeedTime()));
                break;
            case FOLD:
                removeMessages(FOLD);
                changeStateLevel();
                Map<String, Float> datas = new HashMap();
                datas.put("factor", CloseWindowUtils.foldLayout.getFactor() + 0.1f * CloseWindowUtils.foldPets.size() * CloseWindowUtils.foldFlag);
                CloseWindowUtils.update(CloseWindowUtils.FOLD, datas);
                params.x = -MyService.size.x/2 + (int)(CloseWindowUtils.foldLayout.getCurrentWidth());
                MyService.wm.updateViewLayout(elfView, params);
                if(CloseWindowUtils.foldLayout.getFactor() >= 1){
                    CloseWindowUtils.foldFlag = -1;
                    MyService2.as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    sendEmptyMessageDelayed(FOLD, (long)(0.2 * SpeedUtils.getCurrentSpeedTime()));
                }else if(CloseWindowUtils.foldLayout.getFactor() <= 0){

                    CloseWindowUtils.endFold();
                }else sendEmptyMessageDelayed(FOLD, (long)(0.2 * SpeedUtils.getCurrentSpeedTime()));
                break;

        }
    }

    public void removeAllMessages() {
        if(elfView.getVisibility() != VISIBLE)elfView.setVisibility(VISIBLE);
        removeMessages(FALL_TO_THE_GROUND);
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
        removeMessages(FALL_TO_GROUND_STAND);
        removeMessages(LEFT_STAND);
        removeMessages(RIGHT_STAND);
        removeMessages(HUG_END);
        removeMessages(L_SENSOR);
        removeMessages(BOOM_CLOSE_WINDOW);
        removeMessages(FOLD);
        speechView.setVisibility(View.GONE);
    }

    private void sleep() {
        int random = new Random().nextInt(sleepSize);
        stateCount = this.sleepStateCounts.get(random);
        stateIndex = 0;
        elfBody.setImageDrawable(this.stayAnimations.get(direction).get(random));
        elfBody.setImageLevel(0);
//        getMaxWidth();
        sendEmptyMessage(SLEEP);
    }

    public void hugEnd(){
        if(BEFORE_MODE != TIMER_START)BEFORE_MODE = TIMER_START;
        if(CURRENT_ACTION != HUG_END)CURRENT_ACTION = HUG_END;
        direction = direction.equals("left") ? "right" : "left";
        hugPet = null;
        aiXinContainer = null;
        aiXinContainerParams = null;
        if(MyService.isLSensor && lSensorCdl != null)lSensorCdl.countDown();
        lSensorCdl = null;
        int random = new Random().nextInt(hugEndSize);
        stateCount = this.hugEndStateCounts.get(random);
        stateIndex = 0;
        if(elfView.getVisibility() != VISIBLE)elfView.setVisibility(VISIBLE);
        elfBody.setImageDrawable(this.hugEndAnimations.get(direction).get(random));
        elfBody.setImageLevel(0);
//        int currentWidth = getCurrentWidth();
//        params.x = direction.equals("left") ? params.x - currentWidth/2 : params.x + currentWidth/2;
//        MyService.wm.updateViewLayout(elfView, params);
        sendEmptyMessageDelayed(HUG_END, SpeedUtils.getCurrentSpeedTime());
    }

    private void fly() {
        elfBody.setImageDrawable(direction.equals("left") ? flyLeftGifDrawable : flyRightGifDrawable);
        elfBody.setImageLevel(0);
//        getMaxWidth();
    }

    private void jump() {
        elfBody.setImageDrawable(direction.equals("left") ? jumpLeftGifDrawable : jumpRightGifDrawable);
        elfBody.setImageLevel(0);
        //getCurrentWidth();
    }

    private void walkToLeft() {
        direction = "left";
        stateIndex = 0;
        if(BEFORE_MODE == TIMER_START){
            int random = new Random().nextInt(runSize);
            stateCount = this.runStateCounts.get(random);
            LevelListDrawable img = (LevelListDrawable)(runAnimations.get("left").get(random));
            elfBody.setImageDrawable(img);
        }
        else{
            stateCount = 2;
            elfBody.setImageDrawable(climbTopLeftGifDrawable);
        }
        elfBody.setImageLevel(0);
//        getMaxWidth();
        sendEmptyMessage(RUN_LEFT);

    }

    private void walkToRight() {
        direction = "right";
        stateIndex = 0;
        if(BEFORE_MODE == TIMER_START){
            int random = new Random().nextInt(runSize);
            stateCount = this.runStateCounts.get(random);
            elfBody.setImageDrawable(runAnimations.get("right").get(random));
        }
        else{
            stateCount = 2;
            elfBody.setImageDrawable(climbTopRightGifDrawable);
        }
        elfBody.setImageLevel(0);
//        getMaxWidth();
        sendEmptyMessage(RUN_RIGHT);
    }

    private void groundStandLeft(){
        direction = "left";
        if (CURRENT_ACTION != LEFT_STAND) CURRENT_ACTION = LEFT_STAND;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_START ? standLeftGifDrawable : climbTopLeftStandGifDrawable);
        elfBody.setImageLevel(0);
    }

    private void groundStandRight(){
        direction = "right";
        if (CURRENT_ACTION != RIGHT_STAND) CURRENT_ACTION = RIGHT_STAND;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_START ? standRightGifDrawable : climbTopRightStandGifDrawable);
        elfBody.setImageLevel(0);
    }

    private void standLeft() {
        groundStandLeft();
//        getMaxWidth();
    }

    private void standRight() {
        groundStandRight();
//        getMaxWidth();
    }

    public void climbToUp() {
        stateCount = 2;
        stateIndex = 0;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_LEFT_START ? climbLeftGifDrawable :
                climbRightGifDrawable);
        elfBody.setImageLevel(0);
//        if(direction.equals("left")){
//            if(params.x != -MyService.size.x / 2 + params.width / 2 - whDif / 2){
//                params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2;
//                MyService.wm.updateViewLayout(elfView, params);
//            }
//        }else {
//            if(params.x != MyService.size.x / 2 - params.width / 2 + whDif / 2){
//                params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2;
//                MyService.wm.updateViewLayout(elfView, params);
//            }
//        }

//        int currentWidth = getCurrentWidth();
//        if(BEFORE_MODE == TIMER_LEFT_START && params.x != -MyService.size.x / 2 + currentWidth / 2){
//            params.x = -MyService.size.x / 2 + currentWidth / 2;
//            MyService.wm.updateViewLayout(elfView, params);
//        }else if(BEFORE_MODE == TIMER_RIGHT_START && params.x != MyService.size.x / 2 - currentWidth / 2){
//            params.x = MyService.size.x / 2 - currentWidth / 2;
//            MyService.wm.updateViewLayout(elfView, params);
//        }

        sendEmptyMessage(CLIMB_UP);

    }

    private void climbToDown()
    {
        stateCount = 2;
        stateIndex = 0;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_LEFT_START ? climbLeftGifDrawable :
                climbRightGifDrawable);
        elfBody.setImageLevel(0);

//        if(direction.equals("left")){
//            if(params.x != -MyService.size.x / 2 + params.width / 2 - whDif / 2){
//                params.x = -MyService.size.x / 2 + params.width / 2 - whDif / 2;
//                MyService.wm.updateViewLayout(elfView, params);
//            }
//        }else {
//            if(params.x != MyService.size.x / 2 - params.width / 2 + whDif / 2){
//                params.x = MyService.size.x / 2 - params.width / 2 + whDif / 2;
//                MyService.wm.updateViewLayout(elfView, params);
//            }
//        }

//        int currentWidth = getCurrentWidth();
//        if(BEFORE_MODE == TIMER_LEFT_START && params.x != -MyService.size.x / 2 + currentWidth / 2){
//            params.x = -MyService.size.x / 2 + currentWidth / 2;
//            MyService.wm.updateViewLayout(elfView, params);
//        }else if(BEFORE_MODE == TIMER_RIGHT_START && params.x != MyService.size.x / 2 - currentWidth / 2){
//            params.x = MyService.size.x / 2 - currentWidth / 2;
//            MyService.wm.updateViewLayout(elfView, params);
//        }

        sendEmptyMessage(CLIMB_DOWN);
    }

    private void climbStand() {
        if (CURRENT_ACTION != CLIMB_STAND) CURRENT_ACTION = CLIMB_STAND;
        elfBody.setImageDrawable(BEFORE_MODE == TIMER_LEFT_START ? climbLeftStandGifDrawable : climbRightStandGifDrawable);
        elfBody.setImageLevel(0);
//        int currentWidth = getCurrentWidth();
//        if(BEFORE_MODE == TIMER_LEFT_START && params.x != -MyService.size.x / 2 + currentWidth / 2){
//            params.x = -MyService.size.x / 2 + currentWidth / 2;
//            MyService.wm.updateViewLayout(elfView, params);
//        }else if(BEFORE_MODE == TIMER_RIGHT_START && params.x != MyService.size.x / 2 - currentWidth / 2){
//            params.x = MyService.size.x / 2 - currentWidth / 2;
//            MyService.wm.updateViewLayout(elfView, params);
//        }
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

        if(MyService.downContainerView.getVisibility() != VISIBLE)MyService.downContainerView.setVisibility(VISIBLE);
        int randomCount = new Random().nextInt(50) + 180;
        int randomIndex;
        int i = 0;
        CountDownLatch cdl = new CountDownLatch(randomCount);
        MyService.downList.add(cdl);
        while (i < randomCount) {
            randomIndex = new Random().nextInt(this.callTexts.size());
            CallMsg cm = new CallMsg(ctx, this.callTexts.get(randomIndex), cdl, name);
            MyService.downContainerView.addView(cm.callView, cm.callParams);
            i++;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
                    MyService.downList.remove(cdl);
                    if(MyService.downList.size() <= 0){
                        if(MyService.myService.serviceMessenger != null){
                            Message msg = new Message();
                            msg.what = MyService.HIDDEN_CONTAINER;
                            try {
                                MyService.myService.serviceMessenger.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void propFunction() {

        if(MyService.downContainerView.getVisibility() != VISIBLE)MyService.downContainerView.setVisibility(VISIBLE);
        int randomCount = new Random().nextInt(50) + 180;
        int randomIndex;
        int i = 0;
        CountDownLatch cdl = new CountDownLatch(randomCount);
        MyService.downList.add(cdl);
        while (i < randomCount) {
            randomIndex = new Random().nextInt(this.propList.size());
            PropMsg pm = new PropMsg(ctx, this.propList.get(randomIndex), cdl, -1);
            MyService.downContainerView.addView(pm.propView, pm.propParams);
            i++;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
                    MyService.downList.remove(cdl);
                    if(MyService.downList.size() <= 0){
                        if(MyService.myService.serviceMessenger != null){
                            Message msg = new Message();
                            msg.what = MyService.HIDDEN_CONTAINER;
                            try {
                                MyService.myService.serviceMessenger.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void voiceFunction() {
        if (this.voiceIds == null || voiceIds.isEmpty()) return;
        int count = 0;
        while(count++ < 10 && !Utils.voice(ctx, name + "/voice/" + this.voiceIds.get(new Random().nextInt(this.voiceIds.size())) + mscExt));
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

            }
        });
        functionPanelCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmptyMessage(CALL);
                hideFuncPanel();
            }
        });
        functionPanelPropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmptyMessage(PROP);
                hideFuncPanel();

            }
        });
        functionPanelVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmptyMessage(VOICE);
                hideFuncPanel();

            }
        });


        functionPanelParams.width = (int)(200 * ctx.getResources().getDisplayMetrics().density + 0.5f);
        functionPanelParams.height = (int)(200 * ctx.getResources().getDisplayMetrics().density + 0.5f);
        switch (BEFORE_MODE) {
            case TIMER_START:
            case FLY:
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


    private void initSpeechList() {
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

    private void initStateRes(){
        if(sleepStateCounts == null)this.sleepStateCounts = new LinkedList<>();
        if(runStateCounts == null)this.runStateCounts = new LinkedList<>();
        if(hugEndStateCounts == null)this.hugEndStateCounts = new LinkedList<>();

        LevelListDrawable levelListDrawable = null;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_LEFT + imageExt));
        this.moveLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx,name + "/action/" + MOVE_RIGHT + imageExt));
        this.moveRightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_LEFT_LIGHT + imageExt));
        this.moveLeftLightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_RIGHT_LIGHT + imageExt));
        this.moveRightLightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_LEFT_MIDDLE + imageExt));
        this.moveLeftMiddleGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_RIGHT_MIDDLE + imageExt));
        this.moveRightMiddleGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_LEFT_WEIGHT + imageExt));
        this.moveLeftWeightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + MOVE_RIGHT_WEIGHT + imageExt));
        this.moveRightWeightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + LEFT_CLIMB + "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + LEFT_CLIMB + "2" + imageExt));
        this.climbLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + RIGHT_CLIMB + "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + RIGHT_CLIMB + "2" + imageExt));
        this.climbRightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + TOP_LEFT_CLIMB + "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + TOP_LEFT_CLIMB + "2" + imageExt));
        this.climbTopLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + TOP_RIGHT_CLIMB + "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + TOP_RIGHT_CLIMB + "2" + imageExt));
        this.climbTopRightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + CLIMB_LEFT_STAND + imageExt));
        this.climbLeftStandGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + CLIMB_RIGHT_STAND + imageExt));
        this.climbRightStandGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + CLIMB_TOP_LEFT_STAND + imageExt));
        this.climbTopLeftStandGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + CLIMB_TOP_RIGHT_STAND + imageExt));
        this.climbTopRightStandGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + FLY_LEFT + imageExt));
        this.flyLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + FLY_RIGHT + imageExt));
        this.flyRightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + JUMP_LEFT + imageExt));
        this.jumpLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + JUMP_RIGHT + imageExt));
        this.jumpRightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + STAND_LEFT + imageExt));
        this.standLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + STAND_RIGHT + imageExt));
        this.standRightGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + FALL_TO_GROUND_LEFT + "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + FALL_TO_GROUND_LEFT + "2" + imageExt));
        levelListDrawable.addLevel(2, 2, Utils.assets2Drawable(ctx, name + "/action/" + FALL_TO_GROUND_LEFT + "3" + imageExt));
        this.fallToGroundLeftGifDrawable = levelListDrawable;

        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + FALL_TO_GROUND_RIGHT + "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + FALL_TO_GROUND_RIGHT + "2" + imageExt));
        levelListDrawable.addLevel(2, 2, Utils.assets2Drawable(ctx, name + "/action/" + FALL_TO_GROUND_RIGHT + "3" + imageExt));
        this.fallToGroundRightGifDrawable = levelListDrawable;

        if (this.runAnimations == null) this.runAnimations = new HashMap<>();
        List<Drawable> leftRuns = new LinkedList<>();
        List<Drawable> rightRuns = new LinkedList<>();
        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + WALK_LEFT+ "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + WALK_LEFT + "2" + imageExt));
        leftRuns.add(levelListDrawable);
        levelListDrawable = new LevelListDrawable();
        levelListDrawable.addLevel(0, 0, Utils.assets2Drawable(ctx, name + "/action/" + WALK_RIGHT+ "1" + imageExt));
        levelListDrawable.addLevel(1, 1, Utils.assets2Drawable(ctx, name + "/action/" + WALK_RIGHT + "2" + imageExt));
        rightRuns.add(levelListDrawable);
        runStateCounts.add(2);
        runSize = 1;
        int resId2 = ctx.getResources().getIdentifier(name + "_runs", "array", ctx.getPackageName());
        if (resId2 != 0) {
            String[] runGifDrawableStrs = ctx.getResources().getStringArray(resId2);
            runSize = runGifDrawableStrs.length;
            String[] runGifDrawableStrInfo;
            for (int k = 0; k < runSize; k++) {
                runGifDrawableStrInfo = runGifDrawableStrs[k].split(":");
                levelListDrawable = new LevelListDrawable();
                for(int y = 0; y < Integer.valueOf(runGifDrawableStrInfo[1]); y++)
                    levelListDrawable.addLevel(y, y, Utils.assets2Drawable(ctx, name + "/action/" + runGifDrawableStrInfo[0] + "_left" + Integer.valueOf(y+1) + imageExt));
                leftRuns.add(levelListDrawable);
                levelListDrawable = new LevelListDrawable();
                for(int y = 0; y < Integer.valueOf(runGifDrawableStrInfo[1]); y++)
                    levelListDrawable.addLevel(y, y, Utils.assets2Drawable(ctx, name + "/action/" + runGifDrawableStrInfo[0] + "_right" + Integer.valueOf(y+1) + imageExt));
                rightRuns.add(levelListDrawable);
                runStateCounts.add(Integer.valueOf(runGifDrawableStrInfo[1]));
            }
            runSize++;
        }
        this.runAnimations.put("left", leftRuns);
        this.runAnimations.put("right", rightRuns);


        int resId = ctx.getResources().getIdentifier(name + "_sleeps", "array", ctx.getPackageName());
        List<Drawable> leftSleeps = new LinkedList<>();
        List<Drawable> rightSleeps = new LinkedList<>();
        if (this.stayAnimations == null) this.stayAnimations = new HashMap<>();
        if(resId != 0){
            String[] sleepGifDrawableStrs = ctx.getResources().getStringArray(resId);
            String[] sleepGifDrawableStrInfo;
            sleepSize = sleepGifDrawableStrs.length;
            for (int k = 0; k < sleepSize; k++) {
                sleepGifDrawableStrInfo = sleepGifDrawableStrs[k].split(":");
                levelListDrawable = new LevelListDrawable();
                for(int y = 0; y < Integer.valueOf(sleepGifDrawableStrInfo[1]); y++)
                    levelListDrawable.addLevel(y, y, Utils.assets2Drawable(ctx, name + "/action/" + sleepGifDrawableStrInfo[0] + "_left" + Integer.valueOf(y+1) + imageExt));
                leftSleeps.add(levelListDrawable);
                levelListDrawable = new LevelListDrawable();
                for(int y = 0; y < Integer.valueOf(sleepGifDrawableStrInfo[1]); y++)
                    levelListDrawable.addLevel(y, y, Utils.assets2Drawable(ctx, name + "/action/" + sleepGifDrawableStrInfo[0] + "_right" + Integer.valueOf(y+1) + imageExt));
                rightSleeps.add(levelListDrawable);
                this.sleepStateCounts.add(Integer.valueOf(sleepGifDrawableStrInfo[1]));
            }
        }

        this.stayAnimations.put("left", leftSleeps);
        this.stayAnimations.put("right", rightSleeps);


        if (this.hugEndAnimations == null) this.hugEndAnimations = new HashMap<>();
        int resId3 = ctx.getResources().getIdentifier(name + "_hug_ends", "array", ctx.getPackageName());
        if (resId3 != 0) {
            List<Drawable> leftHugEnds = new LinkedList<>();
            List<Drawable> rightHugEnds = new LinkedList<>();
            String[] hugEndGifDrawableStrs = ctx.getResources().getStringArray(resId3);
            hugEndSize = hugEndGifDrawableStrs.length;
            String[] hugEndGifDrawableStrInfo;
            for (int k = 0; k < hugEndSize; k++) {
                hugEndGifDrawableStrInfo = hugEndGifDrawableStrs[k].split(":");
                levelListDrawable = new LevelListDrawable();
                for(int y = 0; y < Integer.valueOf(hugEndGifDrawableStrInfo[1]); y++)
                    levelListDrawable.addLevel(y, y, Utils.assets2Drawable(ctx, name + "/action/" + hugEndGifDrawableStrInfo[0] + "_left" + Integer.valueOf(y+1) + imageExt));
                leftHugEnds.add(levelListDrawable);
                levelListDrawable = new LevelListDrawable();
                for(int y = 0; y < Integer.valueOf(hugEndGifDrawableStrInfo[1]); y++)
                    levelListDrawable.addLevel(y, y, Utils.assets2Drawable(ctx, name + "/action/" + hugEndGifDrawableStrInfo[0] + "_right" + Integer.valueOf(y+1) + imageExt));
                rightHugEnds.add(levelListDrawable);
                hugEndStateCounts.add(Integer.valueOf(hugEndGifDrawableStrInfo[1]));
            }
            this.hugEndAnimations.put("left", leftHugEnds);
            this.hugEndAnimations.put("right", rightHugEnds);
        }


    }

    private void changeStateLevel(){
        if(stateIndex + 1 > stateCount)stateIndex = 0;
        elfBody.setImageLevel(stateIndex++);

    }


    public void hideFuncPanel(){
        if(this.functionPanelView != null){
            MyService.wm.removeView(this.functionPanelView);
            this.functionPanelView = null;
        }
        if(!(MyService.isLSensor && hugPet != null && SensorUtils.isInCouple(this)))sendEmptyMessage(BEFORE_MODE);
    }

    private void chooseDownPet(MotionEvent event){
        if(MyService.pets == null){
            MyService.pets = new LinkedBlockingQueue<>();
            Iterator<Map.Entry<String, List<Pet>>> it = MyService.groupPets.entrySet().iterator();
            List<Pet> pets = new LinkedList<>();
            while (it.hasNext()){
                Map.Entry<String, List<Pet>> entry = it.next();
                for (Pet pet : entry.getValue()){
                    pets.add(pet);
                }
            }
            Collections.sort(pets);
            MyService.pets.addAll(pets);
        }else if(MyService.pets.isEmpty()){
            MyService.pets = null;
            MyService.downPet = null;
            if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty()){
                for(Pet pet : MyService.choosedPets){
                    pet.params.flags = Utils.getNormalFlags() | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    MyService.wm.updateViewLayout(pet.elfView, pet.params);
                }
            }
            return;
        }

        int rawX = (int)(event.getRawX());
        int rawY = (int)(event.getRawY());
        int deltaX, deltaY;

        Pet currentPet;
        int count = MyService.pets.size();

        for(int i = 0; i < count; i++){
            currentPet = MyService.pets.poll();
            if(currentPet == this){
                if(MyService.choosedPets == null)MyService.choosedPets = new LinkedList<>();
                MyService.choosedPets.add(currentPet);
                continue;
            }
            deltaX = rawX - (currentPet.params.x - -MyService.size.x/2 - currentPet.params.width/2);
            deltaY = rawY - (currentPet.params.y - -MyService.size.y/2 - currentPet.params.height/2);
            if(deltaX < 0 || deltaX > currentPet.params.width || deltaY < 0 || deltaY > currentPet.params.height)continue;
            event.setLocation(deltaX, deltaY);
            MyService.downPet = currentPet;
            if(MyService.choosedPets == null)MyService.choosedPets = new LinkedList<>();
            MyService.choosedPets.add(currentPet);
            return;
        }

        MyService.pets = null;
        MyService.downPet = null;
        if(MyService.choosedPets != null && !MyService.choosedPets.isEmpty()){
            for(Pet pet : MyService.choosedPets){
                pet.params.flags = Utils.getNormalFlags() | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                MyService.wm.updateViewLayout(pet.elfView, pet.params);
            }
        }

    }

    public int getCurrentWidth(){
        int currentWidth = (int)(params.height * (elfBody.getDrawable().getCurrent().getIntrinsicWidth() * 1.0 / elfBody.getDrawable().getCurrent().getIntrinsicHeight()));
        if(params.width != currentWidth){
            params.width = currentWidth;
            MyService.wm.updateViewLayout(elfView, params);
        }
        return currentWidth;
    }

    public int getMaxWidth(){
        int petW = (int) (MyService.size.x * (MyService.currentSize / 100.0));
        int maxWidth = whRate != 0 && whRate != 1 ? (int)(petW * whRate) : petW;
        if(params.width != maxWidth){
            params.width = maxWidth;
            MyService.wm.updateViewLayout(elfView, params);
        }
        return maxWidth;
    }

    public void twoWayRunnig(int lheight, int moveXDirection, int moveYDirection, CountDownLatch cdl){
        lSensorCdl = cdl;
        Message msg1 = new Message();
        Map<String, Long> data = new HashMap<>();
        data.put("moveXDirection", (long)moveXDirection);
        data.put("moveYDirection", (long)moveYDirection);
        data.put("lheight", (long)lheight);
        msg1.what = Pet.L_SENSOR;
        msg1.obj = data;
        sendMessage(msg1);
    }

    private void dispatchEvent(MotionEvent event){
        if(MyService.downPet != null)MyService.downPet.elfView.dispatchTouchEvent(event);

    }


    private void pp(int startX, int startY){
        Path mPath = new Path();
        mPath.moveTo(startX, startY);//代表从哪个点开始滑动
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(mPath, 0, 1)).build();
            MyService2.as.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                @Override

                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.i("++++++++++++++++++++++","success");

                }

                @Override

                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.i("++++++++++++++++++++++","fail");

                }

            }, null);

        }
    }

    private void gg(int startX, int startY, int endX, int endY){
        Path mPath = new Path();
        mPath.moveTo(startX, startY);//代表从哪个点开始滑动
        mPath.lineTo(endX, endY);//滑动到哪个点
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(mPath, 0, 1)).build();
            MyService2.as.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                @Override

                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.i("++++++++++++++++++++++","success");

                }

                @Override

                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.i("++++++++++++++++++++++","fail");

                }

            }, null);

        }
    }

    private void boomCloseWindow(){
        if(CURRENT_ACTION != SLEEP)CURRENT_ACTION = SLEEP;
        stateCount = 4;
        stateIndex = 0;
        elfBody.setImageDrawable(this.stayAnimations.get(direction).get(0));
        elfBody.setImageLevel(0);
        sendEmptyMessage(BOOM_CLOSE_WINDOW);
    }

    private void foldWindow(){
        if(CURRENT_ACTION != FOLD)CURRENT_ACTION = FOLD;
        else return;
        elfBody.setImageDrawable(hugEndAnimations.get(direction).get(0));
        elfBody.setImageLevel(0);
        stateCount = 3;
        stateIndex = 0;
        if(!CloseWindowUtils.foldPets.contains(this))CloseWindowUtils.foldPets.add(this);
        if(CloseWindowUtils.isClosing)return;
        CloseWindowUtils.foldFlag = 1;
        CloseWindowUtils.start(ctx, CloseWindowUtils.FOLD, null);
        sendEmptyMessage(FOLD);
    }

    @Override
    public int compareTo(Pet pet) {
        return (int)(pet.id - id);
    }
}

