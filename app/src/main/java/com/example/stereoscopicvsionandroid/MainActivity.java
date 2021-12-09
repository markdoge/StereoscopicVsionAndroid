package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Environment;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity implements OnClickAction {
    private String pathVideo = Environment.getExternalStorageDirectory().getPath() + File.separator + "/videoVersion";
    private String pathPic = Environment.getExternalStorageDirectory().getPath() + File.separator + "/pictureVersion";
    private Camera camera;
    private File fileVideo;
    private File filePic;
    private ImageButton document;
    private ImageButton btncam;
    private TestScroller text;
    private TextView pic;
    private SurfaceView camView;
    private boolean isPreview;
    private SurfaceHolder camViewHolder;
    final int[] isChange = {1};
    private static final String TAG = "TAG";
    private static String[] permisstions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasPermission()){
            Log.d(TAG,"inxxxxxxxxx");
            init();
        }else{
            Log.d(TAG,"inxxxxxxxxx");
            requestP();
        }
    }
    private boolean hasPermission() {
        Log.i(TAG, "hasPermission: 判断是否有权限");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result1=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==PackageManager.PERMISSION_GRANTED;
            boolean result2=checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==PackageManager.PERMISSION_GRANTED;
            boolean result3=checkSelfPermission(Manifest.permission.CAMERA) ==PackageManager.PERMISSION_GRANTED;
            boolean result4=checkSelfPermission(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) ==PackageManager.PERMISSION_GRANTED;
            return result1&&result2&&result3&&result4;
        } else {
            return true;
        }
    }
    private void requestP(){
        Log.d(TAG,"start request");
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            if (shouldShowRequestPermissionRationale(Manifest.permission_group.CAMERA)&&
                    shouldShowRequestPermissionRationale(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)&&
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)&&
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Log.d(TAG,"request CAMERA SDcard FILE");
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }
    private void init(){
        permisstions= new String[]{Manifest.permission.CAMERA};
        filePic=new File(pathPic);
        fileVideo=new File(pathVideo);
        if (!filePic.exists()&&!filePic.isDirectory()){
            filePic.mkdirs();
            try {
                //new File(filePic+"/text.txt").createNewFile();
            }
            catch (Exception e){e.printStackTrace();}
            Log.d(TAG,"pic init");
        }
        if (!fileVideo.exists()&&!fileVideo.isDirectory()){
            fileVideo.mkdirs();
            Log.d(TAG,"video init");
        }
        //摄像头的SurfaceView
        camera = Camera.open();
        camView.findViewById(R.id.camView);
        camViewHolder=camView.getHolder();
        camViewHolder.setFormat(PixelFormat.TRANSPARENT);
        camViewHolder.addCallback(camSurfaceCallback);//执行回调函数，当关闭软件时销毁Holder

        document=findViewById(R.id.file);
        document.setBackgroundResource(R.color.transparent);
        btncam = findViewById(R.id.cam);
        text=findViewById(R.id.selecteText);
        btncam.setBackgroundResource(R.mipmap.init1);
        pic=findViewById(R.id.pic);
        text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (text.getSelectedString()=="立体模式"){
                    Mvideo();
                }
                if (text.getSelectedString()=="景深合成"){
                    Mcam();
                }
                if (text.getSelectedString()=="测距模式"){
                    Mruler();
                }
                return false;
            }
        });
    }
    private SurfaceHolder.Callback camSurfaceCallback= new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);//通过SurfaceView显示取景画面
                camera.startPreview();//开始预览
                isPreview = true;//设置是否预览参数为真
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            if(camera != null){
                if(isPreview){//正在预览
                    camera.stopPreview();
                    camera.release();
                }
            }
        }
    };
    private void Mcam(){
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        jump('p');
        btncam.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (text.getSelectedString()=="景深合成"&&event.getAction()==MotionEvent.ACTION_DOWN){
                    btncam.setBackgroundResource(R.drawable.btn_bg_pressed);
                    pic.setBackgroundResource(R.mipmap.pic);
                }
                if (text.getSelectedString()=="景深合成"&&event.getAction()==MotionEvent.ACTION_UP){
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pic.setBackgroundResource(R.drawable.btn_bg_pressed);
                    btncam.setBackgroundResource(R.mipmap.init3);
                }
                return false;
            }
        });
    }
    private void Mvideo(){
        btncam.setBackgroundResource(R.mipmap.init2);
        document.setBackgroundResource(R.mipmap.document);
        jump('v');
        btncam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text.getSelectedString()=="立体模式"){
                    if (isChange[0] ==1){
                        btncam.setBackgroundResource(R.mipmap.shoot);
                        isChange[0] =0;
                    }
                    else{
                        btncam.setBackgroundResource(R.mipmap.init2);
                        isChange[0]=1;
                    }
                }
            }
        });
    }
    private void Mruler(){
        btncam.setBackgroundResource(R.mipmap.init1);
        document.setBackgroundResource(R.color.transparent);
    }
    private void jump(char a){
        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a=='p'){
                    Intent intent=new Intent(MainActivity.this,PicActivity.class);
                    startActivity(intent);
                }
                if (a=='v'){
                    Intent intent=new Intent(MainActivity.this,VideoActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}