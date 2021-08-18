package com.fl.phone_pet.com.fl.phone_pet.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

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
}
