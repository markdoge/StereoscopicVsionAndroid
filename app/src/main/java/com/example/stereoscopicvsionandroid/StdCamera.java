package com.example.stereoscopicvsionandroid;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class StdCamera extends AppCompatActivity {
    private Camera camera1;
    private Camera camera2;
    private int cametacount= Camera.getNumberOfCameras();
    private SurfaceView camraView;
    private boolean isPreview;
    private SurfaceHolder camViewHolder;
    StdCamera(SurfaceView temp){
        camera1 = Camera.open(0);
        camera1.setDisplayOrientation(90);
        if (cametacount>2){
            camera2 = Camera.open(1);
            camera2.setDisplayOrientation(90);//不加的话，预览的图像就是横的
            //camera2.autoFocus();//自动对焦（焦点）
        }
        camraView=temp;
        camViewHolder= camraView.getHolder();
        camViewHolder.setFormat(PixelFormat.TRANSPARENT);
        camViewHolder.addCallback(camSurfaceCallback);//执行回调函数，当关闭软件时销毁Holder
    }
    private SurfaceHolder.Callback camSurfaceCallback= new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            try {
                camera1.setPreviewDisplay(holder);//通过SurfaceView显示取景画面
                camera1.startPreview();//开始预览
                isPreview = true;//设置是否预览参数为真
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}
        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            if(camera1 != null){
                if(isPreview){//正在预览
                    camera1.stopPreview();
                    camera1.release();
                }
            }
        }
    };
    public int getCam(){return cametacount;}
}
