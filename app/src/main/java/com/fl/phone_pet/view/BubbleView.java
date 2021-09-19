package com.fl.phone_pet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.fl.phone_pet.R;

import java.util.Arrays;

public class BubbleView extends View {

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;
    public static int count = (WIDTH + 1) * (HEIGHT + 1);
    public float[] srcDatas = new float[count * 2];
    public float[] vertsDatas = new float[count * 2];
    public int K = 100000;
    Bitmap img;
    int resId;

    public BubbleView(Context context) {
        this(context, R.drawable.qipao1);
    }

    public BubbleView(Context context, int resId) {
        super(context);
        setFocusable(true);
        this.resId = resId;
        setBackgroundColor(Color.BLUE);
    }

    private void setImageResourceId(int resId){
        img = BitmapFactory.decodeResource(getResources(), resId);
        int width = getWidth();
        int height = getHeight();
        float scaleWidth = (width * 1f) / img.getWidth();
        float scaleHeight = (height * 1f) / img.getHeight();
        Matrix m = new Matrix();
        m.postScale(scaleWidth, scaleHeight);
        img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), m,true);
    }

    private void setXY(float[] arrary, int index, float x, float y){
        arrary[index * 2 + 0] = x;
        arrary[index * 2 + 1] = y;
    }

    private void init(){
        float width = img.getWidth();
        float height = img.getHeight();
        float currentX;
        float currentY;
        int index = 0;

        for(int i = 0; i <= HEIGHT; i++){
            currentY = height * ((i * 1f) / HEIGHT);
            for(int j = 0; j <= WIDTH; j++){
                currentX = width * ((j * 1f) / WIDTH);
                setXY(srcDatas, index, currentX, currentY);
                setXY(vertsDatas, index, currentX, currentY);
                index++;
            }
        }
        Log.i("------width-------", String.valueOf(width));
        Log.i("------height-------", String.valueOf(height));
        Log.i("------src-------", Arrays.toString(srcDatas));
        Log.i("------dist-------", Arrays.toString(vertsDatas));
        //setBackgroundColor(Color.BLUE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(img == null){
            setImageResourceId(resId);
            init();
        }
        canvas.drawBitmapMesh(img, WIDTH, HEIGHT, vertsDatas, 0, null, 0, null);
    }

    public void mesh(float x, float y){
        mesh(x, y, K);
    }

    public void mesh(float x, float y, int k){
        K = k;
        if(x < 0)x = 0;
        if(y < 0)y = 0;
        if(x > img.getWidth())x = img.getWidth();
        if(y > img.getHeight())y = img.getHeight();
        float dx;
        float dy;
        float dxy;
        float pull;

        for (int p = 0; p < vertsDatas.length; p+=2){
            dx = x - srcDatas[p];
            dy = y - srcDatas[p + 1];
            dxy = dx * dx + dy * dy;
            pull = K / (float)(dxy * Math.sqrt((double) dxy));
            if(pull > 1){
                vertsDatas[p] = x;
                vertsDatas[p + 1] = y;
            }else{
                vertsDatas[p] = srcDatas[p] + dx * pull;
                vertsDatas[p + 1] = srcDatas[p + 1] + dy * pull;
            }
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        mesh(event.getX(), event.getY());
        return true;
    }
}
