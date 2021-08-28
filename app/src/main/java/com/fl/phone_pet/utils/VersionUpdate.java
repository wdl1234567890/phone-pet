package com.fl.phone_pet.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fl.phone_pet.MainActivity;
import com.fl.phone_pet.MyService;
import com.fl.phone_pet.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VersionUpdate {
    public static String versionUrlBase = "https://app-version-fl-wdl.oss-cn-shenzhen.aliyuncs.com/";
    public static JSONObject versinJson;
    public static Activity ctx1;
    public static MyConsumer consumer1;

    public static List<Handler> checkVersionUpdate(Activity ctx, MyConsumer consumer){
        List<Handler> handler = new LinkedList<>();
        ctx1 = ctx;
        consumer1 = consumer;
        handleSSLHandshake();
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Handler handler1 = new VersionCheckHandle();
                    handler.add(handler1);
                    handler1.sendEmptyMessage(1);
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        th.start();
        return handler;
    }

    private static class VersionCheckHandle extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try{
                GetServerJson();
                int versionCode = 0;
                try {
                    versionCode = versinJson.getInt("versionCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ctx1.getPackageManager().getPackageInfo(ctx1.getPackageName(), 0).versionCode < versionCode) {
                    consumer1.consume(true);
                }else{
                    consumer1.consume(false);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            sendEmptyMessageDelayed(1, 1000);
        }
    }

    public interface MyConsumer{
        void consume(Boolean isUpdate);
    }

    private static JSONObject GetServerJson() {
        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            infoUrl = new URL(versionUrlBase + "version.json");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                String json = strber.substring(start, end + 1);
                if (json != null) {
                    try {
                        versinJson = new JSONObject(json);
                        return versinJson;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void showDialogUpdate() {

        if(versinJson == null)return;
        int versionCode = 0;
        String versionName = "";
        String[] updateLog = null;
        String apkSize="";
        String Url = "";

        try {
            versionCode = versinJson.getInt("versionCode");
            versionName = versinJson.getString("versionName");
            updateLog = versinJson.getString("updateLog").split("。");
            apkSize = versinJson.getString("apkSize");
            Url = versionUrlBase + versinJson.getString("apkUrl");
            if(ctx1.getPackageManager().getPackageInfo(ctx1.getPackageName(), 0).versionCode < versionCode){
                String message = "新版本大小："+apkSize+"\n更新功能：";
                for (int newFunc = 0; newFunc < updateLog.length; newFunc++)message += ("\n" + updateLog[newFunc]);
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx1);
                final String ApkUrl = Url;
                builder.setTitle("是否升级到"+versionName+"版本？").setIcon(R.mipmap.ic_launcher_round).setMessage(message)
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse(ApkUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                ctx1.startActivity(intent);
                                ctx1.getSharedPreferences("pet_store", Context.MODE_PRIVATE).edit()
                                        .putInt("current_size", MyService.currentSize)
                                        .putInt("speed", MyService.speed)
                                        .putInt("frequest", MyService.frequest)
                                        .putBoolean("check_status_bar", MyService.statusBarHeight == 0 ? true : false)
                                        .commit();
                                if(((MainActivity)ctx1).serviceMessenger != null)ctx1.unbindService(((MainActivity)ctx1).sc);
                                ctx1.releaseInstance();
                                ctx1.finish(); //销毁
                            }
                        }).setNegativeButton("取消", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else {
                Toast.makeText(ctx1, "当前已是最新版本", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            
    }



}
