package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import photoFun.*;
import java.util.*;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private GetCamera getCamera;
    private TextureView v1;
    private TextureView v2;
    private Handler handler;
    private final int[] isSwithCam={1};
    private final int[] isChange={1};
    private boolean isFlash=false;

    private RelativeLayout toolbar;
    private TestScroller text;
    private ImageView switchCam;
    private ImageView flashButton;
    private ImageButton document;
    private ImageButton btncam;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasPermission())
            requestP();
        init();
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result1 = (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);//如果没有权限返回的是false
            boolean result2 = (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            boolean result3 = (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            return (!result1) || (!result2) || (!result3);
        } else {
            return true;
        }
    }
    private void requestP() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
    private TextureView.SurfaceTextureListener surfaceTextureListener =  new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //必须是在此处开启摄像头，
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        getCamera = new GetCamera(MainActivity.this);
        v1=findViewById(R.id.textureView0);
        v2=findViewById(R.id.textureView1);
        v2.setSurfaceTextureListener(surfaceTextureListener);
        switchCam=findViewById(R.id.switchCam);//双摄像头配置
        switchCam.setBackgroundResource(R.mipmap.altercam);

        toolbar=findViewById(R.id.toolBar);
        toolbar.setBackgroundResource(R.color.transparent);
        text=findViewById(R.id.selecteText);
        document=findViewById(R.id.document);
        document.setVisibility(View.GONE);
        btncam =findViewById(R.id.btncam);
        btncam.setVisibility(View.GONE);
        flashButton=findViewById(R.id.flash_button);

        switchCam.setOnClickListener(this);
        document.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        //监听器设置
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
    private void Mruler(){
        btncam.setVisibility(btncam.GONE);
        document.setVisibility(document.GONE);
        toolbar.setBackgroundResource(R.color.transparent);
        document.setBackgroundResource(R.color.transparent);
    }
    private void Mvideo(){
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        toolbar.setBackgroundResource(R.color.grayTrans);
        btncam.setBackgroundResource(R.mipmap.init2);
        document.setBackgroundResource(R.mipmap.document);
        btncam.setOnClickListener(view -> {
            if (text.getSelectedString().equals("立体模式")){
                if (isChange[0] ==1){
                    Log.d("TAG","video");
                    btncam.setBackgroundResource(R.mipmap.shoot);
                    isChange[0] =0;
                    //开始录像
                }
                else{
                    btncam.setBackgroundResource(R.mipmap.init2);
                    isChange[0]=1;
                    //结束录像并保存

                }
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void Mcam(){
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        toolbar.setBackgroundResource(R.color.grayTrans);
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        btncam.setOnTouchListener((v, event) -> {
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_DOWN){
                btncam.setBackgroundResource(R.drawable.btn_bg_pressed);
                //拍照

                //获取深度图

            }
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_UP){
                btncam.setBackgroundResource(R.mipmap.init3);
            }
            return false;
        });
    }
    private void openCamera(){
        HandlerThread thread = new HandlerThread("DualCeamera");
        thread.start();
        handler = new Handler(thread.getLooper());
        CameraManager manager = (CameraManager) MainActivity.this
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            //权限检查
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //否则去请求相机权限
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},1000);
                return;
            }
            manager.openCamera(getCamera.getLogicCameraId(),AsyncTask.SERIAL_EXECUTOR, cameraOpenCallBack);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d("TAG", "相机已经打开");
            //当逻辑摄像头开启后， 配置物理摄像头的参数
            config(cameraDevice);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d("TAG", "相机连接断开");
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d("TAG", "相机打开失败");
        }
    };
    public void config(CameraDevice cameraDevice){
        String cameraID[]=getCamera.getCameraID();
        if (cameraID.length<2){
            Toast toast=Toast.makeText(MainActivity.this, "后置摄像头少于2个！", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 1920);
            toast.show();
        }
        try {
            //构建输出参数  在参数中设置物理摄像头
            List<OutputConfiguration> configurations = new ArrayList<>();
            CaptureRequest.Builder mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //配置第一个物理摄像头
            SurfaceTexture texture = v1.getSurfaceTexture();
            OutputConfiguration outputConfiguration = new OutputConfiguration(new Surface(texture));
            outputConfiguration.setPhysicalCameraId(cameraID[0]);
            configurations.add(outputConfiguration);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration.getSurface()));

            //配置第2个物理摄像头
            SurfaceTexture texture2 = v2.getSurfaceTexture();
            OutputConfiguration outputConfiguration2 = new OutputConfiguration(new Surface(texture2));
            outputConfiguration2.setPhysicalCameraId(cameraID[1]);
            configurations.add(outputConfiguration2);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration2.getSurface()));

            //注册摄像头
            SessionConfiguration sessionConfiguration = new SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    configurations,
                    AsyncTask.SERIAL_EXECUTOR,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(), null, handler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    }
            );
            cameraDevice.createCaptureSession(sessionConfiguration);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id==R.id.switchCam){
            if (isSwithCam[0]==1){
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.VISIBLE);
                isSwithCam[0]=0;
            }else {
                v1.setVisibility(View.VISIBLE);
                v2.setVisibility(View.GONE);
                isSwithCam[0]=1;
            }
        }
        else if (id==R.id.document){
            if (text.getSelectedString().equals("景深合成")){
                Intent intent=new Intent(MainActivity.this, PicActivity.class);
                startActivity(intent);
            }
            if (text.getSelectedString().equals("立体模式")){
                Intent intent=new Intent(MainActivity.this,VideoActivity.class);
                startActivity(intent);
            }
        }
        else if (id==R.id.flash_button){
            switchFlash();
        }
    }
    private void switchFlash(){
        isFlash = !isFlash;
        flashButton.setImageResource(isFlash ? R.mipmap.flash_open : R.mipmap.flash_close);
        try {
            //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        } catch (Exception e) {
            Toast.makeText(this, "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }
}
