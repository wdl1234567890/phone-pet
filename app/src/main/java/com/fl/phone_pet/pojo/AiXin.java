package com.fl.phone_pet.pojo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.fl.phone_pet.R;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class AiXin  extends Handler {
    public static final int LEFT_STATUS = 40003;
    public static final int RIGHT_STATUS = 40004;
    public static final int TOP_STATUS = 40005;
    public static final int BOTTOM_STATUS = 40006;

    public RelativeLayout.LayoutParams aiXinParams;
    public View aiXinView;
    ImageView aiXinImg;
    Integer aiXinContent;
    Context ctx;
    int status;
    int objSize;
    CountDownLatch cdl;
    WindowManager.LayoutParams params;

    public AiXin(Context ctx, Integer aiXinContent, int status, int objSize, WindowManager.LayoutParams params, CountDownLatch cdl){
        this.ctx = ctx;
        this.aiXinContent = aiXinContent;
        this.status = status;
        this.objSize = objSize;
        this.params = params;
        this.cdl = cdl;
        this.aiXinView = LayoutInflater.from(ctx).inflate(R.layout.prop_msg, null);
        this.aiXinImg = aiXinView.findViewById(R.id.prop_img);
        aiXinImg.setImageResource(aiXinContent);
        initParams();
    }

    private void initParams(){
        int randomSize = (int) (Math.random() * (objSize/2.5));
        this.aiXinParams = new RelativeLayout.LayoutParams(randomSize, randomSize);
        int randomX = 0;
        int randomY = 0;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flyAnimator = null;
        ObjectAnimator hidenAnimator = null;
//        switch (this.status){
//            case LEFT_STATUS:
//                randomX = (int) (Math.random() * 15 - (size.x/2) + objSize);
//                randomY = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
//                aiXinView.setRotation(90);
//                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationX", 300);
//                break;
//            case RIGHT_STATUS:
//                randomX = (int) (-Math.random() * 15 + (size.x/2) - objSize);
//                randomY = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
//                aiXinView.setRotation(-90);
//                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationX", -300);
//                break;
//            case TOP_STATUS:
//                randomX = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
//                randomY = (int) (int) (Math.random() * 15 - (size.y/2) + objSize);
//                aiXinView.setRotation(180);
//                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationY", 300);
//                break;
//            case BOTTOM_STATUS:
//                randomX = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
//                randomY = (int) (int) (-Math.random() * 15 + (size.y/2) - objSize);
//                aiXinView.setRotation(0);
//                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationY", -300);
//                break;
//        }
        switch (this.status){
            case LEFT_STATUS:
                randomX = (int) (Math.random() * 6 );
                randomY = (int) (Math.random() * (this.params.height - randomSize));
                aiXinView.setRotation(90);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationX", (int)(Math.random() * (this.params.width - randomSize - 6) + 6));
                break;
            case RIGHT_STATUS:
                randomX = (int) (this.params.width - randomSize - Math.random() * 6);
                randomY = (int) (Math.random() * (this.params.height - randomSize));
                aiXinView.setRotation(-90);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationX", -(int)(Math.random() * (this.params.width - randomSize - 6) + 6));
                break;
            case TOP_STATUS:
                randomX = (int) (Math.random() * (this.params.width - randomSize));
                randomY = (int) (Math.random() * 6);
                aiXinView.setRotation(180);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationY", (int)(Math.random() * (this.params.height - randomSize - 6) + 6));
                break;
            case BOTTOM_STATUS:
                randomX = (int) (Math.random() * (this.params.width - randomSize));
                randomY = (int) (this.params.height - randomSize - Math.random() * 6);
                aiXinView.setRotation(0);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationY", -(int)(Math.random() * (this.params.height - randomSize - 6) + 6));
                break;
        }
        aiXinView.setAlpha(1);
        aiXinView.setX(randomX);
        aiXinView.setY(randomY);
        flyAnimator.setDuration(2000);
        hidenAnimator = ObjectAnimator.ofFloat(aiXinView,"alpha", 0);
        hidenAnimator.setDuration(1500);
        animatorSet.play(hidenAnimator).after(flyAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cdl.countDown();
            }
        });
        animatorSet.start();
    }

//    @Override
//    public void handleMessage(@NonNull Message msg) {
//        switch (msg.what){
//            case FLY:
//                removeMessages(FLY);
//                if(aiXinView.getVisibility() != View.VISIBLE)aiXinView.setVisibility(View.VISIBLE);
//                switch (status){
//                    case LEFT_STATUS:
//                        aiXinParams.x = (int) (aiXinParams.x + v0 + 1/2 * g);
//                        aiXinParams.y = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
//                        v0 = (int) (v0 + g);
//                        wm.updateViewLayout(aiXinView, aiXinParams);
//                        if(aiXinParams.x >= -size.x/2 + 3 * objSize){
//                            sendEmptyMessage(DISAPPEAR);
//                        }else {
//                            sendEmptyMessageDelayed(FLY, 50);
//                        }
//                        break;
//                    case RIGHT_STATUS:
//                        aiXinParams.x = (int) (aiXinParams.x + v0 + 1/2 * g);
//                        aiXinParams.y = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
//                        v0 = (int) (v0 + g);
//                        wm.updateViewLayout(aiXinView, aiXinParams);
//                        if(aiXinParams.x <= size.x/2 - 3 * objSize){
//                            sendEmptyMessage(DISAPPEAR);
//                        }else {
//                            sendEmptyMessageDelayed(FLY, 50);
//                        }
//                        break;
//                    case TOP_STATUS:
//                        aiXinParams.y = (int) (aiXinParams.y + v0 + 1/2 * g);
//                        aiXinParams.x = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
//                        v0 = (int) (v0 + g);
//                        wm.updateViewLayout(aiXinView, aiXinParams);
//                        if(aiXinParams.y >= -size.y/2 + 3 * objSize){
//                            sendEmptyMessage(DISAPPEAR);
//                        }else {
//                            sendEmptyMessageDelayed(FLY, 50);
//                        }
//                        break;
//                    case BOTTOM_STATUS:
//                        aiXinParams.y = (int) (aiXinParams.y + v0 + 1/2 * g);
//                        aiXinParams.x = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
//                        v0 = (int) (v0 + g);
//                        wm.updateViewLayout(aiXinView, aiXinParams);
//                        if(aiXinParams.y <= size.y/2 - 3 * objSize){
//                            sendEmptyMessage(DISAPPEAR);
//                        }else {
//                            sendEmptyMessageDelayed(FLY, 50);
//                        }
//                        break;
//                }
//                break;
//            case DISAPPEAR:
//                removeMessages(DISAPPEAR);
//                aiXinView.setAlpha((float) (aiXinView.getAlpha() - 0.2));
//                if(aiXinView.getAlpha() <= 0){
//                    aiXinView.setVisibility(View.GONE);
//                }else {
//                    sendEmptyMessageDelayed(DISAPPEAR, 50);
//                }
//                break;
//        }
//    }
//
//    public void run(){
//        int randomDelayed = new Random().nextInt(500) * 2;
//        sendEmptyMessageDelayed(FLY, randomDelayed);
//    }
}
