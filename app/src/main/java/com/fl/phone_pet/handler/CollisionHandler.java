package com.fl.phone_pet.handler;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.utils.SpeedUtils;
import com.fl.phone_pet.utils.Utils;
import com.fl.phone_pet.pojo.AiXin;
import com.fl.phone_pet.pojo.Pet;
import com.fl.phone_pet.pojo.PropMsg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class CollisionHandler extends Handler {

    List<Pet> pets;
    List<Integer> resIds;
    Context ctx;
    List<CountDownLatch> cdls;

    final int deviation = 20;
    int pngDeviation = 0;
    String imageExt = ".png";

    public static final int COLLISION_HAPPEN = 40001;
    public static final int REMOVE_AIXIN_VIEW = 40003;
    public static final int HUG = 40005;
    public static final int HUG_END = 40006;

    public CollisionHandler(Context ctx, Map<String, List<Pet>> groupPets){
        this.ctx = ctx;
        initResIds();
        if(groupPets != null && !groupPets.isEmpty())this.pets = new LinkedList<>();
        Set<String> keys = groupPets.keySet();
        for (String key : keys)this.pets.addAll(groupPets.get(key));

    }

    private void initResIds(){
        if(resIds == null)resIds = new LinkedList<>();
        int resId = ctx.getResources().getIdentifier("aixin", "array", ctx.getPackageName());
        String[] aixinStrs = ctx.getResources().getStringArray(resId);
        for (String str : aixinStrs){
            resIds.add(ctx.getResources().getIdentifier(str, "drawable", ctx.getPackageName()));
        }
    }

    private void run(int status, int objSize, Map<String, Object> datas){
        RelativeLayout aiXinContainerView = (RelativeLayout)datas.get("view");
        WindowManager.LayoutParams params = (WindowManager.LayoutParams)datas.get("params");
        int count = new Random().nextInt(50) + 100;
        if(cdls == null)cdls = new LinkedList<>();
        CountDownLatch cdl = new CountDownLatch(count);
        cdls.add(cdl);
        for(int i = 0; i < count; i++){
            AiXin aixim = new AiXin(ctx, this.resIds.get(new Random().nextInt(this.resIds.size())), status, objSize, params, cdl);
            aiXinContainerView.addView(aixim.aiXinView, aixim.aiXinParams);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
                    cdls.remove(cdl);
                    Message msg = new Message();
                    msg.what = REMOVE_AIXIN_VIEW;
                    msg.obj = aiXinContainerView;
                    sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void handleMessage(@NonNull Message msg){
        switch (msg.what){
            case COLLISION_HAPPEN:
                removeMessages(COLLISION_HAPPEN);
                if(this.pets == null || this.pets.isEmpty())return;
                int topY, bottomY, leftX, rightX;
                for (Pet pet : pets){
                    if(pet.CURRENT_ACTION == Pet.COLLISION
                            || pet.CURRENT_ACTION == Pet.HUG
                            || pet.CURRENT_ACTION == Pet.HUG_END
                            || pet.CURRENT_ACTION == Pet.FLY
                            || pet.CURRENT_ACTION == Pet.MOVE
                            || pet.name.equals(MyService.WZ)
                            || pet.CURRENT_ACTION == Pet.SPEECH_START)continue;
                    for (Pet pet1 : pets){
                        if(pet == pet1 || pet1.name.equals(MyService.WZ)
                                || pet.name.equals(pet1.name)
                                || pet1.CURRENT_ACTION == Pet.COLLISION
                                || pet1.CURRENT_ACTION == Pet.HUG
                                || pet.BEFORE_MODE != pet1.BEFORE_MODE
                                || pet1.CURRENT_ACTION == Pet.FLY
                                || pet1.CURRENT_ACTION == Pet.MOVE
                                || pet1.CURRENT_ACTION == Pet.HUG_END
                                || pet1.CURRENT_ACTION == Pet.SPEECH_START)continue;
                        topY = pet.params.y - pet.params.height/2 - pet1.params.height/2;
                        bottomY = pet.params.y + pet.params.height/2 + pet1.params.height/2;
                        leftX = pet.params.x - pet.params.width/2 - pet1.params.width/2;
                        rightX = pet.params.x + pet.params.width/2 + pet1.params.width/2;

//                        if(pedestoryRest.BEFORE_MODE == Pet.TIMER_TOP_START){
//                            pngDeviation = (int)(pet.params.width * 0.15);
//                        }else if(pet.BEFORE_MODE == Pet.TIMER_START){
//                            pngDeviation = (int)(pet.params.width * 0.2 + pet.params.width * 0.2);
//                        }

                        if(pet.params.y == pet1.params.y && (pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.RUN_LEFT && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && isCollision(pet, pet1)
                                || pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RUN_RIGHT && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && isCollision(pet1, pet)
                                || pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.LEFT_STAND && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && isCollision(pet, pet1)
                                || pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RIGHT_STAND && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && isCollision(pet1, pet)
                                || pet.CURRENT_ACTION == Pet.LEFT_STAND && pet1.CURRENT_ACTION == Pet.RUN_RIGHT && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && isCollision(pet1, pet)
                                || pet.CURRENT_ACTION == Pet.RIGHT_STAND && pet1.CURRENT_ACTION == Pet.RUN_LEFT && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && isCollision(pet, pet1)
                                || pet.CURRENT_ACTION == Pet.RIGHT_STAND && pet1.CURRENT_ACTION == Pet.LEFT_STAND && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && isCollision(pet, pet1)
                                || pet.CURRENT_ACTION == Pet.LEFT_STAND && pet1.CURRENT_ACTION == Pet.RIGHT_STAND && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && isCollision(pet1, pet)
                        )){
                            pet.removeAllMessages();
                            pet1.removeAllMessages();
                            if(pet.BEFORE_MODE == Pet.TIMER_START){
                                pet.CURRENT_ACTION = Pet.HUG;
                                pet1.CURRENT_ACTION = Pet.HUG;
                                int flag = pet.params.x < pet1.params.x && pet.name.equals(MyService.LW) || pet1.params.x < pet.params.x && pet1.name.equals(MyService.LW)? 0 : 1;
                                int petX = pet.params.x;
                                int pet1X = pet1.params.x;
                                shouHug(pet, pet1, flag);
                                run(AiXin.BOTTOM_STATUS, pet.params.height, createAiXinContainer((int)(Math.abs(petX- pet1X) * 1.4), pet.params.height*2, MyService.orientation == Configuration.ORIENTATION_LANDSCAPE ? (petX + pet1X)/2-(int)(Math.abs(petX- pet1X) * 1.4)/2 : (petX + pet1X)/2, pet.params.y + pet.params.height/2 - (int)(pet.params.height * (MyService.orientation == Configuration.ORIENTATION_LANDSCAPE ? 1.1 : 1.5)) - (pet.params.height*2)/2));
                            }else if(pet.BEFORE_MODE == Pet.TIMER_TOP_START){
                                pet.CURRENT_ACTION = Pet.COLLISION;
                                pet1.CURRENT_ACTION = Pet.COLLISION;
                                run(AiXin.TOP_STATUS, pet.params.height, createAiXinContainer((int)(Math.abs(pet.params.x - pet1.params.x)/1.4), pet.params.height*2, MyService.orientation == Configuration.ORIENTATION_LANDSCAPE ? (pet.params.x + pet1.params.x)/2-(int)(Math.abs(pet.params.x- pet1.params.x) * 1.4)/3 : (pet.params.x + pet1.params.x)/2, -MyService.size.y/2 + (int)(pet.params.height * 0.7) + (pet.params.height*2)/2));
                                pet.sendEmptyMessageDelayed(pet.BEFORE_MODE, 11 * SpeedUtils.getCurrentSpeedTime());
                                pet1.sendEmptyMessageDelayed(pet1.BEFORE_MODE, 11 * SpeedUtils.getCurrentSpeedTime());
                            }
                        }else if(pet.CURRENT_ACTION == Pet.CLIMB_UP&& pet1.CURRENT_ACTION == Pet.CLIMB_DOWN && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_DOWN&& pet1.CURRENT_ACTION == Pet.CLIMB_UP && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_UP && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_DOWN && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_DOWN && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_UP && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                        ){
//                            pet.removeAllMessages();
//                            pet1.removeAllMessages();
//                            pet.CURRENT_ACTION = Pet.COLLISION;
//                            pet1.CURRENT_ACTION = Pet.COLLISION;
//                            Log.i("----petY-----",String.valueOf(pet.params.y));
//                            Log.i("----pet1Y-----",String.valueOf(pet1.params.y));
//                            if(pet.BEFORE_MODE == Pet.TIMER_LEFT_START){
//                                run(AiXin.LEFT_STATUS, pet.params.width, createAiXinContainer(pet.params.width*2, (int)(Math.abs(pet.params.y - pet1.params.y)/1.4), -size.x/2 + (int)(pet.params.width/2.5) + (pet.params.width*2)/2, (int)((pet.params.y + pet1.params.y)/2) - 20));
//                            }else if(pet.BEFORE_MODE == Pet.TIMER_RIGHT_START){
//                                run(AiXin.RIGHT_STATUS, pet.params.width, createAiXinContainer(pet.params.width*2, (int)(Math.abs(pet.params.y - pet1.params.y)/1.4), size.x/2 - (int)(pet.params.width/2.5) - (pet.params.width*2)/2, (int)((pet.params.y + pet1.params.y)/2) - 20));
//                            }
//                            pet.sendEmptyMessageDelayed(pet.BEFORE_MODE, 11 * SpeedUtils.getCurrentSpeedTime());
//                            pet1.sendEmptyMessageDelayed(pet1.BEFORE_MODE, 11 * SpeedUtils.getCurrentSpeedTime());
                        }
                    }
                }
                sendEmptyMessageDelayed(COLLISION_HAPPEN, 50);
                break;
            case HUG:
//                removeMessages(HUG);
                Map map = (Map)(msg.obj);
                int currentLevel = ((LevelListDrawable)((Pet)map.get("pet")).elfBody.getDrawable()).getLevel();
                int currentLevel1 = ((LevelListDrawable)((Pet)map.get("pet1")).elfBody.getDrawable()).getLevel();
                if(currentLevel + 1 < (int)(map.get("maxLevel"))){
                    if(!(Boolean)(map.get("start"))){
                        Pet pet = (Pet)map.get("pet");
                        Pet pet1 = (Pet)map.get("pet1");
                        pet.elfBody.setImageLevel(currentLevel + 1);
                        pet1.elfBody.setImageLevel(currentLevel1 + 1);

                    }

                    Message msg1 = new Message();
                    msg1.what = HUG;
                    map.put("start", false);
                    msg1.obj = map;
                    sendMessageDelayed(msg1, SpeedUtils.getCurrentSpeedTime());
                }else{
                    Pet pet = ((Pet) map.get("pet"));
                    Pet pet1 = ((Pet) map.get("pet1"));
                    pet.hugEnd();
                    pet1.hugEnd();
                }
                break;
            case REMOVE_AIXIN_VIEW:
                if(MyService.wm != null)MyService.wm.removeView((View) msg.obj);
                break;
        }

    }

    public void addPet(Pet pet){
        if(this.pets == null)this.pets = new LinkedList<>();
        this.pets.add(pet);
        if(this.pets.size() == 1)sendEmptyMessage(0);
    }

    public void removePet(Pet pet){
        if(this.pets == null)return;
        this.pets.remove(pet);
    }

    private void shouHug(Pet pet, Pet pet1, int flag){
        LevelListDrawable levelListDrawable = new LevelListDrawable();
        LevelListDrawable levelListDrawable1 = new LevelListDrawable();
        String[] hdStrs = ctx.getResources().getStringArray(ctx.getResources().getIdentifier("hd", "array", ctx.getPackageName()));
        if(hdStrs == null || hdStrs.length <= 0)return;
        pet.removeAllMessages();
        pet1.removeAllMessages();
        int hugX = (pet.params.x + pet1.params.x)/2;
        pet.params.x = hugX;
        pet1.params.x= hugX;
        try{

            String[] hdInfo = hdStrs[new Random().nextInt(hdStrs.length)].split(":");

            for (int uu = 0; uu < Integer.valueOf(hdInfo[1]); uu++){
                String prefix;
                if(flag == 0){
                    prefix = MyService.LW + MyService.AX;
                }else{
                    prefix = MyService.AX + MyService.LW;
                }
                levelListDrawable.addLevel(uu, uu, Utils.assets2Drawable(ctx, "hd/" + prefix + hdInfo[0] + Integer.valueOf(uu + 1) + imageExt));
                levelListDrawable1.addLevel(uu, uu, Utils.assets2Drawable(ctx, "hd/" + prefix + hdInfo[0] + Integer.valueOf(uu + 1) + imageExt));
            }


            pet.elfBody.setImageDrawable(levelListDrawable);
            pet.elfBody.setImageLevel(0);
            MyService.wm.updateViewLayout(pet.elfView, pet.params);
            pet1.elfBody.setImageDrawable(levelListDrawable1);
            pet1.elfBody.setImageLevel(0);
            MyService.wm.updateViewLayout(pet1.elfView, pet1.params);
            Message msg = new Message();
            HashMap map = new HashMap<>();
            map.put("pet", pet);
            map.put("pet1", pet1);
            map.put("maxLevel", Integer.valueOf(hdInfo[1]));
            map.put("start", true);
            msg.obj = map;
            msg.what = HUG;
            sendMessage(msg);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Map<String, Object> createAiXinContainer(int width, int height, int x, int y){
        WindowManager.LayoutParams aiXinContainerParams = new WindowManager.LayoutParams();
        RelativeLayout aiXinContainerView = (RelativeLayout)LayoutInflater.from(ctx).inflate(R.layout.container, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
            aiXinContainerParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            aiXinContainerParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        aiXinContainerParams.format = PixelFormat.RGBA_8888;
        aiXinContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        aiXinContainerParams.width = width;
        aiXinContainerParams.height = height;
        aiXinContainerParams.x = x;
        aiXinContainerParams.y = y;
        MyService.wm.addView(aiXinContainerView, aiXinContainerParams);
        Map<String, Object> datas = new HashMap<>();
        datas.put("view", aiXinContainerView);
        datas.put("params", aiXinContainerParams);
        return datas;
    }

    public void destoryRes(){
        if(cdls != null && cdls.size() > 0){
            int hugViewsCount = cdls.size();
            for (int k = 0; k < hugViewsCount; k++){
                int countSize = (int) cdls.get(k).getCount();
                for (int count = 0; count < countSize; count++)cdls.get(k).countDown();
            }
            cdls.clear();
        }
        removeMessages(COLLISION_HAPPEN);
        removeMessages(HUG);
        removeMessages(HUG_END);
    }

    public void start(){
        sendEmptyMessage(COLLISION_HAPPEN);
    }

    private boolean isCollision(Pet pet, Pet pet1){
        int height = pet.params.height;
        int centerPointX = (pet.params.x + pet1.params.x) / 2;

        Bitmap bitmap = ((BitmapDrawable)(pet.elfBody.getDrawable().getCurrent())).getBitmap();
        Matrix matrix = new Matrix();
        float scale = (float)((pet.elfBody.getHeight() * 1.0)/bitmap.getHeight());
        matrix.postScale(scale, scale);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        Bitmap bitmap1 = ((BitmapDrawable)(pet1.elfBody.getDrawable().getCurrent())).getBitmap();
        Matrix matrix1 = new Matrix();
        float scale1 = (float)((pet1.elfBody.getHeight() * 1.0)/bitmap1.getHeight());
        matrix1.postScale(scale1, scale1);
        bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix1, true);

        int petPointX = bitmap.getWidth()/2 + (centerPointX - pet.params.x);
        int pet1PointX = centerPointX - (pet1.params.x - bitmap1.getWidth()/2);
        int count = 0;
        for(int i = 0; i < height; i++){
            if(petPointX < 0 || i < 0 || petPointX >= bitmap.getWidth() || i >= bitmap.getHeight() || pet1PointX < 0 || pet1PointX >= bitmap.getWidth())continue;
            if(bitmap.getPixel(petPointX, i) != 0 && bitmap1.getPixel(pet1PointX, i) != 0)count++;
        }
        if(count <= 5 || count >= 130)return false;
        return true;
    }

}
