package com.fl.phone_pet.handler;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
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

import pl.droidsonroids.gif.GifDrawable;

public class CollisionHandler extends Handler {

    List<Pet> pets;
    List<Integer> resIds;
    Context ctx;
    Point size;
    public CopyOnWriteArrayList<View> hugViews;
    Map<Integer, MediaPlayer> mp;
    RelativeLayout downContainerView;
    CopyOnWriteArrayList downList;

    final int deviation = 60;
    int pngDeviation;

    public static final int COLLISION = 40001;
    public static final int END_HUG = 40002;
    public static final int REMOVE_AIXIN_VIEW = 40003;
    public static final int HIDDEN_CONTAINER = 40004;
//
    public CollisionHandler(Context ctx, Map<String, List<Pet>> groupPets, Point size, Map<Integer, MediaPlayer> mp, RelativeLayout downContainerView, CopyOnWriteArrayList downList){
        this.ctx = ctx;
        this.size = size;
        this.mp = mp;
        this.downContainerView = downContainerView;
        this.downList = downList;
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
        CountDownLatch cdl = new CountDownLatch(count);
        for(int i = 0; i < count; i++){
            AiXin aixim = new AiXin(ctx, this.resIds.get(new Random().nextInt(this.resIds.size())), status, objSize, params, cdl);
            aiXinContainerView.addView(aixim.aiXinView, aixim.aiXinParams);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
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
            case COLLISION:
                removeMessages(COLLISION);
                if(this.pets == null || this.pets.isEmpty())return;
                int topY, bottomY, leftX, rightX, tempPngDeviation = 0;
                for (Pet pet : pets){
                    if(pet.CURRENT_ACTION == Pet.COLLISION || pet.CURRENT_ACTION == Pet.FLY
                            || pet.CURRENT_ACTION == Pet.MOVE || pet.name.equals(MyService.WZ)
                            || pet.CURRENT_ACTION == Pet.SPEECH_START)continue;
                    for (Pet pet1 : pets){
                        if(pet == pet1 || pet1.name.equals(MyService.WZ)
                                || pet.name.equals(pet1.name)
                                || pet1.CURRENT_ACTION == Pet.COLLISION || pet.BEFORE_MODE != pet1.BEFORE_MODE
                                || pet1.CURRENT_ACTION == Pet.FLY ||pet1.CURRENT_ACTION == Pet.MOVE
                                || pet1.CURRENT_ACTION == Pet.SPEECH_START)continue;
                        topY = pet.params.y - pet.params.height/2 - pet1.params.height/2;
                        bottomY = pet.params.y + pet.params.height/2 + pet1.params.height/2;
                        leftX = pet.params.x - pet.params.width/2 - pet1.params.width/2;
                        rightX = pet.params.x + pet.params.width/2 + pet1.params.width/2;

                        if(pet.BEFORE_MODE == Pet.TIMER_TOP_START){
                            tempPngDeviation = pngDeviation;
                            pngDeviation = (int)Math.round(pet.whDif/1.7 + pet1.whDif/1.7);
                        }else if(pet.BEFORE_MODE == Pet.TIMER_START){
                            pngDeviation = (int)(pet.params.height * 0.4 + pet.whDif/2 +pet1.whDif/2);
                        }
                        if(pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.RUN_LEFT && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RUN_RIGHT && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                                || pet.CURRENT_ACTION == Pet.RUN_RIGHT && pet1.CURRENT_ACTION == Pet.LEFT_STAND && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.RUN_LEFT && pet1.CURRENT_ACTION == Pet.RIGHT_STAND && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                                || pet.CURRENT_ACTION == Pet.LEFT_STAND && pet1.CURRENT_ACTION == Pet.RUN_RIGHT && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                                || pet.CURRENT_ACTION == Pet.RIGHT_STAND && pet1.CURRENT_ACTION == Pet.RUN_LEFT && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.RIGHT_STAND && pet1.CURRENT_ACTION == Pet.LEFT_STAND && pet.params.x < pet1.params.x && pet1.params.x <= rightX - pngDeviation && pet1.params.x > rightX - pngDeviation - deviation
                                || pet.CURRENT_ACTION == Pet.LEFT_STAND && pet1.CURRENT_ACTION == Pet.RIGHT_STAND && pet.params.x > pet1.params.x && pet1.params.x >= leftX + pngDeviation && pet1.params.x < leftX + pngDeviation + deviation
                        ){
                            if(pngDeviation == 0)pngDeviation = tempPngDeviation;
                            pet.removeAllMessages();
                            pet1.removeAllMessages();
                            pet.CURRENT_ACTION = Pet.COLLISION;
                            pet1.CURRENT_ACTION = Pet.COLLISION;
                            if(pet.BEFORE_MODE == Pet.TIMER_START && pet.params.y == pet1.params.y){
                                int flag = pet.params.x < pet1.params.x && pet.name.equals(MyService.LW) || pet1.params.x < pet.params.x && pet1.name.equals(MyService.LW)? 0 : 1;
                                int petX = pet.params.x;
                                int pet1X = pet1.params.x;
                                shouHug(pet, pet1, flag);
                                run(AiXin.BOTTOM_STATUS, pet.params.height, createAiXinContainer((int)(Math.abs(petX- pet1X) * 1.4), pet.params.height*2, (petX + pet1X)/2, size.y/2 - pet.params.height - (pet.params.height*2)/2 - MyService.deviation));
                            }else if(pet.BEFORE_MODE == Pet.TIMER_TOP_START){
                                run(AiXin.TOP_STATUS, pet.params.height, createAiXinContainer((int)(Math.abs(pet.params.x - pet1.params.x)/1.4), pet.params.height*2, (pet.params.x + pet1.params.x)/2, -size.y/2 + pet.params.height + (pet.params.height*2)/2 - pet.params.height / 2));
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
                                run(AiXin.LEFT_STATUS, pet.params.width, createAiXinContainer(pet.params.width*2, (int)(Math.abs(pet.params.y - pet1.params.y)/1.4), -size.x/2 + pet.params.width + (pet.params.width*2)/2 - pet.params.width / 2, (pet.params.y + pet1.params.y)/2));
                            }else if(pet.BEFORE_MODE == Pet.TIMER_RIGHT_START){
                                run(AiXin.RIGHT_STATUS, pet.params.width, createAiXinContainer(pet.params.width*2, (int)(Math.abs(pet.params.y - pet1.params.y)/1.4), size.x/2 - pet.params.width - (pet.params.width*2)/2 + pet.params.width / 2, (pet.params.y + pet1.params.y)/2));
                            }
                            pet.sendEmptyMessageDelayed(pet.BEFORE_MODE, 3000);
                            pet1.sendEmptyMessageDelayed(pet1.BEFORE_MODE, 3000);
                        }
                    }
                }
                sendEmptyMessageDelayed(COLLISION, 50);
                break;
            case END_HUG:
//                removeMessages(END_HUG);
//                View hugView = (View)msg.obj;
//                hugViews.remove(hugView);
//                if(MyService.wm != null)MyService.wm.removeView(hugView);
                Map map = (Map)msg.obj;
                int petX = (int)map.get("petX");
                Pet pet = (Pet)map.get("pet");
                Pet pet1 = (Pet)map.get("pet1");
                Drawable petImage = (Drawable)map.get("petImage");
                pet1.elfView.setVisibility(VISIBLE);
                pet.params.x = petX;
                pet.elfBody.setImageDrawable(petImage);
                MyService.wm.updateViewLayout(pet.elfView, pet.params);
                pet.sendEmptyMessage(pet.BEFORE_MODE);
                pet1.sendEmptyMessage(pet1.BEFORE_MODE);
                break;
            case REMOVE_AIXIN_VIEW:
//                removeMessages(REMOVE_AIXIN_VIEW);
                if(MyService.wm != null)MyService.wm.removeView((View) msg.obj);
                break;
            case HIDDEN_CONTAINER:
//                removeMessages(HIDDEN_CONTAINER);
                if(downContainerView.getVisibility() == VISIBLE){
                    downContainerView.setVisibility(View.GONE);
                    this.downContainerView.removeAllViews();
                }
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
        pet.removeAllMessages();
        pet1.removeAllMessages();
        int petX = pet.params.x;
        int hugX = (pet.params.x + pet1.params.x)/2;
        Drawable petImage = pet.elfBody.getDrawable();
        pet.params.x = hugX;
        try{
            pet.elfBody.setImageDrawable(new GifDrawable(ctx.getAssets(), flag == 0 ? "hd/lwaxhug.gif" : "hd/axlwhug.gif"));
            MyService.wm.updateViewLayout(pet.elfView, pet.params);
            pet1.elfView.setVisibility(View.GONE);
            Message msg = new Message();
            HashMap map = new HashMap<>();
            map.put("pet", pet);
            map.put("pet1", pet1);
            map.put("petX", petX);
            map.put("petImage", petImage);
            msg.obj = map;
            msg.what = END_HUG;
            sendMessageDelayed(msg, 3800);

        }catch (Exception e){
            e.printStackTrace();
        }
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
            if(hugViews == null)hugViews = new CopyOnWriteArrayList<>();
            hugViews.add(hugView);
            hugView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            showGun();
                            playVoiceGun();
                            break;
                    }
                    return true;
                }
            });
            MyService.wm.addView(hugView, hugParam);
            Message msg = new Message();
            msg.obj = hugView;
            msg.what = END_HUG;
            sendMessageDelayed(msg, 3800);
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
        aiXinContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
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
        if(hugViews != null && hugViews.size() > 0){
            removeMessages(END_HUG);
            int hugViewsCount = hugViews.size();
            for (int k = 0; k < hugViewsCount; k++)MyService.wm.removeView(hugViews.get(k));
            hugViews.clear();
        }
    }

    synchronized private void playVoiceGun() {
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
            mp1.setDataSource(MyService.OSS_BASE + "lw/mscs/gunba.mp3");
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

    private void showGun(){
        if(this.downContainerView.getVisibility() != VISIBLE)this.downContainerView.setVisibility(VISIBLE);
        CountDownLatch cdl = new CountDownLatch(1);
        this.downList.add(cdl);
        PropMsg pm = new PropMsg(ctx, size, R.drawable.gun, cdl, (int)(this.size.x / 1.1));
        this.downContainerView.addView(pm.propView, pm.propParams);
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
}
