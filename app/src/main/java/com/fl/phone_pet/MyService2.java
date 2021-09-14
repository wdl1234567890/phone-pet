package com.fl.phone_pet;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.fl.phone_pet.utils.Utils;

public class MyService2 extends AccessibilityService {

    public static AccessibilityService as;

    public MyService2() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        as = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        switch (accessibilityEvent.getEventType()){
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String packageName = accessibilityEvent.getPackageName() == null ? "" : accessibilityEvent.getPackageName().toString();
                String text = accessibilityEvent.getText() == null || accessibilityEvent.getText().size() == 0? "" : accessibilityEvent.getText().get(0).toString();
//                Log.i("------text--------", text);

                as = this;

//                if(text.equals(""))return;
//                if(Utils.getHomePackageNames(this).contains(packageName)){
//                    as = null;
//                    Log.i("------home--------", text);
//                    return;
//                }
//                if("温周桌宠".equals(text) || "输入不可关闭的窗口包名".equals(text)){
//                    as = null;
//                    return;
//                }
                if(MyService.dontCloseWindowName != null && !MyService.dontCloseWindowName.trim().equals("")){
                    String[] rawDatas = MyService.dontCloseWindowName.split(",");
                    for(String pn : rawDatas)if(pn.trim().equals(text))as= null;
                }

                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        as = null;
    }
}