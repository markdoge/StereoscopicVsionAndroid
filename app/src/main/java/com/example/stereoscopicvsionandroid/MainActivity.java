package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity implements OnClickAction {
    private ImageButton document;
    private ImageButton btncam;
    private TestScroller text;
    private TextView pic;
    final int[] isChange = {1};
    private static final String TAG = "TAG";
    private StdCamera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasPermission()){
            requestP();
        }
        init();
    }
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result1=(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);//如果没有权限返回的是false
            boolean result2=(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==PackageManager.PERMISSION_GRANTED);
            boolean result3=(checkSelfPermission(android.Manifest.permission.CAMERA) ==PackageManager.PERMISSION_GRANTED);
            return (!result1)||(!result2)||(!result3);
        } else {
            return true;
        }
    }
    private void requestP(){
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void init(){
        /*File file = null;
        try {
            file = new File("/storage/emulated/0/pic/");
            if (!file.exists()) {
                file.mkdir();
                file = new File("/storage/emulated/0/pic/"+"readMEME.txt");
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }*/
        SurfaceView temp = findViewById(R.id.view);
        camera=new StdCamera(temp);
        //摄像头的SurfaceView
        Log.d(TAG,"camera");
        document=findViewById(R.id.file);
        document.setBackgroundResource(R.color.transparent);
        btncam = findViewById(R.id.cam);
        text=findViewById(R.id.selecteText);
        btncam.setBackgroundResource(R.mipmap.init1);
        pic=findViewById(R.id.pic);
        text.setOnTouchListener((v, event) -> {
            if (camera.getCam()<=2){
                Toast.makeText(MainActivity.this, "后置摄像头少于2个！", Toast.LENGTH_LONG).show();
            }
            if (text.getSelectedString().equals("立体模式")){
                Mvideo();
            }
            if (text.getSelectedString().equals("景深合成")){
                Mcam();
            }
            if (text.getSelectedString().equals("测距模式")){
                Mruler();
            }
            return false;
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void Mcam(){
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        jump('p');
        btncam.setOnTouchListener((v, event) -> {
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_DOWN){
                btncam.setBackgroundResource(R.drawable.btn_bg_pressed);
                pic.setBackgroundResource(R.mipmap.pic);
            }
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_UP){
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pic.setBackgroundResource(R.drawable.btn_bg_pressed);
                btncam.setBackgroundResource(R.mipmap.init3);
            }
            return false;
        });
    }
    private void Mvideo(){
        btncam.setBackgroundResource(R.mipmap.init2);
        document.setBackgroundResource(R.mipmap.document);
        jump('v');
        btncam.setOnClickListener(view -> {
            if (text.getSelectedString().equals("立体模式")){
                if (isChange[0] ==1){
                    btncam.setBackgroundResource(R.mipmap.shoot);
                    isChange[0] =0;
                }
                else{
                    btncam.setBackgroundResource(R.mipmap.init2);
                    isChange[0]=1;
                }
            }
        });
    }
    private void Mruler(){
        btncam.setBackgroundResource(R.mipmap.init1);
        document.setBackgroundResource(R.color.transparent);
    }
    private void jump(char a){
        document.setOnClickListener(v -> {
            if (a=='p'){
                Intent intent=new Intent(MainActivity.this,PicActivity.class);
                startActivity(intent);
            }
            if (a=='v'){
                Intent intent=new Intent(MainActivity.this,VideoActivity.class);
                startActivity(intent);
            }
        });
    }
}