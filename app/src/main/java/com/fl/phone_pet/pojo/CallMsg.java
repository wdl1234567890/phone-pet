package com.fl.phone_pet.pojo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.utils.SpeedUtils;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class CallMsg{

    public RelativeLayout.LayoutParams callParams;
    Point size;
    public View callView;
    TextView callText;
    String callContent;
    Context ctx;
    CountDownLatch cdl;
    String name;

    public CallMsg(Context ctx, Point size, String callContent, CountDownLatch cdl, String name){
        this.ctx = ctx;
        this.size = size;
        this.callContent = callContent;
        this.cdl = cdl;
        this.callView = LayoutInflater.from(ctx).inflate(R.layout.call_msg, null);
        this.callText = callView.findViewById(R.id.call_text);
        this.name = name;
        callText.setText(callContent);
        initParams();
    }

    private void initParams(){
        int randomSize = new Random().nextInt(30) + 4;
        callText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, randomSize);
        int resId = -1;
        if(name.equals(MyService.WZ)){
            switch (new Random().nextInt(3)){
                case 0:
                    resId = ctx.getResources().getIdentifier(MyService.AX + "_call", "color", ctx.getPackageName());
                    break;
                case 1:
                    resId = ctx.getResources().getIdentifier(MyService.LW + "_call", "color", ctx.getPackageName());
                    break;
                case 2:
                    resId = ctx.getResources().getIdentifier(MyService.WZ + "_call", "color", ctx.getPackageName());
                    break;
            }
        }else{
            resId = ctx.getResources().getIdentifier(name + "_call", "color", ctx.getPackageName());
        }
        callText.setTextColor(ctx.getResources().getColor(resId));
        int textWidth = (int) (callText.getPaint().measureText(callText.getText().toString()));
        this.callParams = new RelativeLayout.LayoutParams(textWidth, randomSize * 10);
        int randomX = (int) (Math.random() * (this.size.x - textWidth));
        int randomY = (int) (Math.random() * 70);
        callView.setAlpha(1);
        this.callView.setX(randomX);
        this.callView.setY(randomY);
        float downY = -randomY + this.size.y - callParams.height;


        ObjectAnimator downAnimator = ObjectAnimator.ofFloat(this.callView, "translationY", 0, downY);
        ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(this.callView, "alpha", 1, 0);
        downAnimator.setDuration(5 * SpeedUtils.getCurrentSpeedTime());
        downAnimator.setInterpolator(new BounceInterpolator());
        hideAnimator.setDuration(2 * SpeedUtils.getCurrentSpeedTime());
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
