package com.fl.phone_pet.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Size;

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
}
