package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.*;
import android.hardware.camera2.params.*;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.*;
import android.view.animation.RotateAnimation;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.OpenCVLoader;

import OpenCVFun.RB3D;
import OpenCVFun.RB3DAsyncTask2;
import photoFun.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;



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
    private String savePath0;
    private String savePath1;
    private String depthmapPath;
    private SimpleDateFormat simpleDateFormat;
    private Size[] videoSize;
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private MediaRecorder mMediaRecorder;
    private MediaRecorder mMediaRecorder1;
    private static CameraCaptureSession mCameraCaptureSession;
    private static CameraCaptureSession mCameraCaptureSession1;
    private Point point;
    private CameraManager manager;
    private CaptureRequest.Builder mPreViewBuidler;
    private CaptureRequest.Builder mPreViewBuidler1;
    private CameraDevice mCameraDevice;
    private Handler mChildHandler;
    private Chronometer timer; //计时器
    private String fileName;
    private ProgressBar rb3dProgressBar;
    private OrientationEventListener orientationEventListener;
    private RotateAnimation rotationAnimation;
    private ProgressDialog dialog;
    private Toast videoFinishToast;
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("TAG","Main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasPermission())
            requestP();
        init();
        orientationEventListener=new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (((orientation>=0)&&(orientation<=30))||(orientation>=330)) {
                    mMediaRecorder.setOrientationHint(0);
                }
                else {
                    mMediaRecorder.setOrientationHint(90);
                }
            }
        };
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result1 = (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);//如果没有权限返回的是false
            boolean result2 = (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            boolean result3 = (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            boolean result4 = (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
            return (!result1) || (!result2) || (!result3) || (!result4);
        } else {
            return true;
        }
    }
    private void requestP() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
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
        if (OpenCVLoader.initDebug()) {
            Log.d("TAG", "OpenCVLoader初始化成功");
        }else{
            Log.d("TAG", "OpenCVLoader初始化失败");
        }
        getCamera = new GetCamera(MainActivity.this);
        videoSize =getCamera.getVideoSize();
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
        rb3dProgressBar=findViewById(R.id.rb3dProgressBar);
        rb3dProgressBar.setVisibility(View.GONE);
        dialog=new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//条形进度条
        dialog.setCancelable(false);//能否在显示过程中关闭
        videoFinishToast =Toast.makeText(MainActivity.this, "后置摄像头少于2个！", Toast.LENGTH_LONG);
        videoFinishToast.setGravity(Gravity.CENTER, 0, 1920);
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
        toolbar.setBackgroundResource(R.color.black);
        btncam.setBackgroundResource(R.mipmap.init2);
        document.setBackgroundResource(R.mipmap.document);
        btncam.setOnClickListener(view -> {
            if (text.getSelectedString().equals("立体模式")){
                if (isChange[0] ==1){
                    Log.d("TAG","开始录像");
                    startRecordingVideo();
                    alterCam();
                }
                else{
                    stopRecorder();
                    Log.d("TAG","停止录像");
                    timer.setVisibility(timer.GONE);
                    alterCam();
                    //结束录像并保存
                }
            }
        });
    }
    private void startRecordingVideo() {
        Log.d("TAG","video");
        btncam.setBackgroundResource(R.mipmap.shoot);
        isChange[0] =0;
        for (int i = 0; i< videoSize.length; i++){
            Log.d("TAG","size number:"+ i +" size is "+ videoSize[i]);
        }
        configSession();
        mMediaRecorder.start();
        timer.setVisibility(timer.VISIBLE);
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        timer.start();
    }
    private void stopRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            //停止计时
            timer.stop();
            timer.setBase(SystemClock.elapsedRealtime());//计时器清零
            new RB3DAsyncTask2(rb3dProgressBar,dialog,videoFinishToast).execute(fileName,fileName);
        }
        broadcast();
        startPreview();
    }
    private void startPreview() {
        stopPreview();
        SurfaceTexture mSurfaceTexture = v1.getSurfaceTexture();
        Size cameraSize = getMatchingSize();
        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(cameraSize.getWidth(),
                cameraSize.getHeight());
        //获取Surface显示预览数据
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            //创建CaptureRequestBuilder,TEMPLATE_PREVIEW比表示预览请求
            mPreViewBuidler = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置Surface作为预览数据的显示界面
            mPreViewBuidler.addTarget(previewSurface);
            //创建相机捕获会话,第一个参数是捕获数据Surface列表,
            // 第二个参数是CameraCaptureSession的状态回调接口,
            //当他创建好后会回调onConfigured方法,第三个参数用来确定Callback在哪个线程执行
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCameraCaptureSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(MainActivity.this.getApplicationContext(), "Faileedsa ", Toast.LENGTH_SHORT).show();
                        }
                    }, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
        toolbar.setBackgroundResource(R.color.black);
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        btncam.setOnTouchListener((v, event) -> {
            if (text.getSelectedString().equals("景深合成")&&event.getAction()==MotionEvent.ACTION_DOWN){
                Intent intent=new Intent(MainActivity.this,TakePic.class);
                startActivity(intent);
            }
            return false;
        });
    }
    private void openCamera(){
        HandlerThread thread = new HandlerThread("DualCeamera");
        thread.start();
        handler = new Handler(thread.getLooper());
        manager = (CameraManager) MainActivity.this
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            //权限检查
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //否则去请求相机权限
                ActivityCompat.requestPermissions(MainActivity.this,
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
    //录像时会话状态回调
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCameraCaptureSession = session;
            updatePreview();
            try {
                //执行重复获取数据请求，等于一直获取数据呈现预览画面，mSessionCaptureCallback会返回此次操作的信息回调
                mCameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(),
                        mSessionCaptureCallback, mChildHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };
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
    private void configMediaRecorder() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        File saveLocation = new File(Environment.getExternalStorageDirectory(),"/DCIM/stereo");
        saveLocation.mkdirs();
        //需要先创建目录，否则会无法保存到对应目录
        File file = new File(Environment.getExternalStorageDirectory() +
                "/DCIM/stereo/" + simpleDateFormat.format(date) + "_left.mp4");
        if (file.exists()) {
            file.delete();
        }
        fileName=file.getAbsolutePath();
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        Surface surface = new Surface(v2.getSurfaceTexture());
        Size size = getMatchingSize();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频来源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置视频来源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//设置输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置音频编码格式选择AAC
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码格式应该选择H264
        mMediaRecorder.setVideoEncodingBitRate(8 * 1024 * 1920);//设置比特率。
        mMediaRecorder.setVideoFrameRate(30);//设置帧数
        mMediaRecorder.setVideoSize(size.getWidth(), size.getHeight());
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        //摄像头2
        Log.d("TAG","Stage 1");
        try {
            mMediaRecorder.prepare();
            Log.d("TAG","prepare 1 success");
        } catch (IOException e) {
            Log.d("TAG","prepare 1 fail");
        }
    }
    private void configSession() {
        int rotation = MainActivity.this.getWindowManager().getDefaultDisplay().getRotation();
        mPreViewBuidler.set(CaptureRequest.JPEG_ORIENTATION, INVERSE_ORIENTATIONS.get(rotation));
        try {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.stopRepeating();//停止预览，准备切换到录制视频
                mCameraCaptureSession.close();//关闭预览的会话，需要重新创建录制视频的会话
                mCameraCaptureSession = null;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        configMediaRecorder();
        Size cameraSize = getMatchingSize();
        SurfaceTexture surfaceTexture = v1.getSurfaceTexture();
        SurfaceTexture surfaceTexture1 = v2.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        surfaceTexture1.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        Surface previewSurface1 = new Surface(surfaceTexture1);
        Surface recorderSurface = mMediaRecorder.getSurface();//从获取录制视频需要的Surface
        Surface recorderSurface1 = mMediaRecorder.getSurface();//从获取录制视频需要的Surface
        //摄像头2
        try {
            mPreViewBuidler = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreViewBuidler1 = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreViewBuidler.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreViewBuidler.addTarget(previewSurface);
            mPreViewBuidler.addTarget(recorderSurface);
            mPreViewBuidler1.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreViewBuidler1.addTarget(previewSurface1);
            mPreViewBuidler1.addTarget(recorderSurface1);
            //请注意这里设置了Arrays.asList(previewSurface,recorderSurface) 2个Surface，很好理解录制视频也需要有画面预览，
            // 第一个是预览的Surface，第二个是录制视频使用的Surface
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recorderSurface),
                    mSessionStateCallback, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    public void config(CameraDevice cameraDevice){
        String cameraID[]=getCamera.getCameraID();
        Log.d("TAG", cameraID.toString());
        if (cameraID.length<2){
            //Toast会报错
//            Toast toast=Toast.makeText(MainActivity.this, "后置摄像头少于2个！", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 1920);
//            toast.show();
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
            SurfaceTexture texture2 = v2.getSurfaceTexture();
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
    private void alterCam(){
        Log.d("TAG","alterCam!");
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
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id==R.id.switchCam){
            alterCam();
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
    }
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mCameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(), null, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void stopPreview() {
        //关闭预览就是关闭捕获会话
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }
}
