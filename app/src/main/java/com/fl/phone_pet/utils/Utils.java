package com.fl.phone_pet.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;

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

    public static void voice(String resId) {
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
            mp1.setDataSource(resId);
            mp1.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MyService.mp.get(1).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp1) {
                mp1.stop();
                mp1.release();
                MyService.mp.put(1, null);
            }
        });
    }
}
