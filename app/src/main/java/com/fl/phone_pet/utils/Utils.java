package com.fl.phone_pet.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Size;

import com.fl.phone_pet.MyService;

import java.io.InputStream;

public class Utils {
    public static Drawable assets2Drawable(Context ctx, String fileName){
        InputStream open = null;
        Drawable drawable = null;
        try {
            open = ctx.getAssets().open(fileName);
            drawable = Drawable.createFromStream(open, null);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(open != null){
                    open.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return drawable;
    }
    public static int getWindowDivisionArg(int height){
        for(int k = 40; k <= height; k++){
            if(height % k == 0)return k;
        }
        for(int k = 40; k <= height; k++){
            if(height % k == 1)return k;
        }
        return height;
    }
    public static int getStatusBarHeight(Context ctx){
        int statusBarHeight = 0;
        int resId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resId > 0)statusBarHeight = ctx.getResources().getDimensionPixelSize(resId);
        return statusBarHeight;
    }

    public static boolean voice(Context ctx, String fileName) {
        if (MyService.mp.get(1) != null) {
            if (MyService.mp.get(1).isPlaying()) MyService.mp.get(1).stop();
            MyService.mp.get(1).release();
        }
        MediaPlayer mp1 = new MediaPlayer();
        mp1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        try {
            MyService.mp.put(1, mp1);
            AssetFileDescriptor afd = ctx.getAssets().openFd(fileName);
            if(afd == null)return false;
            mp1.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp1.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        MyService.mp.get(1).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                mp1.stop();
                mp1.release();
                MyService.mp.put(1, null);
            }
        });
        return true;
    }

    public static int getNormalFlags(){
        int flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
        if(MyService.isKeyboardShow)flags = flags | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

//        return int currentFlags = (Integer) params.getClass().getField("privateFlags").get(params);
//        params.getClass().getField("privateFlags").set(params, currentFlags|0x00000040);
        return flags;
    }
}
