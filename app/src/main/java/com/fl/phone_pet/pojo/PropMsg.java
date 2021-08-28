package com.fl.phone_pet.pojo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.utils.SpeedUtils;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class PropMsg{

    public RelativeLayout.LayoutParams propParams;
    public View propView;
    ImageView propImg;
    Integer propContent;
    Context ctx;
    CountDownLatch cdl;
    int defindSize = -1;

    public PropMsg(Context ctx, Integer propContent, CountDownLatch cdl, int defindSize){
        this.ctx = ctx;
        this.propContent = propContent;
        this.cdl = cdl;
        this.defindSize = defindSize;
        this.propView = LayoutInflater.from(ctx).inflate(R.layout.prop_msg, null);
        this.propImg = propView.findViewById(R.id.prop_img);
        propImg.setImageResource(propContent);
        initParams();
    }

    private void initParams(){
        int randomSize = defindSize == -1 ? new Random().nextInt(100) + 100 : defindSize;
        this.propParams = new RelativeLayout.LayoutParams(randomSize, randomSize);
        int randomRotation = (int) (Math.random() * 110 - 90);
        propView.setRotation(randomRotation);
        int randomX = (int) (Math.random() * (MyService.size.x - randomSize));
        int randomY = (int) (Math.random() * 70);
        propView.setX(randomX);
        propView.setY(randomY);
        propView.setAlpha(1);
        float downY = -randomY + MyService.size.y - propParams.height;
        ObjectAnimator downAnimator = ObjectAnimator.ofFloat(this.propView, "translationY", 0, downY);
        ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(this.propView, "alpha", 1, 0);
        downAnimator.setDuration(13 * SpeedUtils.getCurrentSpeedTime());
        downAnimator.setInterpolator(new BounceInterpolator());
        hideAnimator.setDuration(6 * SpeedUtils.getCurrentSpeedTime());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(hideAnimator).after(downAnimator);
        animatorSet.setStartDelay(new Random().nextInt(1200) * 2);
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
