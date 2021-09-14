package com.fl.phone_pet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.fl.phone_pet.utils.SensorUtils;
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
    public static Messenger serviceMessenger;

    public static final int ADD_PET = 20001;
    public static final int REDUCE_PET = 20002;
    public static final int SIZE_CHANGE = 20003;
    public static final int BUTTON_DISENABLED = 20004;
    public static final int DISCONNECTION = 20005;
    public static final int SPEED_CHANGE = 20006;
    public static final int FREQUEST_CHANGE = 20007;
    public static final int STATUS_BAR_CHANGE = 20008;
    public static final int SWITCH_ENABLE = 20009;
    public static final int TOUCH_CHANGE = 20010;
    public static final int KETBOARD_CHANGE = 20011;
    public static final int CLOSE_GSENSOR = 20012;

    List<Handler> versionTh;
    public static Map<String, FloatingActionButton> buttons;

    public static SeekBar speedSetting;

    public ServiceConnection sc = new ServiceConnection(){

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
            if(msg.what == SWITCH_ENABLE){
                Switch switch1 = (Switch)msg.obj;
                switch1.setEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(versionTh != null && !versionTh.isEmpty()){
            versionTh.get(0).getLooper().quit();
            versionTh.clear();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(versionTh != null && !versionTh.isEmpty()){
            versionTh.get(0).getLooper().quit();
            versionTh.clear();
            Log.i("------stop-----","stop");
        }
        Log.i("------stop1-----","stop1");

    }


    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkFloatWindowPermission(this);
        TextView version = findViewById(R.id.version);
        versionTh = VersionUpdate.checkVersionUpdate(this, new VersionUpdate.MyConsumer() {
            @SuppressLint({"ResourceAsColor", "ResourceType"})
            @Override
            public void consume(Boolean isUpdate) {
                try{
                    if(isUpdate){
                        version.setTextColor(Color.rgb(116,0,0));
                    }
                    else{
                        version.setTextColor(Color.rgb(102,102,102));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

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
    }



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
            finish();
            return;
        }

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

        speedSetting = findViewById(R.id.selectSpeed);
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

        CheckBox checkedStatusBar = findViewById(R.id.checkStatusBar);
        checkedStatusBar.setEnabled(false);
        boolean isCheckStatusBar = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("check_status_bar", false);
        checkedStatusBar.setChecked(isCheckStatusBar);

        CheckBox checkedNotTouch = findViewById(R.id.checkNotTouch);
        checkedNotTouch.setEnabled(false);
        boolean isCheckNotTouch = !getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_enable_touch", MyService.isEnableTouch);
        checkedNotTouch.setChecked(isCheckNotTouch);

        CheckBox checkedKeyboard = findViewById(R.id.checkKeyboard);
        checkedKeyboard.setEnabled(false);
        boolean isCheckKeyboard = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_show_keyboard", MyService.isKeyboardShow);
        checkedKeyboard.setChecked(isCheckKeyboard);

        CheckBox checkedVibrator = findViewById(R.id.closeVibrator);
        checkedVibrator.setEnabled(false);
        boolean isCheckVibrator = !getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_vibrator", MyService.isVibrator);
        checkedVibrator.setChecked(isCheckVibrator);

        CheckBox checkedGSensor = findViewById(R.id.checkGSensor);
        checkedGSensor.setEnabled(false);
        boolean isCheckGSensor = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_gsensor", MyService.isGSensorEnabled);
        checkedGSensor.setChecked(isCheckGSensor);

        CheckBox checkedLSensor = findViewById(R.id.checkLSensor);
        checkedLSensor.setEnabled(false);
        boolean isCheckLSensor = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_lsensor", MyService.isLSensorEnabled);
        checkedLSensor.setChecked(isCheckLSensor);

        CheckBox checkedSDSensor = findViewById(R.id.checkSDSensor);
        checkedSDSensor.setEnabled(false);
        boolean isCheckSDSensor = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_sdsensor", MyService.isSDSensorEnabled);
        checkedSDSensor.setChecked(isCheckSDSensor);

//        CheckBox checkedDSensor = findViewById(R.id.checkDSensor);
//        checkedDSensor.setEnabled(false);
//        boolean isCheckDSensor = getSharedPreferences("pet_store",
//                Context.MODE_PRIVATE).getBoolean("is_dsensor", MyService.isDSensorEnabled);
//        checkedDSensor.setChecked(isCheckDSensor);

        CheckBox checkedPSensor = findViewById(R.id.checkPSensor);
        checkedPSensor.setEnabled(false);
        boolean isCheckPSensor = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_psensor", MyService.isPSensorEnabled);
        checkedPSensor.setChecked(isCheckPSensor);

        CheckBox checkedCloseWindow = findViewById(R.id.checkCloseWindow);
        checkedCloseWindow.setEnabled(false);
        boolean isCloseWindow = getSharedPreferences("pet_store",
                Context.MODE_PRIVATE).getBoolean("is_close_window", MyService.isCloseWindowEnabled);
        checkedCloseWindow.setChecked(isCloseWindow);

        Button dontCloseWindowButton = findViewById(R.id.dontCloseWindowButton);
        dontCloseWindowButton.setEnabled(false);

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
                        if(!checkedPSensor.isChecked())speedSetting.setEnabled(true);
                        frequestSetting.setEnabled(true);
                        checkedStatusBar.setEnabled(true);
                        if(!checkedKeyboard.isChecked())checkedNotTouch.setEnabled(true);
                        if(!checkedNotTouch.isChecked())checkedKeyboard.setEnabled(true);
                        checkedVibrator.setEnabled(true);
                        boolean isEnableG = SensorUtils.isSensorAble(ctx, Sensor.TYPE_ACCELEROMETER);
                        if(isEnableG && !checkedLSensor.isChecked())checkedGSensor.setEnabled(true);
                        if(SensorUtils.isSensorAble(ctx, Sensor.TYPE_LIGHT) && !checkedGSensor.isChecked())checkedLSensor.setEnabled(true);
                        if(isEnableG && !checkedGSensor.isChecked())checkedSDSensor.setEnabled(true);
//                        if(SensorUtils.isSensorAble(ctx, Sensor.TYPE_PROXIMITY) && !checkedLSensor.isChecked())checkedDSensor.setEnabled(true);
                        if(isEnableG)checkedPSensor.setEnabled(true);
                        checkedCloseWindow.setEnabled(true);
                        dontCloseWindowButton.setEnabled(true);

                        for(FloatingActionButton button : buttons.values()){
                            button.setEnabled(true);
                        }
                        switch1.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(800);
                                    Message message = new Message();
                                    message.what = SWITCH_ENABLE;
                                    message.obj = switch1;
                                    clientMessenger.send(message);
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
                        checkedStatusBar.setEnabled(false);
                        checkedNotTouch.setEnabled(false);
                        checkedKeyboard.setEnabled(false);
                        checkedVibrator.setEnabled(false);
                        checkedGSensor.setEnabled(false);
                        checkedLSensor.setEnabled(false);
                        checkedSDSensor.setEnabled(false);
//                        checkedDSensor.setEnabled(false);
                        checkedPSensor.setEnabled(false);
                        checkedCloseWindow.setEnabled(false);
                        dontCloseWindowButton.setEnabled(false);
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
        checkedStatusBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        checkedNotTouch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isEnableTouch = !b;
                checkedKeyboard.setEnabled(!b);
                if(serviceMessenger != null){
                    Message msg = new Message();
                    msg.what = TOUCH_CHANGE;
                    msg.replyTo = clientMessenger;
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        checkedKeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isKeyboardShow = b;
                checkedNotTouch.setEnabled(!b);
                if(serviceMessenger != null){
                    Message msg = new Message();
                    msg.what = KETBOARD_CHANGE;
                    msg.replyTo = clientMessenger;
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        checkedVibrator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isVibrator = !b;
            }
        });
        checkedGSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isGSensorEnabled = b;
                checkedLSensor.setEnabled(!b);
                checkedSDSensor.setEnabled(!b);
                if(b){
                    SensorUtils.registerGSensor(ctx);
                }else{
                    SensorUtils.unregisterGSensor();
                    if(serviceMessenger != null){
                        Message msg = new Message();
                        msg.what = CLOSE_GSENSOR;
                        msg.replyTo = clientMessenger;
                        try {
                            serviceMessenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        checkedLSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isLSensorEnabled = b;
                checkedGSensor.setEnabled(!b);
//                checkedDSensor.setEnabled(!b);
                if(b)SensorUtils.registerLSensor(ctx);
                else SensorUtils.unregisterLSensor();
            }
        });
        checkedSDSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isSDSensorEnabled = b;
                checkedGSensor.setEnabled(!b);
                if(b)SensorUtils.registerSDSensor(ctx);
                else SensorUtils.unregisterSDSensor();
            }
        });
//        checkedDSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                MyService.isDSensorEnabled = b;
//                checkedLSensor.setEnabled(!b);
//                if(b)SensorUtils.registerDSensor(ctx);
//                else SensorUtils.unregisterDSensor();
//            }
//        });
        checkedPSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isPSensorEnabled = b;
                if(b)SensorUtils.registerPSensor(ctx);
                else SensorUtils.unregisterPSensor();
            }
        });

        checkedCloseWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyService.isCloseWindowEnabled = b;
            }
        });

        dontCloseWindowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = new EditText(ctx);
                //editText.setMaxLines(5);
                editText.setSingleLine(false);
                editText.setWidth(100);
                editText.setText(MyService.dontCloseWindowName);
                editText.setHeight(500);
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("输入不可关闭的窗口包名").setIcon(R.mipmap.ic_launcher).setView(editText).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String content = editText.getText().toString();
                        if(content == null)content = "";
                        MyService.dontCloseWindowName = content.trim();

                    }
                }).show();
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