package com.fl.phone_pet.pojo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fl.phone_pet.R;

import java.util.Random;

public class CallMsg extends Handler {

    public static final int FLY = 30001;
    public static final int DISAPPEAR = 30002;
    public static final int SHOW = 30003;

    WindowManager wm;
    WindowManager.LayoutParams callParams;
    Point size;
    View callView;
    TextView callText;
    String callContent;
    Context ctx;
    float g = 9.8f;
    int v0 = 0;
    int count;

    public CallMsg(Context ctx, WindowManager wm, Point size, String callContent){
        this.ctx = ctx;
        this.wm = wm;
        this.size = size;
        this.callContent = callContent;
        this.callParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            callParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            callParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        callParams.format = PixelFormat.RGBA_8888; // 设置图片

        // 格式，效果为背景透明
        callParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;


        this.callView = LayoutInflater.from(ctx).inflate(R.layout.call_msg, null);
        this.callText = callView.findViewById(R.id.call_text);
        callText.setText(callContent);
        initParams();
        wm.addView(this.callView, this.callParams);
    }

    private void initParams(){
        int randomSize = new Random().nextInt(30) + 4;
        callText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, randomSize);
        //int randomRotation = (int) (Math.random() * 110 - 90);
        //callView.setRotation(randomRotation);
        //callParams.rotationAnimation = randomRotation;
        callParams.width = (int) (callText.getPaint().measureText(callText.getText().toString()));
        callParams.height = randomSize * 2 * 5;
        int randomX = (int) (Math.random() * this.size.x - this.size.x/2);
        int randomY = (int) (Math.random() * 70 - this.size.y/2);
        callParams.x = randomX;
        callParams.y = randomY;
        callView.setAlpha(1);
        callView.setVisibility(View.GONE);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what){
            case SHOW:
                callView.setVisibility(View.VISIBLE);
                break;
            case FLY:
                removeMessages(FLY);
                callParams.y = (int) (callParams.y + v0 + 1/2 * g);
                v0 = (int) (v0 + g);
                wm.updateViewLayout(callView, callParams);
                if(callView.getVisibility() != View.VISIBLE && count == 0)sendEmptyMessageDelayed(SHOW, 500);
                else if(callView.getVisibility() != View.VISIBLE && count != 0)sendEmptyMessage(SHOW);
                if(callParams.y + callParams.height/2 >= size.y/2){
                    sendEmptyMessage(DISAPPEAR);
                }else {
                    sendEmptyMessageDelayed(FLY, 50);
                }
                break;
            case DISAPPEAR:
                removeMessages(DISAPPEAR);
                callView.setAlpha((float) (callView.getAlpha() - 0.2));
                if(callView.getAlpha() <= 0){
                    if(count > 2)wm.removeView(callView);
                    else {
                        callView.setVisibility(View.GONE);
                        initParams();
                        wm.updateViewLayout(callView, callParams);
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
