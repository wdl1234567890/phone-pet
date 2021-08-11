package com.fl.phone_pet;

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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import pl.droidsonroids.gif.GifDrawable;

public class MyService extends Service {

    public static final String AX = "ax";
    public static final String LW = "lw";
    public static final String WZ = "wz";
    public static final String OSS_BASE = "https://music-fl-wdl.oss-cn-shenzhen.aliyuncs.com/";
    Map<String, List<Pet>> groupPets;
    Messenger serviceMessenger = new Messenger(new ActivityMsgHandler());
    Messenger activityMessenger;
    Handler handler = new Handler();
    public static WindowManager wm;
    Point size;
    Map<Integer, MediaPlayer> mp;
    CollisionHandler collisionHandler;
    private final static String appId = "e0e71b62";
    View mscView;
    WindowManager.LayoutParams mscParams;
    private final int oldDeviation = 62;
    public static int deviation = 62;


    public static int currentSize = 20;
    public static int speed = 1;

    public volatile RelativeLayout downContainerView;
    private volatile CopyOnWriteArrayList<CountDownLatch> downList = new CopyOnWriteArrayList<>();


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
        getSharedPreferences("pet_store", Context.MODE_PRIVATE).edit()
                .putInt("current_size", currentSize).putInt("speed", speed).commit();
//        getSharedPreferences("pet_store", Context.MODE_PRIVATE).edit()
//                .putInt("speed", speed).commit();
        try {
            Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, List<Pet>> entry = it.next();
                for (Pet pet : entry.getValue()){
                    pet.removeAllMessages();
                    if(pet.mIat != null){
                        pet.mIat.cancel();
                        pet.mIat.destroy();
                    }
                    if(this.mp != null && this.mp.get(1) != null)this.mp.get(1).release();
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

            collisionHandler.destoryRes();

//            wm.removeView(mscView);
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
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if(mp == null)mp = new HashMap<>();
        size = new Point();
        if (Build.VERSION.SDK_INT >= 19){
            wm.getDefaultDisplay().getRealSize(size);
        }else {
            wm.getDefaultDisplay().getSize(size);
        }

        initDownContainer();
//        initMSC();
        initPets();
        initCollisionHandler();
        goPets();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)deviation = 0;
            else deviation = oldDeviation;

            int temp;
            temp = size.x;
            size.x = size.y;
            size.y = temp;

            initDownContainer();

            Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, List<Pet>> entry = it.next();
                for (Pet pet : entry.getValue()){
                    pet.elfView.setVisibility(View.GONE);
                    pet.speechView.setVisibility(View.GONE);
                    if(pet.functionPanelView != null)pet.hideFuncPanel();
                    pet.params.x = 0;
                    pet.params.y = -size.y/2 + pet.params.height/2 + 5;
                    wm.updateViewLayout(pet.elfView, pet.params);
                }
            }
            if(downContainerView != null && downContainerView.getVisibility() == View.VISIBLE){
                for (CountDownLatch cdl : this.downList) {
                    int countSize = (int)cdl.getCount();
                    for (int count = 0; count < countSize; count++)cdl.countDown();
                }
            }
            collisionHandler.destoryRes();
            goPets();

        }

    }

    private void initDownContainer(){
        RelativeLayout downContainerView = null;
        if(this.downContainerView == null)downContainerView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.container, null);
        WindowManager.LayoutParams downContainerParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
            downContainerParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            downContainerParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        downContainerParams.format = PixelFormat.RGBA_8888;
        downContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
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


//    private void initCollisionHandler(){
//        collisionHandler = new CollisionHandler(groupPets);
//        collisionHandler.sendEmptyMessage(0);
//    }

//    private void initMSC(){
//        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + this.appId);
//        if(mscView == null){
//            mscView = LayoutInflater.from(this).inflate(R.layout.ifly_layout_mnotice_image, null);
//            try {
//                mscView.setBackground(new GifDrawable(getAssets(), "msc_bg.gif"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mscView.setVisibility(View.GONE);
//            mscParams = new WindowManager.LayoutParams();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
//                mscParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//            }else {
//                mscParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//            }
//
//            mscParams.format = PixelFormat.RGBA_8888; // 设置图片
//
//            // 格式，效果为背景透明
//            mscParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//
//            int petW = (int) (size.x / 1.5);
//
//            mscParams.width = petW;
//            mscParams.height = petW;
//            mscParams.x = 0;
//            mscParams.y = 0;
//            wm.addView(mscView, mscParams);
//        }
//    }


    private void initPets(){
        if(groupPets == null)groupPets = new HashMap<>();

        Pet axPet = new Pet(this, AX, currentSize, speed, size, mp, mscView, this.downContainerView, this.downList);
        groupPets.put(AX, new LinkedList<>(Arrays.asList(axPet)));

        Pet lwPet = new Pet(this, LW, currentSize, speed, size, mp, mscView, this.downContainerView, this.downList);
        groupPets.put(LW, new LinkedList<>(Arrays.asList(lwPet)));

        Pet wzPet = new Pet(this, WZ, currentSize, speed, size, mp, mscView, this.downContainerView, this.downList);
        groupPets.put(WZ, new LinkedList<>(Arrays.asList(wzPet)));

    }

    private void addPetOneCount(String name){
        Pet pet = new Pet(this, name, currentSize, speed, size, mp, mscView, this.downContainerView, this.downList);
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
                        pet.go();
                    }
                }, i * 400);
                i++;
            }
        }

    }

    private void initCollisionHandler(){
        if(collisionHandler == null)collisionHandler = new CollisionHandler(this, groupPets, size, mp, downContainerView, downList);
        collisionHandler.sendEmptyMessage(CollisionHandler.COLLISION);
    }

    private void updateSize(){
        int petW = (int) (size.x * (currentSize / 100.0));
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        collisionHandler.destoryRes();

        if(downContainerView != null && downContainerView.getVisibility() == View.VISIBLE){
            for (CountDownLatch cdl : this.downList){
                int countSize = (int)cdl.getCount();
                for (int count = 0; count < countSize; count++)cdl.countDown();
            }
        }

        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                pet.elfView.setVisibility(View.GONE);
                pet.speechView.setVisibility(View.GONE);
                if(pet.functionPanelView != null)pet.hideFuncPanel();
                pet.params.width =  pet.whRate != 0 && pet.whRate != 1 ? (int)(petW * pet.whRate) : petW;
                pet.params.height = petW;
                pet.whDif = pet.params.width - pet.params.height;
                pet.pngDeviation = 0;
                pet.speechParams.height = (int) (currentSize * 7.5);
                pet.speechBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, currentSize / 2);
                pet.params.x = 0;
                pet.params.y = -size.y/2 + petW/2 + 20;
                wm.updateViewLayout(pet.elfView, pet.params);
                wm.updateViewLayout(pet.speechView, pet.speechParams);
            }
        }
        goPets();
    }

    public void updateSpeed(){
        Iterator<Map.Entry<String, List<Pet>>> it = groupPets.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<Pet>> entry = it.next();
            for (Pet pet : entry.getValue()){
                pet.updateSpeed(speed);
//                wm.updateViewLayout(pet.elfView, pet.params);
//                wm.updateViewLayout(pet.speechView, pet.speechParams);
            }
        }
    }
}