package com.fl.phone_pet.pojo;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fl.phone_pet.R;

import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;

public class PropMsg extends Handler {
    public static final int FLY = 30001;
    public static final int DISAPPEAR = 30002;
    public static final int SHOW = 30003;

    WindowManager wm;
    WindowManager.LayoutParams propParams;
    Point size;
    View propView;
    ImageView propImg;
    Integer propContent;
    Context ctx;
    float g = 9.8f;
    int v0 = 0;
    int count;

    public PropMsg(Context ctx, WindowManager wm, Point size, Integer propContent){
        this.ctx = ctx;
        this.wm = wm;
        this.size = size;
        this.propContent = propContent;
        this.propParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            propParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            propParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        propParams.format = PixelFormat.RGBA_8888; // 设置图片

        // 格式，效果为背景透明
        propParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;


        this.propView = LayoutInflater.from(ctx).inflate(R.layout.prop_msg, null);
        this.propImg = propView.findViewById(R.id.prop_img);
        propImg.setImageResource(propContent);
        initParams();
        wm.addView(this.propView, this.propParams);
    }

    private void initParams(){
        int randomSize = new Random().nextInt(100) + 100;
        int randomRotation = (int) (Math.random() * 110 - 90);
        propView.setRotation(randomRotation);
        //callParams.rotationAnimation = randomRotation;
        propParams.width = randomSize;
        propParams.height = randomSize;
        int randomX = (int) (Math.random() * this.size.x - this.size.x/2);
        int randomY = (int) (Math.random() * 70 - this.size.y/2);
        propParams.x = randomX;
        propParams.y = randomY;
        propView.setAlpha(1);
        propView.setVisibility(View.GONE);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what){
            case SHOW:
                propView.setVisibility(View.VISIBLE);
                break;
            case FLY:
                removeMessages(FLY);
                propParams.y = (int) (propParams.y + v0 + 1/2 * g);
                v0 = (int) (v0 + g);
                wm.updateViewLayout(propView, propParams);
                if(propView.getVisibility() != View.VISIBLE && count == 0)sendEmptyMessageDelayed(SHOW, 500);
                else if(propView.getVisibility() != View.VISIBLE && count != 0)sendEmptyMessage(SHOW);
                if(propParams.y + propParams.height/2 >= size.y/2){
                    sendEmptyMessage(DISAPPEAR);
                }else {
                    sendEmptyMessageDelayed(FLY, 50);
                }
                break;
            case DISAPPEAR:
                removeMessages(DISAPPEAR);
                propView.setAlpha((float) (propView.getAlpha() - 0.2));
                if(propView.getAlpha() <= 0){
                    if(count > 2)wm.removeView(propView);
                    else {
                        propView.setVisibility(View.GONE);
                        initParams();
                        wm.updateViewLayout(propView, propParams);
                        sendEmptyMessage(FLY);
                        count++;
                    }
                }else {
                    sendEmptyMessageDelayed(DISAPPEAR, 50);
                }
                break;
        }
    }

    public void run(){
        int randomDelayed = new Random().nextInt(500) * 2;
        sendEmptyMessageDelayed(FLY, randomDelayed);
    }
}
