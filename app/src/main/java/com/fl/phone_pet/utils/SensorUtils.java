package com.fl.phone_pet.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fl.phone_pet.MainActivity;
import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.pojo.Pet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class SensorUtils{
    public static final int SHAKE_COUNT_ZERO = 50001;
    public static final int BOOM = 50002;
    public static final int STEP_COUNT_RATE = 50003;
    public static final int STEP_COUNT_ZERO = 50004;
    public static final int STEP_COUNT_START = 50005;
    private static SensorManager mSensorManager;
    private static Sensor gSensor;
    private static Sensor lSensor;
    private static Sensor sdSensor;
    private static Sensor pSensor;
    private static Sensor dSensor;
    private static GSensorEventListener gSensorEventListener = new GSensorEventListener();
    private static LSensorEventListener lSensorEventListener = new LSensorEventListener();
    private static SDSensorEventListener sdSensorEventListener = new SDSensorEventListener();
    private static PSensorEventListener pSensorEventListener;
    private static DSensorEventListener dSensorEventListener = new DSensorEventListener();
    public static Context ctx1;
    public static List<List<Pet>> couplePets;
    public static int currentShakeCount;
    public static int currentStepCount;
    public static double last;
    public static final int maxStepCount = 3;
    public static boolean motiveState = true;
    public static long time = 1000;

    public static Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHAKE_COUNT_ZERO:
                    currentShakeCount = 0;
                    break;
                case BOOM:
                    View boomView = (View)msg.obj;
                    boomView.setVisibility(View.GONE);
                    if(MyService.wm == null)return;
                    MyService.wm.removeView(boomView);
                    String name = null;
                    switch (new Random().nextInt(3)){
                        case 0:
                            name = MyService.LW;
                            break;
                        case 1:
                            name = MyService.AX;
                            break;
                        case 2:
                            name = MyService.WZ;
                            break;
                    }
                    Message msg2 = new Message();
                    msg2.what = MainActivity.ADD_PET;
                    msg2.obj = name;
                    msg2.arg1 = 10;
                    if(!MainActivity.buttons.get("reduce_" + name).isEnabled())
                        MainActivity.buttons.get("reduce_" + name).setEnabled(true);
                    try {
                        MainActivity.serviceMessenger.send(msg2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case STEP_COUNT_RATE:
                    removeMessages(STEP_COUNT_RATE);
                    Log.i("----count----", String.valueOf(currentStepCount));
                    currentStepCount = (int)(Math.pow(currentStepCount, 2) / 4);
                    if(currentStepCount > 25)currentStepCount = 25;
                    if(currentStepCount < 2)currentStepCount = 2;
                    if(Math.abs(MyService.speed - currentStepCount) > 2){
                        if(MainActivity.serviceMessenger != null){
                            Message msg3 = new Message();
                            msg3.what = MainActivity.SPEED_CHANGE;
                            msg3.arg1 = currentStepCount;
                            try {
                                MainActivity.serviceMessenger.send(msg3);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    currentStepCount = 0;
                    sendEmptyMessageDelayed(STEP_COUNT_RATE, time);
                    break;
                case STEP_COUNT_ZERO:
                    currentStepCount = 0;
                    break;
                case STEP_COUNT_START:
                    registerPSensor();
                    break;

            }
        }
    };


//    public static long lastStepMilliSecond;
//    public static double last;
//    public static boolean motiveState = true;
    public static volatile CountDownLatch cdl;
    public static boolean isDark = false;

    public static boolean isSensorAble(Context ctx, int type){
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        return mSensorManager.getDefaultSensor(type) != null;
    }

    public static void registerGSensor(Context ctx){
        ctx1 = ctx;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        if(gSensor == null)gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(gSensor == null)return;
        mSensorManager.registerListener(gSensorEventListener, gSensor, (int)(SpeedUtils.getCurrentSpeedTime() * 500));
    }

    public static void registerLSensor(Context ctx){
        ctx1 = ctx;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        if(lSensor == null)lSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lSensor == null)return;
        isDark = false;
        mSensorManager.registerListener(lSensorEventListener, lSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static void registerPSensor(Context ctx){
        ctx1 = ctx;
        handler.sendEmptyMessageDelayed(STEP_COUNT_START, 1500);
    }

    private static void registerPSensor(){
        if(MyService.wm == null)return;;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx1.getSystemService(Context.SENSOR_SERVICE);
        if(pSensor == null)pSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(pSensor == null)return;
        pSensorEventListener = new PSensorEventListener();
        MainActivity.speedSetting.setEnabled(false);
        mSensorManager.registerListener(pSensorEventListener, pSensor, SensorManager.SENSOR_DELAY_GAME);
        handler.sendEmptyMessageDelayed(STEP_COUNT_RATE, time);
    }

    public static void registerSDSensor(Context ctx){
        ctx1 = ctx;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        if(sdSensor == null)sdSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sdSensor == null)return;
        mSensorManager.registerListener(sdSensorEventListener, sdSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public static void registerDSensor(Context ctx){
        ctx1 = ctx;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        if(dSensor == null)dSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Log.i("----d-----", String.valueOf(dSensor == null));
        if(dSensor == null)return;
        mSensorManager.registerListener(dSensorEventListener, dSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public static void unregisterGSensor(){
        if(mSensorManager != null){
            mSensorManager.unregisterListener(gSensorEventListener);
        }
    }

    public static void unregisterLSensor(){
        if(mSensorManager != null){
            lSensorEventListener.backState();
            mSensorManager.unregisterListener(lSensorEventListener);
        }
    }

    public static void unregisterSDSensor(){
        if(mSensorManager != null){
            mSensorManager.unregisterListener(sdSensorEventListener);
        }
    }

    public static void unregisterPSensor(){
        if(mSensorManager != null && pSensorEventListener != null){
            mSensorManager.unregisterListener(pSensorEventListener);
            handler.removeMessages(STEP_COUNT_RATE);
            pSensorEventListener = null;
            if(MainActivity.serviceMessenger != null){
                Message msg3 = new Message();
                msg3.what = MainActivity.SPEED_CHANGE;
                msg3.arg1 = MainActivity.speedSetting.getProgress();
                try {
                    MainActivity.serviceMessenger.send(msg3);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            MainActivity.speedSetting.setEnabled(true);
        }
    }

    public static void unregisterDSensor(){
        if(mSensorManager != null){
            mSensorManager.unregisterListener(dSensorEventListener);
            handler.removeMessages(STEP_COUNT_RATE);
        }
    }

    public static class LSensorEventListener implements SensorEventListener {


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];

            Log.i("----light------", String.valueOf(x));
            if(!isDark && x <= 5 && !MyService.isLSensor){
                Log.i("----yes------", "yes");
                //isDark = true;
                MyService.isLSensor = true;
                couple();
                twoWayRunning();
            }else if(x > 5){
                Log.i("----hh------", "hh");
                backState();
                isDark = false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        private void backState(){
            if(MyService.isLSensor && couplePets != null && !couplePets.isEmpty()){
                Iterator<List<Pet>> lists = couplePets.iterator();
                while (lists.hasNext()){
                    List<Pet> list = lists.next();
                    Pet pet = list.get(0);
                    Pet pet1 = list.get(1);
                    if(pet.BEFORE_MODE == Pet.TIMER_START && pet.CURRENT_ACTION != Pet.HUG && pet.CURRENT_ACTION != Pet.MOVE){
                        pet.removeAllMessages();
                        pet.hugPet = null;
                        pet.sendEmptyMessage(Pet.TIMER_START);
                    }else if(pet.BEFORE_MODE == Pet.FLY){
                        pet.removeAllMessages();
                        pet.hugPet = null;
                        pet.sendEmptyMessage(Pet.FALL_TO_THE_GROUND);
                    }

                    if(pet1.BEFORE_MODE == Pet.TIMER_START && pet1.CURRENT_ACTION != Pet.HUG && pet1.CURRENT_ACTION != Pet.MOVE){
                        pet1.removeAllMessages();
                        pet1.hugPet = null;
                        pet1.sendEmptyMessage(Pet.TIMER_START);
                    }else if(pet1.BEFORE_MODE == Pet.FLY){
                        pet1.removeAllMessages();
                        pet1.hugPet = null;
                        pet1.sendEmptyMessage(Pet.FALL_TO_THE_GROUND);
                    }

                }
                couplePets.clear();
            }
            destoryLSensor();
        }

        public static void destoryLSensor(){

            if(MyService.isLSensor && couplePets != null && !couplePets.isEmpty()) {
                Iterator<List<Pet>> lists = couplePets.iterator();
                while (lists.hasNext()) {
                    List<Pet> list = lists.next();
                    list.get(0).hugPet = null;
                    list.get(1).hugPet = null;
                }
            }

            if(cdl != null){
                long count = cdl.getCount();
                for (int i = 0; i < count; i++)cdl.countDown();
            }

            MyService.isLSensor = false;
            isDark = false;
            if(couplePets != null)couplePets.clear();
        }

        private void couple(){
            List<Pet> pets = Utils.getAllPets();
            if(pets == null)return;
            if(couplePets == null)couplePets = new LinkedList<>();
            else if(!couplePets.isEmpty())couplePets.clear();
            for (Pet pet : pets){
                if(pet.CURRENT_ACTION == Pet.HUG || pet.CURRENT_ACTION == Pet.MOVE || pet.hugPet != null || pet.isOnceFly)continue;
                for (Pet pet1 : pets){
                    if(pet == pet1 || pet1.CURRENT_ACTION == Pet.HUG || pet1.CURRENT_ACTION == Pet.MOVE || pet1.hugPet != null || pet1.isOnceFly)continue;
                    if(Math.abs(pet.params.x - pet1.params.x) < pet.params.width/5.5 + pet1.params.width/5.5)continue;
                    if(!(pet.name.equals(MyService.LW) && pet1.name.equals(MyService.AX) || pet.name.equals(MyService.AX) && pet1.name.equals(MyService.LW)))continue;
                    pet.removeAllMessages();
                    pet1.removeAllMessages();
                    pet.hugPet = pet1;
                    pet1.hugPet = pet;
                    List<Pet> couplePetList = new LinkedList<>();
                    couplePetList.add(pet);
                    couplePetList.add(pet1);
                    couplePets.add(couplePetList);
                    break;
                }
            }
            if(MyService.isLSensor && couplePets.isEmpty())MyService.isLSensor = false;
        }

        private void twoWayRunning(){
            try {
                if(couplePets == null || couplePets.isEmpty())return;
                if(cdl == null)cdl = new CountDownLatch(couplePets.size() * 2);
                Iterator<List<Pet>> lists = couplePets.iterator();
                while (lists.hasNext()){
                    List<Pet> list = lists.next();
                    Pet pet = list.get(0);
                    Pet pet1 = list.get(1);
                    int x = (pet.params.x + pet1.params.x) / 2;
                    int y = (pet.params.y + pet1.params.y) / 2;
                    pet.twoWayRunnig(y, (int)((x - pet.params.x) * 0.08), (int)((y - pet.params.y) * 0.2), cdl);
                    pet1.twoWayRunnig(y, (int)((x - pet1.params.x) * 0.08),(int)((y - pet1.params.y) * 0.2), cdl);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cdl.await();
                            Log.i("----end------", "end");
                            MyService.isLSensor = false;
                            cdl = null;
                            isDark = true;
                            if(couplePets != null && !couplePets.isEmpty())couplePets.clear();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    static class GSensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            int angle = ((WindowManager)ctx1.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (angle){
                case Surface.ROTATION_90:
                    x = -sensorEvent.values[1];
                    y = sensorEvent.values[0];
                    break;
                case Surface.ROTATION_270:
                    x = sensorEvent.values[1];
                    y = -sensorEvent.values[0];
                    break;
            }
//            Log.i("-------x----", String.valueOf(x));
//            Log.i("-------y----", String.valueOf(y));
            Iterator<Map.Entry<String, List<Pet>>> it = MyService.groupPets.entrySet().iterator();
            if(Math.abs(y) > 0.2 || Math.abs(x) > 2){
                while (it.hasNext()){
                    Map.Entry<String, List<Pet>> entry = it.next();
                    for (Pet pet : entry.getValue()){
                        if(pet.BEFORE_MODE != Pet.TIMER_START && pet.BEFORE_MODE != Pet.FLY || pet.CURRENT_ACTION == Pet.MOVE || pet.CURRENT_ACTION == Pet.HUG)continue;
                        if(pet.params.y + pet.params.height/2 >= MyService.size.y/2 && Math.abs(x) > 2){
                            //if(pet.BEFORE_MODE != Pet.TIMER_START || pet.CURRENT_ACTION == Pet.G_SENSOR_XY)continue;
                            pet.removeAllMessages();

                            if(x > 5) pet.elfBody.setImageDrawable(pet.moveLeftWeightGifDrawable);
                            else if(x > 4)pet.elfBody.setImageDrawable(pet.moveLeftMiddleGifDrawable);
                            else if(x > 3)pet.elfBody.setImageDrawable(pet.moveLeftLightGifDrawable);
                            else if(Math.abs(x) <= 3)pet.elfBody.setImageDrawable(pet.direction.equals("left") ? pet.moveLeftGifDrawable : pet.moveRightGifDrawable);
                            else if(x > -3)pet.elfBody.setImageDrawable(pet.moveRightLightGifDrawable);
                            else if(x > -4)pet.elfBody.setImageDrawable(pet.moveRightMiddleGifDrawable);
                            else pet.elfBody.setImageDrawable(pet.moveRightWeightGifDrawable);
                            pet.CURRENT_ACTION = Pet.G_SENSOR_X;


                            if(x > 0){
                                if(pet.params.x - pet.params.width / 2 + pet.whDif / 2 + pet.pngDev < (-MyService.size.x / 2)){
                                    if(pet.params.x != -MyService.size.x / 2 + pet.params.width / 2 - pet.whDif / 2){
                                        pet.params.x = -MyService.size.x / 2 + pet.params.width / 2 - pet.whDif / 2;
                                        MyService.wm.updateViewLayout(pet.elfView, pet.params);
                                    }
                                    if (pet.BEFORE_MODE != Pet.TIMER_LEFT_START) pet.BEFORE_MODE = Pet.TIMER_LEFT_START;
                                    pet.climbToUp();
                                    pet.sendEmptyMessageDelayed(Pet.TIMER_LEFT_START, SpeedUtils.getCurrentFrequestTime());
                                    continue;
                                }

                                pet.params.x -= x * 10;
                            }else {
                                if(pet.params.x + pet.params.width / 2 - pet.whDif / 2 - pet.pngDev> (MyService.size.x / 2)){
                                    if(pet.params.x != MyService.size.x / 2 - pet.params.width / 2 + pet.whDif / 2){
                                        pet.params.x = MyService.size.x / 2 - pet.params.width / 2 + pet.whDif / 2;
                                        MyService.wm.updateViewLayout(pet.elfView, pet.params);
                                    }
                                    if (pet.BEFORE_MODE != Pet.TIMER_RIGHT_START) pet.BEFORE_MODE = Pet.TIMER_RIGHT_START;
                                    pet.climbToUp();
                                    pet.sendEmptyMessageDelayed(Pet.TIMER_RIGHT_START, SpeedUtils.getCurrentFrequestTime());
                                    continue;
                                }
                                pet.params.x += -x * 7;
                            }

                            MyService.wm.updateViewLayout(pet.elfView, pet.params);

                            pet.elfBody.setImageLevel(0);

                            pet.sendEmptyMessageDelayed(Pet.TIMER_START, SpeedUtils.getCurrentSpeedTime() * 2);
                        }else if(pet.params.y + pet.params.height/2 < MyService.size.y/2 || pet.params.y + pet.params.height/2 >= MyService.size.y/2 && y < 0){
                            pet.removeAllMessages();
                            pet.CURRENT_ACTION = Pet.G_SENSOR_XY;
                            Message msg = new Message();
                            Map<String, Long> data = new HashMap<>();
                            data.put("moveXDirection", -(long)(x * 9));
                            data.put("moveYDirection", (long)(y * 9));
                            msg.what = Pet.FLY;
                            msg.obj = data;
                            pet.sendMessage(msg);
                        }


                    }
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    static class SDSensorEventListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            double current = Math.sqrt(x * x + y * y + z * z);
            if(motiveState){
                if(current >= last)last = current;
                else {
                    if (Math.abs(current - last) > 15){
                        Log.i("----up-----", "up");
                        handler.removeMessages(SHAKE_COUNT_ZERO);
                        currentShakeCount++;
                        handler.sendEmptyMessageDelayed(SHAKE_COUNT_ZERO, 800);
                        motiveState = false;
                    }
                }
            }else{
                if(current <= last)last = current;
                else {
                    if (Math.abs(current - last) > 15){
                        Log.i("----down-----", String.valueOf(currentShakeCount));
                        motiveState = true;
                    }
                }
            }

            if(currentShakeCount >= maxStepCount){
                randomAddPet();
                currentShakeCount = 0;
            }

//            currentStepCount = sensorEvent.values[0];
//            Log.i("---currentStepCount---", String.valueOf(currentStepCount));
//            long milliSecond = System.currentTimeMillis();
//            float rate = 0 ;
//            if(lastStepCount != 0 && lastStepMilliSecond != 0){
//                rate = (step - lastStepCount) / (milliSecond - lastStepMilliSecond);
//                Log.i("----rate-----", String.valueOf(rate));
//
//            }
//
//            lastStepCount = step;
//            lastStepMilliSecond = milliSecond;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        private void randomAddPet(){
            if(MainActivity.serviceMessenger == null)return;

            View boomView = LayoutInflater.from(ctx1).inflate(R.layout.petelf, null);
            WindowManager.LayoutParams boomParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//6.0
                boomParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                boomParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            boomParams.format = PixelFormat.RGBA_8888;
            boomParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            boomParams.x = 0;
            boomParams.y = 0;
            int petW;
            if(MyService.orientation == Configuration.ORIENTATION_LANDSCAPE)petW = (int) (MyService.size.y * (MyService.currentSize / 100.0));
            else petW = (int) (MyService.size.x * (MyService.currentSize / 100.0));
            boomParams.width = petW;
            boomParams.height = petW;
            ImageView boomImg = boomView.findViewById(R.id.elfbody);
            boomImg.setImageResource(R.drawable.boom);
            MyService.wm.addView(boomView, boomParams);

            Message msg1 = new Message();
            msg1.what = BOOM;
            msg1.obj = boomView;
            handler.sendMessageDelayed(msg1, 800);


        }
    }

    static class PSensorEventListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            double current = Math.sqrt(x * x + y * y + z * z);

            if(motiveState){
                if(current >= last)last = current;
                else {
                    if (Math.abs(current - last) > 1){
                        //Log.i("----up-----", "up");
                        motiveState = false;
                    }
                }
            }else{
                if(current <= last)last = current;
                else {
                    if (Math.abs(current - last) > 1){
                        handler.removeMessages(STEP_COUNT_ZERO);
                        currentStepCount++;
                        handler.sendEmptyMessageDelayed(STEP_COUNT_ZERO, time / 2);
                        //Log.i("----down-----", String.valueOf(currentShakeCount));
                        motiveState = true;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    static class DSensorEventListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float distance = sensorEvent.values[0];
            Log.i("-----v----", String.valueOf(distance));
            if(distance <= 0 && Utils.hasCouple()){
                Utils.voice(ctx1, "lw/voice/axu.mp3");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    public static boolean isInCouple(Pet pet){
        if(couplePets == null || couplePets.isEmpty())return false;
        Iterator<List<Pet>> lists = couplePets.iterator();
        while (lists.hasNext()){
            List<Pet> list = lists.next();
            if(list.get(0) == pet || list.get(1) == pet)return true;
        }
        return false;
    }

}
