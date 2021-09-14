package com.fl.phone_pet.pojo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.utils.SpeedUtils;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Boom {
    public RelativeLayout.LayoutParams boomParams;
    public View boomView;
    ImageView boomImg;
    Context ctx;
    CountDownLatch cdl;

    public Boom(Context ctx, CountDownLatch cdl, Drawable drawable){
        this.ctx = ctx;
        this.cdl = cdl;
        this.boomView = LayoutInflater.from(ctx).inflate(R.layout.prop_msg, null);
        this.boomImg = boomView.findViewById(R.id.prop_img);
        boomImg.setImageDrawable(drawable);
        initParams();
    }

    private void initParams(){
        int randomSize = new Random().nextInt(300) + 30;
        this.boomParams = new RelativeLayout.LayoutParams(randomSize, randomSize);
        int randomRotation = (int) (Math.random() * 110 - 90);
        boomView.setRotation(randomRotation);
        int randomX = (int) (Math.random() * (MyService.size.x - randomSize));
        int randomY = (int) (Math.random() * (MyService.size.y - randomSize));
        boomView.setX(randomX);
        boomView.setY(randomY);
        boomView.setAlpha(0);

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(this.boomView, "scaleX",  2.2f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(this.boomView, "scaleY", 2.2f);
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(this.boomView, "alpha", 0, 1);
        ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(this.boomView, "alpha", 1, 0);
        scaleXAnimator.setDuration((int)(1.3 * SpeedUtils.getCurrentSpeedTime()));
        scaleYAnimator.setDuration((int)(1.3 * SpeedUtils.getCurrentSpeedTime()));
        showAnimator.setDuration((int)(1.3 * SpeedUtils.getCurrentSpeedTime()));
        scaleXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        showAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        hideAnimator.setDuration(2 * SpeedUtils.getCurrentSpeedTime());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleXAnimator).with(scaleYAnimator).with(showAnimator).before(hideAnimator);
        animatorSet.setStartDelay(new Random().nextInt(900));
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cdl.countDown();
            }

        });

        animatorSet.start();

    }
}
