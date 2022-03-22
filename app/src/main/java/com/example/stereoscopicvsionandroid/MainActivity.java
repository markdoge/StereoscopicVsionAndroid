package com.example.stereoscopicvsionandroid;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import photoFun.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FrameLayout previewLayout;
    private RelativeLayout toolbar;
    private RelativeLayout saveLayout;
    private ImageView flashButton;
    private ImageView btncam;
    private ImageView cancleSaveButton;
    private static final String TAG = "TAG";
    private ImageView mSaveButton;
    private TestScroller text;
    private OverCameraView overCameraView;
    private Camera monitor;//当监视器
    private Handler handler = new Handler();
    private Runnable runnable;
    private ImageButton document;
    private boolean isFlashing;
    final int[] isChange = {1};
    private boolean isFoucing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Camera.getNumberOfCameras()<=2){
            Toast toast=Toast.makeText(MainActivity.this, "后置摄像头少于2个！", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 1920);
            toast.show();
            //finish();//调试可以先注释掉
        }
        setContentView(R.layout.activity_main);
        if (hasPermission())
            requestP();
        init();
    }
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result1=(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);//如果没有权限返回的是false
            boolean result2=(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==PackageManager.PERMISSION_GRANTED);
            boolean result3=(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            return (!result1)||(!result2)||(!result3);
        } else {
            return true;
        }
    }
    private void requestP(){
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    private void init() {
        document = findViewById(R.id.document);
        document.setVisibility(document.GONE);
        previewLayout = findViewById(R.id.camera0Preview);
        toolbar = findViewById(R.id.toolBar);
        toolbar.setBackgroundResource(R.color.transparent);
        saveLayout = findViewById(R.id.savePhotoBar);
        btncam = findViewById(R.id.btncam);
        btncam.setVisibility(btncam.GONE);
        cancleSaveButton = findViewById(R.id.undoButton);
        mSaveButton = findViewById(R.id.saveButton);
        flashButton = findViewById(R.id.flash_button);
        text=findViewById(R.id.selecteText);

        cancleSaveButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);

        text.setOnTouchListener((v, event) -> {
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
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        toolbar.setBackgroundResource(R.color.grayTrans);
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        jump('p');
        btncam.setOnTouchListener((v, event) -> {
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_DOWN){
                btncam.setBackgroundResource(R.drawable.btn_bg_pressed);
                takePhoto();
            }
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_UP){
                btncam.setBackgroundResource(R.mipmap.init3);
            }
            return false;
        });
    }
    private void Mvideo(){
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        toolbar.setBackgroundResource(R.color.grayTrans);
        btncam.setBackgroundResource(R.mipmap.init2);
        document.setBackgroundResource(R.mipmap.document);
        jump('v');
        btncam.setOnClickListener(view -> {
            if (text.getSelectedString().equals("立体模式")){
                if (isChange[0] ==1){
                    Log.d(TAG,"video");
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
        btncam.setVisibility(btncam.GONE);
        document.setVisibility(document.GONE);
        toolbar.setBackgroundResource(R.color.transparent);
        document.setBackgroundResource(R.color.transparent);
    }
    private void jump(char a){
        document.setOnClickListener(v -> {
            if (a=='p'){
                Intent intent=new Intent(MainActivity.this, PicActivity.class);
                startActivity(intent);
            }
            if (a=='v'){
                Intent intent=new Intent(MainActivity.this,VideoActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        monitor = Camera.open(0);
        CameraPreview preview = new CameraPreview(this, monitor);
        overCameraView = new OverCameraView(this);
        previewLayout.addView(preview);
        previewLayout.addView(overCameraView);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isFoucing) {
                float x = event.getX();
                float y = event.getY();
                isFoucing = true;
                if (monitor != null) {
                    overCameraView.setTouchFoucusRect(monitor, autoFocusCallback, x, y);
                }
                runnable = () -> {
                    Toast.makeText(MainActivity.this, "自动聚焦超时,请调整合适的位置拍摄！", Toast.LENGTH_SHORT);
                    isFoucing = false;
                    overCameraView.setFoucuing(false);
                    overCameraView.disDrawTouchFocusRect();
                };
                //设置聚焦超时
                handler.postDelayed(runnable, 3000);
            }
        }
        return super.onTouchEvent(event);
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            isFoucing = false;
            overCameraView.setFoucuing(false);
            overCameraView.disDrawTouchFocusRect();
            //停止聚焦超时回调
            handler.removeCallbacks(runnable);
        }
    };

    private void takePhoto() {

    }

    private void switchFlash() {
        isFlashing = !isFlashing;
        flashButton.setImageResource(isFlashing ? R.mipmap.flash_open : R.mipmap.flash_close);
        try {
            Camera.Parameters parameters = monitor.getParameters();
            parameters.setFlashMode(isFlashing ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            monitor.setParameters(parameters);
        } catch (Exception e) {
            Toast.makeText(this, "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }

    private void cancleSavePhoto() {
        toolbar.setVisibility(View.VISIBLE);
        saveLayout.setVisibility(View.GONE);
        monitor.startPreview();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.document) {

        } else if (id == R.id.flash_button) {
            switchFlash();
        } else if (id == R.id.saveButton) {
            savePhoto();
        } else if (id == R.id.undoButton) {
            cancleSavePhoto();
        }
    }
    private void savePhoto() {

    }

}
