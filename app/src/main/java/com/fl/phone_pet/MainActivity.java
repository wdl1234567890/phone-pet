package com.fl.phone_pet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context ct = this;
        SeekBar sizeSetting = findViewById(R.id.selectSize);
        sizeSetting.setEnabled(false);
        TextView sizeShow = findViewById(R.id.sizeShow);
        int size1 = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getInt("current_size", MyService.currentSize);
        sizeShow.setText(String.valueOf(size1));
        sizeSetting.setProgress(size1);
        SeekBar vSetting = findViewById(R.id.selectV);
        vSetting.setEnabled(false);
        TextView vShow = findViewById(R.id.vShow);
        int speed1 = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getInt("speed", MyService.speed);
        vShow.setText(String.valueOf(speed1));
        vSetting.setProgress(speed1);
        Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(serviceMessenger == null){
                        myService = new Intent(ct, MyService.class);
                        myService.putExtra("clientMessenger", clientMessenger);
                        bindService(myService, sc, Context.BIND_AUTO_CREATE);
                        sizeSetting.setEnabled(true);
                        vSetting.setEnabled(true);
                        for(FloatingActionButton button : buttons.values()){
                            button.setEnabled(true);
                        }
                    }

                }else{
                    if(serviceMessenger != null){
                        unbindService(sc);
                        sizeSetting.setEnabled(false);
                        vSetting.setEnabled(false);
                        for(FloatingActionButton button : buttons.values()){
                            button.setEnabled(false);
                        }
                    }

                }
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
        vSetting.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

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
        initButtons();
        bindButtonsEvent();
    }


//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.wz_func_panel_layout);
//    }

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