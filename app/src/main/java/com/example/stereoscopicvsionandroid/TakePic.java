package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.*;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.OpenCVLoader;

import photoFun.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;



public class TakePic extends AppCompatActivity implements View.OnClickListener {
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
    private String savePath0;
    private String savePath1;
    private SimpleDateFormat simpleDateFormat;
    private Size mPreviewSize;
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static CameraCaptureSession mCameraCaptureSession;
    private Point point;
    private CameraManager manager;
    private CaptureRequest.Builder mPreViewBuidler;
    private ImageReader mImageReader;  //图片阅读器
    private CameraDevice mCameraDevice;
    private Handler mChildHandler;
    private ArrayList<String> imageList = new ArrayList<>();   // 路径集合
    private Chronometer timer; //计时器
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
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
        getCamera = new GetCamera(TakePic.this);
        v1=findViewById(R.id.textureView0);
        v2=findViewById(R.id.textureView1);
        v2.setSurfaceTextureListener(surfaceTextureListener);
        //setPreviewSize(point.x,point.y);
        switchCam=findViewById(R.id.switchCam);//双摄像头配置
        switchCam.setBackgroundResource(R.mipmap.altercam);
        //控件绑定
        toolbar=findViewById(R.id.toolBar);
        toolbar.setBackgroundResource(R.color.transparent);
        text=findViewById(R.id.selecteText);
        document=findViewById(R.id.document);
        document.setVisibility(View.GONE);
        btncam =findViewById(R.id.btncam);
        btncam.setVisibility(View.GONE);
        flashButton=findViewById(R.id.flash_button);
        timer=findViewById(R.id.timer);
        timer.setVisibility(timer.GONE);
        //控件样式

        //设置视频、图片、音频、摄像头规格
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
                Intent intent=new Intent(TakePic.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
    // 广播通知相册更新
    public void broadcast() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        Log.d("TAG", "broadcast: success");
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
                simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
                Calendar calendar = Calendar.getInstance();
                String time=simpleDateFormat.format(calendar.getTime());
                savePath0=time+"_1.jpg";
                savePath1=time+"_2.jpg";
                //拍照

                //获取深度图

                savePath0=savePath1=null;
            }
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_UP){
                btncam.setBackgroundResource(R.mipmap.init3);
            }
            return false;
        });
    }
    public class ImageSaver implements Runnable {
        private Image mImage;//图片
        private Context mContext;
        public ImageSaver(Context context, Image image) {
            Log.d("TAG", "ImageSaver: success");
            mImage = image;
            mContext = context;
        }
        @Override
        public void run() {
            //将照片转字节
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String path = Environment.getExternalStorageDirectory() +
                    "/DCIM/camera/myPicture" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(path);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                fos.write(data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                broadcast();
                Message message = new Message();
                message.what = 0;
                Bundle mBundle = new Bundle();
                mBundle.putString("myPath",path);
                message.setData(mBundle);
                handler.sendMessage(message);
                mImage.close(); // 必须关闭 不然拍第二章会报错
            }
        }
    }
    // 异步消息处理
    private Handler handlerPhoto = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 0:
                    Bundle bundle = message.getData();
                    //通过指定的键值对获取到刚刚发送过来的地址
                    String myPath = bundle.getString("myPath");
                    imageList.add(myPath);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + message.what);
            }

        }
    };
    private void openCamera(){
        HandlerThread thread = new HandlerThread("DualCeamera");
        thread.start();
        handler = new Handler(thread.getLooper());
        manager = (CameraManager) TakePic.this
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            //权限检查
            if (ActivityCompat.checkSelfPermission(TakePic.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //否则去请求相机权限
                ActivityCompat.requestPermissions(TakePic.this,
                        new String[]{Manifest.permission.CAMERA},1000);
                return;
            }
            Log.d("TAG","try to open camera");
            manager.openCamera(getCamera.getLogicCameraId(),AsyncTask.SERIAL_EXECUTOR, cameraOpenCallBack);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d("TAG", "相机已经打开");
            mCameraDevice=cameraDevice;
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
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                     long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };
    public void config(CameraDevice cameraDevice){
        String cameraID[]=getCamera.getCameraID();
        Log.d("TAG", cameraID.toString());
        if (cameraID.length<2){
            try {
                mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreViewBuidler.addTarget(new Surface(v1.getSurfaceTexture()));
                mPreViewBuidler.addTarget(new Surface(v2.getSurfaceTexture()));
                mCameraDevice.createCaptureSession(
                        Arrays.asList(new Surface(v1.getSurfaceTexture()), new Surface(v2.getSurfaceTexture())),
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
                                    mCameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(),null,handler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                    Log.d("linc","set preview builder failed."+e.getMessage());
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            }
                        },handler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            //构建输出参数  在参数中设置物理摄像头
            List<OutputConfiguration> configurations = new ArrayList<>();
            mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

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
                                mCameraCaptureSession=cameraCaptureSession;
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
                Intent intent=new Intent(TakePic.this, PicActivity.class);
                startActivity(intent);
            }
            if (text.getSelectedString().equals("立体模式")){
                Intent intent=new Intent(TakePic.this,VideoActivity.class);
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
