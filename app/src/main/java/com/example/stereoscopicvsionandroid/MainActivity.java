package com.example.stereoscopicvsionandroid;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.service.autofill.OnClickAction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity implements OnClickAction {
    private Camera camera;
    private ImageButton document;
    private ImageButton btncam;
    private TestScroller text;
    private TextView pic;
    final int[] isChange = {1};
    private static final String TAG = "TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initXML();
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
    private void initXML(){
        document=findViewById(R.id.file);
        document.setBackgroundResource(R.color.transparent);
        btncam = findViewById(R.id.cam);
        text=findViewById(R.id.selecteText);
        btncam.setBackgroundResource(R.mipmap.init1);
        pic=findViewById(R.id.pic);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //SurfaceView surfaceView=findViewById(R.id.camView);
        //SurfaceHolder surfaceHolder=surfaceView.getHolder();
        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //camera=Camera.open();
    }
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