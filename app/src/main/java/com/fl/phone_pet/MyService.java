package com.fl.phone_pet;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
//import com.fl.phone_pet.handler.CollisionHandler;
import com.fl.phone_pet.handler.CollisionHandler;
import com.fl.phone_pet.pojo.Pet;
import com.fl.phone_pet.utils.SensorUtils;
import com.fl.phone_pet.utils.SpeedUtils;
import com.fl.phone_pet.utils.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MyService extends Service {

    public static final String AX = "ax";
    public static final String LW = "lw";
    public static final String WZ = "wz";
    public static final String OSS_BASE = "https://music-fl-wdl.oss-cn-shenzhen.aliyuncs.com/";
    public static Map<String, List<Pet>> groupPets;
    public static Queue<Pet> pets;
    public static Pet downPet;
    public static List<Pet> choosedPets;
    public static int orientation;
    public static long currentMaxPetId;
    Messenger serviceMessenger = new Messenger(new ActivityMsgHandler());
    Messenger activityMessenger;
    Handler handler = new Handler();
    public static WindowManager wm;
    public static Point size;
    public static Map<Integer, MediaPlayer> mp;
    CollisionHandler collisionHandler;
    public static int currentSize = 26;
    public static int speed = 9;
    public static int frequest = 3;
    public static int divisionArg = -1;
    public static int oldStatusBarHeight = 0;
    public static int statusBarHeight = 0;
    public static boolean isEnableTouch = true;
    public static boolean isKeyboardShow = false;
    public static boolean isVibrator = true;
    public static boolean isGSensorEnabled = false;
    public static boolean isLSensorEnabled = false;
    public static boolean isLSensor = false;

    public static volatile RelativeLayout downContainerView;
    public static volatile CopyOnWriteArrayList<CountDownLatch> downList = new CopyOnWriteArrayList<>();


    private class ActivityMsgHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == MainActivity.SIZE_CHANGE){
                currentSize = msg.arg1;
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                updateSize();
            }else if(msg.what == MainActivity.ADD_PET){
                addPetOneCount((String)msg.obj);
            }else if(msg.what == MainActivity.REDUCE_PET){
                reducePetOneCount((String)msg.obj);
            }else if(msg.what == MainActivity.SPEED_CHANGE){
                speed = msg.arg1;
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                updateSpeed();
            }else if(msg.what == MainActivity.FREQUEST_CHANGE){
                frequest = msg.arg1;
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                updateFrequest();
            }else if(msg.what == MainActivity.STATUS_BAR_CHANGE){
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                checkStatusBarChange();
            }else if(msg.what == MainActivity.TOUCH_CHANGE){
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                changeIsEnableTouch();
            }else if(msg.what == MainActivity.KETBOARD_CHANGE){
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                changeIsEnableKeyboardShow();
            }else if(msg.what == MainActivity.CLOSE_GSENSOR){
                if(activityMessenger == null)activityMessenger = msg.replyTo;
                closeGSensor();
            }

        }
    }

    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        activityMessenger = intent.getParcelableExtra("clientMessenger");
        return serviceMessenger.getBinder();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm == null)return;
        getSharedPreferences("pet_store", Context.MODE_PRIVATE).edit()
                .putInt("current_size", currentSize)
                .putInt("speed", speed)
                .putInt("frequest", frequest)
                .putBoolean("check_status_bar", statusBarHeight == 0 ? true : false)
                .putBoolean("is_enable_touch", isEnableTouch)
                .putBoolean("is_show_keyboard", isKeyboardShow)
                .putBoolean("is_vibrator", isVibrator)
                .putBoolean("is_gsensor", isGSensorEnabled)
                .putBoolean("is_lsensor", isLSensorEnabled)
                .commit();

        try {

            collisionHandler.destoryRes();
            if(isGSensorEnabled)SensorUtils.unregisterGSensor();
            if(isLSensorEnabled)SensorUtils.unregisterLSensor();


            Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, List<Pet>> entry = it.next();
                for (Pet pet : entry.getValue()){
                    pet.removeAllMessages();
                    if(this.mp != null && this.mp.get(1) != null){
                        this.mp.get(1).stop();
                        this.mp.get(1).release();
                        mp.put(1, null);
                    }
                    wm.removeView(pet.elfView);
                    wm.removeView(pet.speechView);
                    if(pet.functionPanelView != null)pet.hideFuncPanel();
                }
            }
            if(downContainerView != null && downContainerView.getVisibility() == View.VISIBLE){
                for (CountDownLatch cdl : this.downList){
                    int countSize = (int)cdl.getCount();
                    for (int count = 0; count < countSize; count++)cdl.countDown();
                }
                wm.removeView(downContainerView);
            }

            wm = null;
            Message msg = new Message();
            msg.what = MainActivity.DISCONNECTION;
            activityMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentSize = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getInt("current_size", currentSize);
        speed = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getInt("speed", speed);
        frequest = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getInt("frequest", frequest);
        boolean checkStatusBar = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getBoolean("check_status_bar", false);
        isKeyboardShow = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getBoolean("is_show_keyboard", isKeyboardShow);
        isEnableTouch = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getBoolean("is_enable_touch", isEnableTouch);
        isVibrator = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getBoolean("is_vibrator", isVibrator);
        isGSensorEnabled = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getBoolean("is_gsensor", isGSensorEnabled);
        isLSensorEnabled = getSharedPreferences("pet_store", Context.MODE_PRIVATE).getBoolean("is_lsensor", isLSensorEnabled);
        if(isGSensorEnabled)SensorUtils.registerGSensor(this);
        if(isLSensorEnabled)SensorUtils.registerLSensor(this);

        if(checkStatusBar){
            oldStatusBarHeight = 0;
            statusBarHeight = 0;
        }else{
            oldStatusBarHeight = Utils.getStatusBarHeight(this);
            statusBarHeight = Utils.getStatusBarHeight(this);
        }


        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        if(mp == null)mp = new HashMap<>();
        size = new Point();
        if (Build.VERSION.SDK_INT >= 19){
            wm.getDefaultDisplay().getRealSize(size);
        }else {
            wm.getDefaultDisplay().getSize(size);
        }

        divisionArg = Utils.getWindowDivisionArg(size.y);


        initDownContainer();
        initPets();
        initCollisionHandler();
        goPets();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientation = newConfig.orientation;
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            int temp;

            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
                if(size.x < size.y){
                    temp = size.x;
                    size.x = size.y;
                    size.y = temp;
                }
                statusBarHeight = 0;
            }else{
                if(size.x > size.y){
                    temp = size.x;
                    size.x = size.y;
                    size.y = temp;
                }
                statusBarHeight = oldStatusBarHeight;
            }

            collisionHandler.destoryRes();
            if(isLSensorEnabled)SensorUtils.unregisterLSensor();

            initDownContainer();

            Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, List<Pet>> entry = it.next();
                for (Pet pet : entry.getValue()){
                    pet.removeAllMessages();
                    pet.isOnceFly = true;
                    pet.hugPet = null;
                    pet.elfView.setVisibility(View.GONE);
                    pet.speechView.setVisibility(View.GONE);
                    if(pet.functionPanelView != null)pet.hideFuncPanel();
                    pet.params.x = 0;
                    pet.params.y = -size.y/2 + pet.params.height/2 + 5 + MyService.statusBarHeight;
                    wm.updateViewLayout(pet.elfView, pet.params);
                }
            }
            if(downContainerView != null && downContainerView.getVisibility() == View.VISIBLE){
                for (CountDownLatch cdl : this.downList) {
                    int countSize = (int)cdl.getCount();
                    for (int count = 0; count < countSize; count++)cdl.countDown();
                }
            }

            collisionHandler.start();
            goPets();
            if(isLSensorEnabled)SensorUtils.registerLSensor(this);

        }

    }

    private void initDownContainer(){
        RelativeLayout downContainerView = null;
        if(this.downContainerView == null)downContainerView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.container, null);
        WindowManager.LayoutParams downContainerParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//6.0
            downContainerParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            downContainerParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        downContainerParams.format = PixelFormat.RGBA_8888;
        downContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        downContainerParams.width = this.size.x;
        downContainerParams.height = this.size.y;
        downContainerParams.x = 0;
        downContainerParams.y = 0;
        if(this.downContainerView == null){
            downContainerView.setVisibility(View.GONE);
            wm.addView(downContainerView, downContainerParams);
            this.downContainerView = downContainerView;
        }else{
            wm.updateViewLayout(this.downContainerView, downContainerParams);
        }
    }

    private void initPets(){
        if(groupPets == null)groupPets = new HashMap<>();

        Pet axPet = new Pet(this, ++currentMaxPetId, AX);
        groupPets.put(AX, new LinkedList<>(Arrays.asList(axPet)));

        Pet lwPet = new Pet(this, ++currentMaxPetId, LW);
        groupPets.put(LW, new LinkedList<>(Arrays.asList(lwPet)));

        Pet wzPet = new Pet(this, ++currentMaxPetId, WZ);
        groupPets.put(WZ, new LinkedList<>(Arrays.asList(wzPet)));

    }

    private void addPetOneCount(String name){
        if(wm == null || groupPets == null || collisionHandler == null)return;
        Pet pet = new Pet(this, ++currentMaxPetId, name);
        List<Pet> pets = groupPets.get(name);
        if(pets == null){
            pets = new LinkedList<>();
            groupPets.put(name, pets);
        }
        pets.add(pet);
        collisionHandler.addPet(pet);
        pet.go();
    }

    private void reducePetOneCount(String name){
        if(wm == null || groupPets == null || collisionHandler == null)return;
        List<Pet> pets = groupPets.get(name);
        if(pets == null || pets.isEmpty())return;
        int random = new Random().nextInt(pets.size());
        Pet pet = pets.get(random);
        pet.removeAllMessages();
        wm.removeView(pet.elfView);
        wm.removeView(pet.speechView);
        pets.remove(pet);
        collisionHandler.removePet(pet);
        if(pets == null || pets.isEmpty()){
            Message msg = new Message();
            msg.what = MainActivity.BUTTON_DISENABLED;
            msg.obj = name;
            try{
                activityMessenger.send(msg);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void goPets(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        int i = 0;
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(wm != null && groupPets != null && !groupPets.isEmpty()){
                            List<Pet> myPets = new LinkedList<>();
                            Iterator<Map.Entry<String, List<Pet>>> it1 = groupPets.entrySet().iterator();
                            while (it1.hasNext())myPets.addAll(it1.next().getValue());
                            if(myPets.contains(pet))pet.go();
                        }
                    }
                }, i * 400);
                i++;
            }
        }

    }

    private void initCollisionHandler(){
        if(collisionHandler == null)collisionHandler = new CollisionHandler(this, groupPets);
        collisionHandler.start();
    }

    private void updateSize(){
        int petW = (int) (size.x * (currentSize / 100.0));
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        collisionHandler.destoryRes();
        if(isLSensorEnabled)SensorUtils.unregisterLSensor();

        if(downContainerView != null && downContainerView.getVisibility() == View.VISIBLE){
            for (CountDownLatch cdl : this.downList){
                int countSize = (int)cdl.getCount();
                for (int count = 0; count < countSize; count++)cdl.countDown();
            }
        }

        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                pet.removeAllMessages();
                pet.elfView.setVisibility(View.GONE);
                pet.speechView.setVisibility(View.GONE);
                if(pet.functionPanelView != null)pet.hideFuncPanel();
                pet.params.width =  pet.whRate != 0 && pet.whRate != 1 ? (int)(petW * pet.whRate) : petW;
                pet.params.height = petW;
                pet.whDif = pet.params.width - pet.params.height;
                pet.isOnceFly = true;
                pet.hugPet = null;
                pet.speechParams.height = (int) (currentSize * 7.5);
                pet.speechBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, currentSize / 2);
                pet.params.x = 0;
                pet.params.y = -size.y/2 + petW/2 + 20 + MyService.statusBarHeight;
                wm.updateViewLayout(pet.elfView, pet.params);
                wm.updateViewLayout(pet.speechView, pet.speechParams);
            }
        }
        collisionHandler.start();
        goPets();

        if(isLSensorEnabled)SensorUtils.registerLSensor(this);
    }

    private void updateFrequest(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                if(pet.BEFORE_MODE != Pet.FLY && pet.CURRENT_ACTION != Pet.HUG && pet.CURRENT_ACTION != Pet.COLLISION && pet.CURRENT_ACTION != Pet.SPEECH_START){
                    pet.removeMessages(pet.BEFORE_MODE);
                    pet.sendEmptyMessageDelayed(pet.BEFORE_MODE, frequest);
                }
            }
        }
    }

    private void updateSpeed(){
        SensorUtils.unregisterGSensor();
        SensorUtils.registerGSensor(this);
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                if(pet.CURRENT_ACTION != Pet.COLLISION){
                    pet.removeMessages(pet.CURRENT_ACTION);
                    pet.sendEmptyMessageDelayed(pet.CURRENT_ACTION, speed);
                }

            }
        }
    }

    private void checkStatusBarChange(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                if(pet.BEFORE_MODE == Pet.TIMER_TOP_START){
                    if(pet.params.y >= -size.y/2 + pet.params.height/2 && statusBarHeight == 0){
                        pet.removeAllMessages();
                        pet.params.y = -size.y/2 + pet.params.height/2;
                        wm.updateViewLayout(pet.elfView, pet.params);
                        pet.sendEmptyMessage(pet.BEFORE_MODE);
                    }else  if(pet.params.y <= -size.y/2 + pet.params.height/2 && statusBarHeight != 0){
                        pet.removeAllMessages();
                        pet.params.y = -size.y/2 + Utils.getStatusBarHeight(this) + pet.params.height/2;
                        wm.updateViewLayout(pet.elfView, pet.params);
                        pet.sendEmptyMessage(pet.BEFORE_MODE);
                    }
                }

            }
        }
    }

    private void changeIsEnableTouch(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                if(MyService.isEnableTouch){
                    pet.params.flags = Utils.getNormalFlags();
                }else{
                    pet.params.flags = Utils.getNormalFlags() | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                }
                MyService.wm.updateViewLayout(pet.elfView, pet.params);

            }
        }
    }

    private void changeIsEnableKeyboardShow(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                if(!MyService.isKeyboardShow){
                    pet.params.flags = Utils.getNormalFlags();
                }else{
                    pet.params.flags = Utils.getNormalFlags() | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
                }
                MyService.wm.updateViewLayout(pet.elfView, pet.params);

            }
        }
    }

    private void closeGSensor(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                if(pet.CURRENT_ACTION == Pet.G_SENSOR_X){
                    pet.removeAllMessages();
                    pet.sendEmptyMessage(Pet.FALL_TO_GROUND_STAND);
                    pet.sendEmptyMessageDelayed(Pet.TIMER_START, SpeedUtils.getCurrentFrequestTime());
                }

            }
        }
    }

}