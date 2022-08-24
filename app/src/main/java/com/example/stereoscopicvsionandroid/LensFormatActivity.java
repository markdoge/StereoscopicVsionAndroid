package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import photoFun.GetCamera;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class LensFormatActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton btn;
    int cameraNum=0;
    private TextureView v1;
    private TextureView v0;
    private CameraManager manager;
    private Handler handler;
    private GetCamera getCamera;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreViewBuidler;
    private static CameraCaptureSession mCameraCaptureSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lensformat);
        init();

    }
    private void openCamera() {
        HandlerThread thread = new HandlerThread("DualCeamera");
        thread.start();
        handler = new Handler(thread.getLooper());
        manager = (CameraManager) LensFormatActivity.this
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            //权限检查
            if (ActivityCompat.checkSelfPermission(LensFormatActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //否则去请求相机权限
                ActivityCompat.requestPermissions(LensFormatActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 1000);
                return;
            }
            Log.d("TAG", "try to open camera");
            manager.openCamera(getCamera.getLogicCameraId(), AsyncTask.SERIAL_EXECUTOR, cameraOpenCallBack);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void init(){
        btn=findViewById(R.id.camBtn);
        v0 = findViewById(R.id.textureView_zero);
        v1 = findViewById(R.id.textureView_one);
        btn.setOnClickListener(this);
        v0.setVisibility(View.VISIBLE);
        v1.setVisibility(View.GONE);
        btn.setBackgroundResource(R.mipmap.init3);
        Toast.makeText(LensFormatActivity.this, "请距离纸张1米远！", Toast.LENGTH_SHORT);
        getCamera = new GetCamera(LensFormatActivity.this);
        v0.setSurfaceTextureListener(surfaceTextureListener);
    }
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d("TAG", "相机已经打开");
            mCameraDevice = cameraDevice;
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
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
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
    public void config(CameraDevice cameraDevice) {
        String cameraID[] = getCamera.getCameraID();
        Log.d("TAG", cameraID.toString());
        if (cameraID.length < 2) {
            try {
                Size cameraSize = getMatchingSize();
                mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                SurfaceTexture texture1 = v0.getSurfaceTexture();
                SurfaceTexture texture2 = v1.getSurfaceTexture();
                texture1.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
                texture2.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
                mPreViewBuidler.addTarget(new Surface(texture1));
                mPreViewBuidler.addTarget(new Surface(texture2));
                mCameraDevice.createCaptureSession(
                        Arrays.asList(new Surface(texture1), new Surface(texture2)),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                // The camera is already closed
                                if (null == mCameraDevice) {
                                    return;
                                }
                                mCameraCaptureSession = cameraCaptureSession;
                                try {
                                    mPreViewBuidler.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                    mCameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(), null, handler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                    Log.d("linc", "set preview builder failed." + e.getMessage());
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            }
                        }, handler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            //构建输出参数  在参数中设置物理摄像头
            Size cameraSize = getMatchingSize();
            List<OutputConfiguration> configurations = new ArrayList<>();
            mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //配置第一个物理摄像头
            SurfaceTexture texture = v1.getSurfaceTexture();
            texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
            OutputConfiguration outputConfiguration = new OutputConfiguration(new Surface(texture));
            outputConfiguration.setPhysicalCameraId(cameraID[0]);
            configurations.add(outputConfiguration);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration.getSurface()));

            //配置第2个物理摄像头
            SurfaceTexture texture2 = v0.getSurfaceTexture();
            texture2.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
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
                                mCameraCaptureSession = cameraCaptureSession;
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
    private Size getMatchingSize() {
        Size selectSize = null;
        try {
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(String.valueOf(CameraCharacteristics.LENS_FACING_BACK));
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get
                    (CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            //这里是将预览铺满屏幕,所以直接获取屏幕分辨率
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            //屏幕分辨率宽
            int deviceWidth = displayMetrics.widthPixels;
            //屏幕分辨率高
            int deviceHeight = displayMetrics.heightPixels;
            /**
             * 循环40次,让宽度范围从最小逐步增加,找到最符合屏幕宽度的分辨率,
             * 你要是不放心那就增加循环,肯定会找到一个分辨率,不会出现此方法返回一个null的Size的情况
             * ,但是循环越大后获取的分辨率就越不匹配
             */
            for (int j = 1; j < 41; j++) {
                for (int i = 0; i < sizes.length; i++) { //遍历所有Size
                    Size itemSize = sizes[i];
                    //判断当前Size高度小于屏幕宽度+j*5  &&  判断当前Size高度大于屏幕宽度-j*5  &&  判断当前Size宽度小于当前屏幕高度
                    if (itemSize.getHeight() < (deviceWidth + j * 5) && itemSize.getHeight() > (deviceWidth - j * 5)) {
                        if (selectSize != null) { //如果之前已经找到一个匹配的宽度
                            if (Math.abs(deviceHeight - itemSize.getWidth()) < Math.abs(deviceHeight - selectSize.getWidth())) { //求绝对值算出最接近设备高度的尺寸
                                selectSize = itemSize;
                                continue;
                            }
                        } else {
                            selectSize = itemSize;
                        }
                    }
                }
                if (selectSize != null) { //如果不等于null 说明已经找到了 跳出循环
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "getMatchingSize: 选择的分辨率宽度=" + selectSize.getWidth());
        Log.d("TAG", "getMatchingSize: 选择的分辨率高度=" + selectSize.getHeight());
        return selectSize;
    }
    @Override
    public void onClick(View v) {
        int id =v.getId();
        if (id==btn.getId()){
            cameraNum+=1;

            if (cameraNum==2){
                Intent intent = new Intent(LensFormatActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
