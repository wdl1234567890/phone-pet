package com.fl.phone_pet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.fl.phone_pet.utils.Utils;
import com.fl.phone_pet.utils.VersionUpdate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Messenger clientMessenger = new Messenger(new ServiceMsgHandler());
    private Intent myService;
    private Messenger serviceMessenger;

    public static final int ADD_PET = 20001;
    public static final int REDUCE_PET = 20002;
    public static final int SIZE_CHANGE = 20003;
    public static final int BUTTON_DISENABLED = 20004;
    public static final int DISCONNECTION = 20005;
    public static final int SPEED_CHANGE = 20006;
    public static final int FREQUEST_CHANGE = 20007;
    public static final int STATUS_BAR_CHANGE = 20008;


    List<Handler> versionTh;
    Map<String, FloatingActionButton> buttons;

    private ServiceConnection sc = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            serviceMessenger = null;
        }
    };

    private class ServiceMsgHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == DISCONNECTION) serviceMessenger = null;
            if(msg.what == BUTTON_DISENABLED){
                buttons.get("reduce_" + msg.obj).setEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(versionTh != null && !versionTh.isEmpty()){
            versionTh.get(0).getLooper().quit();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        try{
//            getSupportActionBar().hide();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        setContentView(R.layout.activity_main);
        Context ctx = this;

        SeekBar sizeSetting = findViewById(R.id.selectSize);
        sizeSetting.setEnabled(false);
        TextView sizeShow = findViewById(R.id.sizeShow);
        int size1 = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getInt("current_size", MyService.currentSize);
        sizeShow.setText(String.valueOf(size1));
        sizeSetting.setProgress(size1);

        SeekBar speedSetting = findViewById(R.id.selectSpeed);
        speedSetting.setEnabled(false);
        TextView vShow = findViewById(R.id.vShow);
        int speed1 = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getInt("speed", MyService.speed);
        vShow.setText(String.valueOf(speed1));
        speedSetting.setProgress(speed1);

        SeekBar frequestSetting = findViewById(R.id.selectFrequest);
        frequestSetting.setEnabled(false);
        TextView fShow = findViewById(R.id.fShow);
        int frequest = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getInt("frequest", MyService.frequest);
        fShow.setText(String.valueOf(frequest));
        frequestSetting.setProgress(frequest);

        CheckBox checkedBox = findViewById(R.id.checkStatusBar);
        checkedBox.setEnabled(false);
        boolean checkStatusBar = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("check_status_bar", false);
        checkedBox.setChecked(checkStatusBar);

        Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(serviceMessenger == null){
                        myService = new Intent(ctx, MyService.class);
                        myService.putExtra("clientMessenger", clientMessenger);
                        bindService(myService, sc, Context.BIND_AUTO_CREATE);
                        sizeSetting.setEnabled(true);
                        speedSetting.setEnabled(true);
                        frequestSetting.setEnabled(true);
                        checkedBox.setEnabled(true);
                        for(FloatingActionButton button : buttons.values()){
                            button.setEnabled(true);
                        }
                        switch1.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(800);
                                    switch1.setEnabled(true);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                }else{
                    if(serviceMessenger != null){
                        unbindService(sc);
                        sizeSetting.setEnabled(false);
                        speedSetting.setEnabled(false);
                        frequestSetting.setEnabled(false);
                        checkedBox.setEnabled(false);
                        for(FloatingActionButton button : buttons.values()){
                            button.setEnabled(false);
                        }
                    }

                }
            }
        });

        TextView version = findViewById(R.id.version);
        versionTh = VersionUpdate.checkVersionUpdate((Activity)ctx, new VersionUpdate.MyConsumer() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void consume(Boolean isUpdate) {
                if(isUpdate)version.setTextColor(R.color.purple_200);
                else  version.setTextColor(R.color.white);
            }
        });
        try{
            version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }catch(Exception e){
            version.setText("0.0.0");
            e.printStackTrace();
        }
        version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VersionUpdate.showDialogUpdate();
            }
        });

        sizeSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sizeShow.setText(String.valueOf(progress));
                if(serviceMessenger != null){
                    Message msg = new Message();
                    msg.what = SIZE_CHANGE;
                    msg.arg1 = progress;
                    msg.replyTo = clientMessenger;
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        speedSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vShow.setText(String.valueOf(progress));
                if(serviceMessenger != null){
                    Message msg = new Message();
                    msg.what = SPEED_CHANGE;
                    msg.arg1 = progress;
                    msg.replyTo = clientMessenger;
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        frequestSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                fShow.setText(String.valueOf(progress));
                if(serviceMessenger != null){
                    Message msg = new Message();
                    msg.what = FREQUEST_CHANGE;
                    msg.arg1 = progress;
                    msg.replyTo = clientMessenger;
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        checkedBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    MyService.oldStatusBarHeight = 0;
                }else{
                    MyService.oldStatusBarHeight = Utils.getStatusBarHeight(ctx);
                }
                MyService.statusBarHeight = MyService.oldStatusBarHeight;
                if(serviceMessenger != null){
                    Message msg = new Message();
                    msg.what = STATUS_BAR_CHANGE;
                    msg.replyTo = clientMessenger;
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        initButtons();
        bindButtonsEvent();
    }

    private void initButtons(){
        if(buttons == null)buttons = new HashMap<>();
        buttons.put("reduce_ax", findViewById(R.id.reduce_ax));
        buttons.put("add_ax", findViewById(R.id.add_ax));
        buttons.put("add_lw", findViewById(R.id.add_lw));
        buttons.put("reduce_lw", findViewById(R.id.reduce_lw));
        buttons.put("add_wz", findViewById(R.id.add_wz));
        buttons.put("reduce_wz", findViewById(R.id.reduce_wz));

        for(FloatingActionButton button : buttons.values()){
            button.setEnabled(false);
        }
    }

    private void bindButtonsEvent(){
        for(FloatingActionButton button : buttons.values()){
            button.setOnClickListener(addOrReduceListener);
        }
    }

    private View.OnClickListener addOrReduceListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(serviceMessenger == null)return;
            String tag = (String)v.getTag();
            String[] datas = tag.split("_");
            Message msg = new Message();
            msg.what = datas[0].equals("add") ? ADD_PET : REDUCE_PET;
            msg.obj = datas[1];
            if(datas[0].equals("add") && !buttons.get("reduce_" + datas[1]).isEnabled())
                buttons.get("reduce_" + datas[1]).setEnabled(true);
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

}