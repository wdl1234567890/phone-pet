package com.fl.phone_pet.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.pojo.Pet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class SensorUtils {
    private static SensorManager mSensorManager;
    private static Sensor gSensor;
    private static Sensor lSensor;
    private static GSensorEventListener gSensorEventListener = new GSensorEventListener();
    private static LSensorEventListener lSensorEventListener = new LSensorEventListener();
    public static Context ctx1;
    private static List<List<Pet>> couplePets;
    public static volatile CountDownLatch cdl;
    public static boolean isDark = false;

    public static void registerGSensor(Context ctx){
        ctx1 = ctx;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        if(gSensor == null)gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(gSensorEventListener, gSensor, (int)(SpeedUtils.getCurrentSpeedTime() * 800));
    }

    public static void registerLSensor(Context ctx){
        ctx1 = ctx;
        if(mSensorManager == null)mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        if(lSensor == null)lSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(lSensorEventListener, lSensor, (int)(SpeedUtils.getCurrentSpeedTime() * 800));
    }

    public static void unregisterGSensor(){
        if(mSensorManager != null)mSensorManager.unregisterListener(gSensorEventListener);
    }

    public static void unregisterLSensor(){
        if(mSensorManager != null){
            lSensorEventListener.backState();
            mSensorManager.unregisterListener(lSensorEventListener);
        }

    }

    public static class LSensorEventListener implements SensorEventListener {


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];

            //Log.i("----light------", String.valueOf(x));
            if(!isDark && x <= 0 && !MyService.isLSensor){
                //isDark = true;
                MyService.isLSensor = true;
                couple();
                twoWayRunning();
            }else if(isDark && x > 0){
                isDark = false;
                backState();
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
                    if(Math.abs(pet.params.x - pet1.params.x) < pet.params.width/1.2 + pet1.params.width/1.2)continue;
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
}
