package com.fl.phone_pet.handler;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.pojo.AiXin;
import com.fl.phone_pet.pojo.Pet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import pl.droidsonroids.gif.GifDrawable;

public class CollisionHandler extends Handler {

    List<Pet> pets;
    List<Integer> resIds;
    Context ctx;
    Point size;
    WindowManager wm;
    public List<View> hugViews;
    final int deviation = 60;
    int pngDeviation;

    public static final int COLLISION = 40001;
    public static final int END_HUG = 40002;
//
    public CollisionHandler(Context ctx, Map<String, List<Pet>> groupPets, WindowManager wm, Point size){
        this.ctx = ctx;
        this.wm = wm;
        this.size = size;
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

    private void run(int centralPoint, int status, int objSize){
        int count = new Random().nextInt(10) + 10;
        for(int i = 0; i < count; i++){
            new AiXin(ctx, wm, size, this.resIds.get(new Random().nextInt(this.resIds.size())), centralPoint, objSize, status).run();
        }
    }

    @Override
    public void handleMessage(@NonNull Message msg){
        switch (msg.what){
            case COLLISION:
                removeMessages(COLLISION);
                if(this.pets == null || this.pets.isEmpty())return;
                int topY, bottomY, leftX, rightX, tempPngDeviation = 0;
                for (Pet pet : pets){
                    if(pet.CURRENT_ACTION == Pet.COLLISION || pet.CURRENT_ACTION == Pet.FLY || pet.CURRENT_ACTION == Pet.MOVE || pet.name.equals(MyService.WZ))continue;
                    for (Pet pet1 : pets){
                        if(pet == pet1 || pet1.name.equals(MyService.WZ)
                                || pet.name.equals(pet1.name)
                                || pet1.CURRENT_ACTION == Pet.COLLISION || pet.BEFORE_MODE != pet1.BEFORE_MODE
                                || pet1.CURRENT_ACTION == Pet.FLY ||pet1.CURRENT_ACTION == Pet.MOVE)continue;
                        topY = pet.params.y - pet.params.height/2 - pet1.params.height/2;
                        bottomY = pet.params.y + pet.params.height/2 + pet1.params.height/2;
                        leftX = pet.params.x - pet.params.width/2 - pet1.params.width/2;
                        rightX = pet.params.x + pet.params.width/2 + pet1.params.width/2;

//                if(this.deviation == -1)this.deviation = (int)(pet.params.width * 0.07);
                        if(pet.BEFORE_MODE == Pet.TIMER_TOP_START){
                            tempPngDeviation = pngDeviation;
                            pngDeviation = 0;
                        }else if(pet.BEFORE_MODE == Pet.TIMER_START){
                            pngDeviation = (int)(pet.params.height * 0.5 + pet.whDif +pet1.whDif);
                        }
                        if(pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.RUN_LEFT && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RUN_RIGHT && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                                || pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.LEFT_STAND && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RIGHT_STAND && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                                || pet.CURRENT_ACTION == Pet.LEFT_STAND && pet1.CURRENT_ACTION == Pet.RUN_RIGHT && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                                || pet.CURRENT_ACTION == Pet.RIGHT_STAND && pet1.CURRENT_ACTION == Pet.RUN_LEFT && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.LEFT_STAND && pet1.CURRENT_ACTION == Pet.RIGHT_STAND && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.RIGHT_STAND && pet1.CURRENT_ACTION == Pet.LEFT_STAND && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                        ){
                            if(pngDeviation == 0)pngDeviation = tempPngDeviation;
                            pet.removeAllMessages();
                            pet1.removeAllMessages();
                            pet.CURRENT_ACTION = Pet.COLLISION;
                            pet1.CURRENT_ACTION = Pet.COLLISION;
                            if(pet.BEFORE_MODE == Pet.TIMER_START){
                                int flag = pet.params.x < pet1.params.x && pet.name.equals(MyService.LW) || pet1.params.x < pet.params.x && pet1.name.equals(MyService.LW)? 0 : 1;
                                pet.elfView.setVisibility(View.GONE);
                                pet1.elfView.setVisibility(View.GONE);
                                shouHug((pet.params.x + pet1.params.x)/2, this.size.y/2 - pet.params.height/2 - MyService.deviation, flag, pet.params.height);
                                //run((pet.params.x + pet1.params.x)/2 , AiXin.BOTTOM_STATUS, pet.params.width);
                            }else if(pet.BEFORE_MODE == Pet.TIMER_TOP_START){
                                run((pet.params.x + pet1.params.x)/2 , AiXin.TOP_STATUS, pet.params.width);
                            }
                            pet.sendEmptyMessageDelayed(pet.BEFORE_MODE, 3800);
                            pet1.sendEmptyMessageDelayed(pet1.BEFORE_MODE, 3800);
                        }else if(pet.CURRENT_ACTION == Pet.CLIMB_UP&& pet1.CURRENT_ACTION == Pet.CLIMB_DOWN && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_DOWN&& pet1.CURRENT_ACTION == Pet.CLIMB_UP && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_UP && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_DOWN && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_DOWN && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_UP && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y > pet1.params.y && pet1.params.y >= topY && pet1.params.y < topY + deviation
                                || pet.CURRENT_ACTION == Pet.CLIMB_STAND && pet1.CURRENT_ACTION == Pet.CLIMB_STAND && pet.params.y < pet1.params.y && pet1.params.y <= bottomY && pet1.params.y > bottomY - deviation
                        ){
                            pet.removeAllMessages();
                            pet1.removeAllMessages();
                            pet.CURRENT_ACTION = Pet.COLLISION;
                            pet1.CURRENT_ACTION = Pet.COLLISION;
                            if(pet.BEFORE_MODE == Pet.TIMER_LEFT_START){
                                run((pet.params.y + pet1.params.y)/2 , AiXin.LEFT_STATUS, pet.params.width);
                            }else if(pet.BEFORE_MODE == Pet.TIMER_RIGHT_START){
                                run((pet.params.y + pet1.params.y)/2 , AiXin.RIGHT_STATUS, pet.params.width);
                            }
                            pet.sendEmptyMessageDelayed(pet.BEFORE_MODE, 3000);
                            pet1.sendEmptyMessageDelayed(pet1.BEFORE_MODE, 3000);
                        }
                    }
                }
                sendEmptyMessageDelayed(COLLISION, 50);
                break;
            case END_HUG:
                removeMessages(END_HUG);
                View hugView = (View)msg.obj;
                hugViews.remove(hugView);
                if(wm != null)wm.removeView(hugView);
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

    private void shouHug(int x, int y, int flag, int height){
        try{
            WindowManager.LayoutParams hugParam = new WindowManager.LayoutParams();
            View hugView = LayoutInflater.from(ctx).inflate(R.layout.petelf, null);
            ImageView hugImg = hugView.findViewById(R.id.elfbody);
            hugImg.setImageDrawable(new GifDrawable(ctx.getAssets(), flag == 0 ? "hd/lwaxhug.gif" : "hd/axlwhug.gif"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                hugParam.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                hugParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            hugParam.format = PixelFormat.RGBA_8888;
            hugParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            hugParam.height = height;
            hugParam.width = (int)(height *  1.084);
            hugParam.x = x;
            hugParam.y = y;
            if(hugViews == null)hugViews = new ArrayList<>();
            hugViews.add(hugView);
            wm.addView(hugView, hugParam);
            Message msg = new Message();
            msg.obj = hugView;
            sendMessageDelayed(msg, 3800);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
//
//    @Override
//    public void handleMessage(@NonNull Message msg) {
//        int topY, bottomY, leftX, rightX;
//        for (Pet pet : pets){
//            for (Pet pet1 : pets){
//                if(pet == pet1)continue;
//                topY = pet.params.y - pet.params.height/2 - pet1.params.height/2;
//                bottomY = pet.params.y + pet.params.height/2 + pet1.params.height/2;
//                leftX = pet.params.x - pet.params.width/2 - pet1.params.width/2;
//                rightX = pet.params.x + pet.params.width/2 + pet1.params.width/2;
//                if(pet1.params.x >= leftX && pet1.params.x <= rightX && pet1.params.y >= topY && pet1.params.y <= bottomY){
//                    if(pet.CURRENT_ACTION != Pet.SLEEP)pet.pausedCurrentAction();
//                    if(pet.BEFORE_MODE == Pet.TIMER_START && pet1.BEFORE_MODE == Pet.TIMER_START){
//                        if(pet.params.x < pet1.params.x){
//                            if(pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.SLEEP){
////                                pet.moveSlow();
////                                pet.recoveryCurrentAction();
//                                pet.sendEmptyMessage(Pet.TIMER_START);
//                            }else if(pet.CURRENT_ACTION == Pet.SLEEP && pet1.CURRENT_ACTION == Pet.RUN_LEFT){
////                                pet.pushToLeftSleep();
//                                pet.sendEmptyMessage(Pet.TIMER_START);
//                            }else if(pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.RUN_LEFT){
//                                pet.sendEmptyMessage(Pet.TIMER_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }else{
//                            if(pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.SLEEP){
////                                pet.moveSlow();
////                                pet.recoveryCurrentAction();
//                                pet.sendEmptyMessage(Pet.TIMER_START);
//                            }else if(pet.CURRENT_ACTION == Pet.SLEEP && pet1.CURRENT_ACTION == Pet.RUN_RIGHT){
////                                pet.pushToRightSleep();
//                                pet.sendEmptyMessage(Pet.TIMER_START);
//                            }else if(pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RUN_RIGHT){
//                                pet.sendEmptyMessage(Pet.TIMER_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }
//                    }else if(pet.BEFORE_MODE == Pet.TIMER_LEFT_START && pet1.BEFORE_MODE == Pet.TIMER_LEFT_START){
//                        if(pet.params.y < pet1.params.y){
//                            if(pet.CURRENT_ACTION == Pet.CLIMB_DOWN && pet1.CURRENT_ACTION == Pet.CLIMB_UP){
//                                pet.sendEmptyMessage(Pet.TIMER_LEFT_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }else{
//                            if(pet.CURRENT_ACTION == Pet.CLIMB_UP && pet1.CURRENT_ACTION == Pet.CLIMB_DOWN){
//                                pet.sendEmptyMessage(Pet.TIMER_LEFT_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }
//
//                    }else if(pet.BEFORE_MODE == Pet.TIMER_RIGHT_START && pet1.BEFORE_MODE == Pet.TIMER_RIGHT_START){
//                        if(pet.params.y < pet1.params.y){
//                            if(pet.CURRENT_ACTION == Pet.CLIMB_DOWN && pet1.CURRENT_ACTION == Pet.CLIMB_UP){
//                                pet.sendEmptyMessage(Pet.TIMER_RIGHT_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }else{
//                            if(pet.CURRENT_ACTION == Pet.CLIMB_UP && pet1.CURRENT_ACTION == Pet.CLIMB_DOWN){
//                                pet.sendEmptyMessage(Pet.TIMER_RIGHT_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }
//
//                    }else if(pet.BEFORE_MODE == Pet.TIMER_TOP_START && pet1.BEFORE_MODE == Pet.TIMER_TOP_START){
//                        if (pet.params.x < pet1.params.x){
//                            if (pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.RUN_LEFT){
//                                pet.sendEmptyMessage(Pet.TIMER_TOP_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }else {
//                            if (pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RUN_RIGHT){
//                                pet.sendEmptyMessage(Pet.TIMER_TOP_START);
//                            }else{
//                                pet.recoveryCurrentAction();
//                            }
//                        }
//
//                    }else if(pet.BEFORE_MODE == Pet.FLY){
//
//                        Log.i("----fly-----","-------fly-----");
//                        if(pet.params.x == pet1.params.x - pet1.params.width/2 - pet.params.width/2
//                                && pet.params.y == pet1.params.y + pet1.params.height/2 + pet.params.height/2
//                                || pet.params.x == pet1.params.x + pet1.params.width/2 + pet.params.width/2
//                                && pet.params.y == pet1.params.y + pet1.params.height/2 + pet.params.height/2
//                                || pet.params.x == pet1.params.x + pet1.params.width/2 + pet.params.width/2
//                                && pet.params.y == pet1.params.y - pet1.params.height/2 - pet.params.height/2
//                                || pet.params.x == pet1.params.x - pet1.params.width/2 - pet.params.width/2
//                                && pet.params.y == pet1.params.y - pet1.params.height/2 - pet.params.height/2){
//                            Log.i("++++++++++","++++++++++++");
//                            pet.oppositeDirectionFlySpeedXY();
//                        }else if (pet.params.y >= pet1.params.y + pet1.params.height/2 + pet.params.height/2 - deviation
//                                || pet.params.y <= pet1.params.y - pet1.params.height/2 - pet.params.height/2 + deviation){
//                            Log.i("***********","***********");
//                            pet.oppositeDirectionFlySpeedY();
//                        }else if (pet.params.x >= pet1.params.x + pet1.params.width/2 + pet.params.width/2 - deviation
//                                ||pet.params.x <= pet1.params.x - pet1.params.width/2 - pet.params.width/2 + deviation){
//                            Log.i("(((((((((((","((((((((((((((");
//                            pet.oppositeDirectionFlySpeedX();
//                        }
//                        pet.recoveryCurrentAction();
//
//                    }else if(pet.CURRENT_ACTION != Pet.FLY){
//                        pet.recoveryCurrentAction();
//                    }else{
//                        pet.recoveryCurrentAction();
//                    }
//                }
//            }
//        }
//        sendEmptyMessageDelayed(0, 10);
//    }
}
