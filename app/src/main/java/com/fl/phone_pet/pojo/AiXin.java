package com.fl.phone_pet.pojo;

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

import com.fl.phone_pet.R;

import java.util.Random;

public class AiXin  extends Handler {
    public static final int FLY = 40001;
    public static final int DISAPPEAR = 40002;
    public static final int LEFT_STATUS = 40003;
    public static final int RIGHT_STATUS = 40004;
    public static final int TOP_STATUS = 40005;
    public static final int BOTTOM_STATUS = 40006;

    WindowManager wm;
    WindowManager.LayoutParams aiXinParams;
    Point size;
    View aiXinView;
    ImageView aiXinImg;
    Integer aiXinContent;
    Context ctx;
    int centralPoint;
    int objSize;
    int status;
    float g = 9.8f;
    int v0 = 0;

    public AiXin(Context ctx, WindowManager wm, Point size, Integer aiXinContent, int centralPoint, int objSize, int status){
        this.ctx = ctx;
        this.wm = wm;
        this.size = size;
        this.aiXinContent = aiXinContent;
        this.aiXinParams = new WindowManager.LayoutParams();
        this.centralPoint = centralPoint;
        this.objSize = objSize;
        this.status = status;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            aiXinParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            aiXinParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        aiXinParams.format = PixelFormat.RGBA_8888; // 设置图片

        // 格式，效果为背景透明
        aiXinParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        switch (status){
            case RIGHT_STATUS:
            case BOTTOM_STATUS:
                v0  = -v0;
                g = -g;
                break;
        }

        this.aiXinView = LayoutInflater.from(ctx).inflate(R.layout.prop_msg, null);
        this.aiXinImg = aiXinView.findViewById(R.id.prop_img);
        aiXinImg.setImageResource(aiXinContent);
        initParams();
        wm.addView(this.aiXinView, this.aiXinParams);
    }

    private void initParams(){
        int randomSize = (int) (Math.random() * (objSize/2.0));
        aiXinParams.width = randomSize;
        aiXinParams.height = randomSize;
        int randomX = 0;
        int randomY = 0;
        switch (this.status){
            case LEFT_STATUS:
                randomX = (int) (Math.random() * 15 - (size.x/2) + objSize);
                randomY = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
                aiXinView.setRotation(90);
                break;
            case RIGHT_STATUS:
                randomX = (int) (-Math.random() * 15 + (size.x/2) - objSize);
                randomY = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
                aiXinView.setRotation(-90);
                break;
            case TOP_STATUS:
                randomX = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
                randomY = (int) (int) (Math.random() * 15 - (size.y/2) + objSize);
                aiXinView.setRotation(180);
                break;
            case BOTTOM_STATUS:
                randomX = (int) (Math.random() * (objSize - 8) + centralPoint - objSize/2.0);
                randomY = (int) (int) (-Math.random() * 15 + (size.y/2) - objSize);
                aiXinView.setRotation(0);
                break;
        }

        aiXinParams.x = randomX;
        aiXinParams.y = randomY;
        aiXinView.setAlpha(1);
        aiXinView.setVisibility(View.GONE);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what){
            case FLY:
                removeMessages(FLY);
                if(aiXinView.getVisibility() != View.VISIBLE)aiXinView.setVisibility(View.VISIBLE);
                switch (status){
                    case LEFT_STATUS:
                        aiXinParams.x = (int) (aiXinParams.x + v0 + 1/2 * g);
                        aiXinParams.y = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
                        v0 = (int) (v0 + g);
                        wm.updateViewLayout(aiXinView, aiXinParams);
                        if(aiXinParams.x >= -size.x/2 + 3 * objSize){
                            sendEmptyMessage(DISAPPEAR);
                        }else {
                            sendEmptyMessageDelayed(FLY, 50);
                        }
                        break;
                    case RIGHT_STATUS:
                        aiXinParams.x = (int) (aiXinParams.x + v0 + 1/2 * g);
                        aiXinParams.y = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
                        v0 = (int) (v0 + g);
                        wm.updateViewLayout(aiXinView, aiXinParams);
                        if(aiXinParams.x <= size.x/2 - 3 * objSize){
                            sendEmptyMessage(DISAPPEAR);
                        }else {
                            sendEmptyMessageDelayed(FLY, 50);
                        }
                        break;
                    case TOP_STATUS:
                        aiXinParams.y = (int) (aiXinParams.y + v0 + 1/2 * g);
                        aiXinParams.x = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
                        v0 = (int) (v0 + g);
                        wm.updateViewLayout(aiXinView, aiXinParams);
                        if(aiXinParams.y >= -size.y/2 + 3 * objSize){
                            sendEmptyMessage(DISAPPEAR);
                        }else {
                            sendEmptyMessageDelayed(FLY, 50);
                        }
                        break;
                    case BOTTOM_STATUS:
                        aiXinParams.y = (int) (aiXinParams.y + v0 + 1/2 * g);
                        aiXinParams.x = (int) (Math.random() * (objSize - 16) + centralPoint - objSize/2.0);
                        v0 = (int) (v0 + g);
                        wm.updateViewLayout(aiXinView, aiXinParams);
                        if(aiXinParams.y <= size.y/2 - 3 * objSize){
                            sendEmptyMessage(DISAPPEAR);
                        }else {
                            sendEmptyMessageDelayed(FLY, 50);
                        }
                        break;
                }
                break;
            case DISAPPEAR:
                removeMessages(DISAPPEAR);
                aiXinView.setAlpha((float) (aiXinView.getAlpha() - 0.2));
                if(aiXinView.getAlpha() <= 0){
                    aiXinView.setVisibility(View.GONE);
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
