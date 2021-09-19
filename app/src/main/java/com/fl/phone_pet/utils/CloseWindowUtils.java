package com.fl.phone_pet.utils;

import static android.view.View.VISIBLE;

import android.accessibilityservice.AccessibilityService;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.MyService2;
import com.fl.phone_pet.R;
import com.fl.phone_pet.pojo.Boom;
import com.fl.phone_pet.pojo.Pet;
import com.fl.phone_pet.pojo.PropMsg;
import com.fl.phone_pet.viewGroup.FoldLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CloseWindowUtils {

    public static boolean isClosing = false;
    public static FoldLayout foldLayout;
    public static int foldFlag = 1;
    public static List<Pet> foldPets = new LinkedList<>();

    public static final int BOOM = 80001;
    public static final int FOLD = 80002;

    public static void start(Context ctx, int type, Map<String, Object> datas){
        switch(type){
            case BOOM:
                boom(ctx, (Drawable)datas.get("boomImg"));
                break;
            case FOLD:
                foldWindow(ctx);
                break;
        }
    }

    public static void update(int type, Map<String, Float> datas){
        switch(type){
            case FOLD:
                if(foldLayout != null && datas != null){
                    foldLayout.setFactor(datas.get("factor"));
                }
                break;
        }
    }

    public static void endFold(){
        if(MyService.wm != null){
            WindowManager.LayoutParams downContainerParams = (WindowManager.LayoutParams) MyService.downContainerView.getLayoutParams();
            downContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            MyService.wm.updateViewLayout(MyService.downContainerView, downContainerParams);
        }
        if(MyService.myService.serviceMessenger != null){
            Message msg5 = new Message();
            msg5.what = MyService.HIDDEN_CONTAINER;
            try {
                MyService.myService.serviceMessenger.send(msg5);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if(!foldPets.isEmpty()){
            for (Pet pet : foldPets){
//                        pet.removeAllMessages();
                Message msg6 = new Message();
                Map<String, Long> datass = new HashMap<>();
                datass.put("moveXDirection", (long) (Math.random() * 40 + 15));
                datass.put("moveYDirection", (long) (Math.random() * 80));
                msg6.what = Pet.FLY;
                msg6.obj = datass;
                pet.sendMessage(msg6);
            }
        }

        CloseWindowUtils.foldFlag = 1;
        CloseWindowUtils.isClosing = false;
        CloseWindowUtils.foldPets.clear();
    }

    private static void foldWindow(Context ctx){
        if(isClosing || MyService.downContainerView == null)return;
        isClosing = true;
        Utils.clearDownContainer();
        if(MyService.downContainerView.getVisibility() != VISIBLE)MyService.downContainerView.setVisibility(VISIBLE);
        WindowManager.LayoutParams downContainerParams = (WindowManager.LayoutParams) MyService.downContainerView.getLayoutParams();
        downContainerParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        MyService.wm.updateViewLayout(MyService.downContainerView, downContainerParams);

        foldLayout = new FoldLayout(ctx);
        RelativeLayout.LayoutParams foldLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView imageView = new ImageView(ctx);
        imageView.setImageResource(R.drawable.uu);
        imageView.setBackgroundResource(R.color.white);
        FoldLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        foldLayout.addView(imageView, imgParams);
        MyService.downContainerView.addView(foldLayout, foldLayoutParams);

    }

    private static void boom(Context ctx, Drawable boomImg){
        if(isClosing || MyService.downContainerView == null)return;
        isClosing = true;
        Utils.clearDownContainer();
        if(MyService.downContainerView.getVisibility() != VISIBLE)MyService.downContainerView.setVisibility(VISIBLE);

        int randomCount = new Random().nextInt(230) + 230;
        int i = 0;
        CountDownLatch cdl = new CountDownLatch(randomCount);
        MyService.downList.add(cdl);

        //RelativeLayout boomContainerView = (RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.container, null);
        //RelativeLayout.LayoutParams boomContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        boomContainerView.setX(0);
//        boomContainerView.setY(0);
//        boomContainerView.setBackgroundResource(R.color.white);
//        boomContainerView.setAlpha(0);
        //ObjectAnimator showAnimator = ObjectAnimator.ofFloat(boomContainerView, "alpha", 0, 0.8f, 0);
        //showAnimator.setDuration(7 * SpeedUtils.getCurrentSpeedTime());
        //showAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        //showAnimator.start();
        //MyService.downContainerView.addView(boomContainerView, boomContainerParams);

        while (i < randomCount) {
            Boom boom = new Boom(ctx, cdl, boomImg);
            MyService.downContainerView.addView(boom.boomView, boom.boomParams);
            i++;
        }

        backHome();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(10, TimeUnit.SECONDS);
                    MyService.downList.remove(cdl);
                    if(MyService.myService.serviceMessenger != null){
                        Message msg = new Message();
                        msg.what = MyService.HIDDEN_CONTAINER;
                        try {
                            MyService.myService.serviceMessenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        isClosing = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void backHome(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long)(2.4 * SpeedUtils.getCurrentSpeedTime()));
                    MyService2.as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
