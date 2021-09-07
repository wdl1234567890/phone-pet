package com.fl.phone_pet.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Size;

import com.fl.phone_pet.MainActivity;
import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;
import com.fl.phone_pet.pojo.Pet;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static void checkFloatWindowPermission(Context ctx){
        if(Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(ctx)){
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("开启悬浮窗").setIcon(R.mipmap.ic_launcher).setMessage("桌宠运行必须开启悬浮窗权限（允许在其他应用上层显示），点击去设置")
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                              dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)intent.setData(Uri.parse("package:" + ctx.getPackageName()));
                            ctx.startActivity(intent);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        }
    }

    public static boolean isGroupPetsEmpty(){
        if(MyService.groupPets == null || MyService.groupPets.isEmpty())return true;
        Iterator<Map.Entry<String, List<Pet>>> it = MyService.groupPets.entrySet().iterator();
        while (it.hasNext()){
            List<Pet> petList = it.next().getValue();
            if(petList != null && !petList.isEmpty())return false;
        }
        return true;
    }

    public static List<Pet> getAllPets(){
        List<Pet> pets;
        if(MyService.groupPets != null && !MyService.groupPets.isEmpty())pets = new LinkedList<>();
        else return null;
        Set<String> keys = MyService.groupPets.keySet();
        for (String key : keys)pets.addAll(MyService.groupPets.get(key));
        return pets;
    }
}
