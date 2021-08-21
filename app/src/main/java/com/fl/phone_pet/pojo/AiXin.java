package com.fl.phone_pet.pojo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fl.phone_pet.R;
import com.fl.phone_pet.utils.SpeedUtils;

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
        switch (this.status){
            case LEFT_STATUS:
                randomX = (int) (Math.random() * 6);
                randomY = (int) (Math.random() * (this.params.height - randomSize));
                aiXinView.setRotation(90);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationX", (int)(Math.random() * (this.params.width - randomX - randomSize - 6) + 6));
                break;
            case RIGHT_STATUS:
                randomX = (int) (this.params.width - randomSize - Math.random() * 6);
                randomY = (int) (Math.random() * (this.params.height - randomSize));
                aiXinView.setRotation(-90);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationX", -(int)(Math.random() * (randomX - 6 - randomSize*17) + 6));
                break;
            case TOP_STATUS:
                randomX = (int) (Math.random() * (this.params.width - randomSize));
                randomY = (int) (Math.random() * 6);
                aiXinView.setRotation(180);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationY", (int)(Math.random() * (this.params.height - randomY - randomSize - 6) + 6));
                break;
            case BOTTOM_STATUS:
                randomX = (int) (Math.random() * (this.params.width - randomSize));
                randomY = (int) (this.params.height - randomSize - Math.random() * 6);
                aiXinView.setRotation(0);
                flyAnimator = ObjectAnimator.ofFloat(aiXinView, "translationY", -(int)(Math.random() * (randomY - 6) + 6));
                break;
        }
        aiXinView.setAlpha(1);
        aiXinView.setX(randomX);
        aiXinView.setY(randomY);
        flyAnimator.setDuration(SpeedUtils.getCurrentSpeedTime());
        hidenAnimator = ObjectAnimator.ofFloat(aiXinView,"alpha", 0);
        hidenAnimator.setInterpolator(new AnticipateInterpolator());
        hidenAnimator.setDuration((long)(0.4 * SpeedUtils.getCurrentSpeedTime()));
        animatorSet.play(hidenAnimator).after(flyAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cdl.countDown();
            }
        });
        animatorSet.setStartDelay(new Random().nextInt(1700));
        animatorSet.start();
    }
}
