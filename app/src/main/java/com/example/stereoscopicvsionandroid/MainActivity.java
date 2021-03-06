package com.example.stereoscopicvsionandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.OpenCVLoader;

import OpenCVFun.RB3DAsyncTask;
import albumFun.PhotoLoader;
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
    private ImageView takePhoto;
    private ImageButton document;
    private ImageButton btncam;
    private Size[] videoSize;
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private MediaRecorder mMediaRecorder;
    private static CameraCaptureSession mCameraCaptureSession;
    private Point point;
    private CameraManager manager;
    private CaptureRequest.Builder mPreViewBuidler;
    private CameraDevice mCameraDevice;
    private Handler mChildHandler;
    private Chronometer timer; //?????????
    private String fileName;
    private ProgressBar rb3dProgressBar;
    private OrientationEventListener orientationEventListener;
    private RotateAnimation rotationAnimation;
    private ProgressDialog dialog;
    private Toast videoFinishToast;
    private int mode=0;//0,1,2????????????????????????????????????
    private StereoBMUtil stereoBMUtil;
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }
    private ImageView imageView;
    private TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("TAG","Main activity");
        if (OpenCVLoader.initDebug()) {
            Log.d("TAG", "OpenCVLoader???????????????");
        }else{
            Log.d("TAG", "OpenCVLoader???????????????");
        }
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

    public TextureView getV1() {
        return v1;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result1 = (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);//??????????????????????????????false
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
            //????????????????????????????????????
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
        videoSize =getCamera.getVideoSize();
        v1=findViewById(R.id.textureView0);
        v2=findViewById(R.id.textureView1);
        v2.setSurfaceTextureListener(surfaceTextureListener);
        //setPreviewSize(point.x,point.y);
        switchCam=findViewById(R.id.switchCam);//??????????????????
        switchCam.setBackgroundResource(R.mipmap.altercam);
        //????????????
        toolbar=findViewById(R.id.toolBar);
        toolbar.setBackgroundResource(R.color.transparent);
        text=findViewById(R.id.selecteText);
        takePhoto=findViewById(R.id.takephoto);
        takePhoto.setVisibility(takePhoto.GONE);
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
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//???????????????
        dialog.setCancelable(false);//??????????????????????????????
        videoFinishToast =Toast.makeText(MainActivity.this, "?????????????????????2??????", Toast.LENGTH_LONG);
        videoFinishToast.setGravity(Gravity.CENTER, 0, 1920);
        imageView=findViewById(R.id.dynamicImage);
        textView=findViewById(R.id.dynamicText);
        imageView.setVisibility(imageView.GONE);
        textView.setVisibility(textView.GONE);
        //????????????

        //????????????????????????????????????????????????
        switchCam.setOnClickListener(this);
        document.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        imageView.setOnClickListener(this);
        //???????????????
        text.setOnTouchListener((v, event) -> {
            if (text.getSelectedString().equals("????????????")){
                mode=1;
                takePhoto.setVisibility(takePhoto.GONE);
                Mvideo();
            }
            if (text.getSelectedString().equals("????????????")){
                mode=2;
                takePhoto.setVisibility(takePhoto.GONE);
                Mcam();
            }
            if (text.getSelectedString().equals("????????????")){
                mode=0;
                takePhoto.setVisibility(takePhoto.GONE);
                imageView.setVisibility(imageView.GONE);
                Mruler();
            }
            return false;
        });

        v1.setOnTouchListener((v,event)->{
            int eventType=event.getAction();
            if (mode!=0)return false;
            double[] c = new double[0];
            if(eventType==MotionEvent.ACTION_DOWN){
                //??????????????????????????????
                  try {
                      PhotoLoader photoloader=new PhotoLoader(Environment.getExternalStorageDirectory().getPath()+"/DCIM/stereo/picture");
                      ArrayList<String> fileList=photoloader.getPicLocation();
                      Bitmap leftBitmap = BitmapFactory.decodeStream(getAssets().open(fileList.get(0)));
                      Bitmap rightBitmap = BitmapFactory.decodeStream(getAssets().open(fileList.get(1)));
                      Bitmap result = stereoBMUtil.compute(leftBitmap, rightBitmap);
                      float[] dst = new float[2];
                      Matrix inverseMatrix = new Matrix();
                      inverseMatrix.mapPoints(dst, new float[]{event.getX(), event.getY()});
                      int dstX = (int) dst[0];
                      int dstY = (int) dst[1];
                      // ???????????????????????????
                      c = stereoBMUtil.getCoordinate(dstX, dstY);
                      Log.i("v1 ACTION_DOWN","x:"+event.getX()+" y:"+event.getY());

                  } catch (IOException e) {
                      e.printStackTrace();
                  }
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
            params.setMargins((int)event.getX(), (int)event.getY(), 0, 0);// ??????????????????????????????????????????
            textView.setLayoutParams(params);
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)textView.getLayoutParams();
            params1.setMargins((int)event.getX(), (int)event.getY()-30, 0, 0);// ??????????????????????????????????????????
            imageView.setLayoutParams(params1);
            textView.setText(c[2]+"cm");
            imageView.setVisibility(imageView.VISIBLE);
            textView.setVisibility(textView.VISIBLE);
            return false;

        });
        v2.setOnTouchListener((v,event)->{
            int eventType=event.getAction();
            if (mode!=0)return false;
            if(eventType==MotionEvent.ACTION_DOWN){
                //??????????????????????????????
                Log.i("v2 ACTION_DOWN","x:"+event.getX()+" y:"+event.getY());
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
        imageView.setVisibility(imageView.GONE);
        textView.setVisibility(textView.GONE);
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        toolbar.setBackgroundResource(R.color.black);
        btncam.setBackgroundResource(R.mipmap.init2);
        document.setBackgroundResource(R.mipmap.document);
        btncam.setOnClickListener(view -> {
            if (text.getSelectedString().equals("????????????")){
                if (isChange[0] ==1){
                    Log.d("TAG","????????????");
                    startRecordingVideo();
                    alterCam();
                }
                else{
                    stopRecorder();
                    btncam.setBackgroundResource(R.mipmap.init2);
                    Log.d("TAG","????????????");
                    timer.setVisibility(timer.GONE);
                    alterCam();
                    //?????????????????????
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
        timer.setBase(SystemClock.elapsedRealtime());//???????????????
        timer.start();
    }
    private void stopRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            //????????????
            timer.stop();
            timer.setBase(SystemClock.elapsedRealtime());//???????????????
            new RB3DAsyncTask(rb3dProgressBar,dialog,videoFinishToast).execute(fileName,fileName);
            isChange[0]=1;

        }
        broadcast();
        startPreview();
    }
    private void startPreview() {
        stopPreview();
        SurfaceTexture mSurfaceTexture = v1.getSurfaceTexture();
        Size cameraSize = getMatchingSize();
        //??????TextureView??????????????????
        mSurfaceTexture.setDefaultBufferSize(cameraSize.getWidth(),
                cameraSize.getHeight());
        //??????Surface??????????????????
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            //??????CaptureRequestBuilder,TEMPLATE_PREVIEW?????????????????????
            mPreViewBuidler = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //??????Surface?????????????????????????????????
            mPreViewBuidler.addTarget(previewSurface);
            //????????????????????????,??????????????????????????????Surface??????,
            // ??????????????????CameraCaptureSession?????????????????????,
            //???????????????????????????onConfigured??????,???????????????????????????Callback?????????????????????
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
    // ????????????????????????
    public void broadcast() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/stereo";
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        Log.d("TAG", "broadcast: success");
    }
    @SuppressLint("ClickableViewAccessibility")
    private void Mcam(){
        imageView.setVisibility(imageView.GONE);
        textView.setVisibility(textView.GONE);
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        toolbar.setBackgroundResource(R.color.black);
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        btncam.setOnTouchListener((v, event) -> {
            if (text.getSelectedString().equals("????????????")&&event.getAction()==MotionEvent.ACTION_DOWN){
                takePhoto.setVisibility(takePhoto.VISIBLE);
                try {
                    Thread.sleep(300);
                }
                catch (Exception e){
                    Log.d("TAG","take photo:"+e);
                }
            }
            if (text.getSelectedString().equals("????????????")&&event.getAction()==MotionEvent.ACTION_UP){
                takePhoto.setVisibility(takePhoto.GONE);
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
            //????????????
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //???????????????????????????
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
            Log.d("TAG", "??????????????????");
            mCameraDevice=cameraDevice;
            //?????????????????????????????? ??????????????????????????????
            config(cameraDevice);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d("TAG", "??????????????????");
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d("TAG", "??????????????????");
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
    //???????????????????????????
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCameraCaptureSession = session;
            Log.i("TAG", " startRecordingVideo  ?????????????????? ");
            updatePreview();
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
            //??????????????????????????????,?????????????????????????????????
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            //??????????????????
            int deviceWidth = displayMetrics.widthPixels;
            //??????????????????
            int deviceHeight = displayMetrics.heightPixels;
            /**
             * ??????40???,????????????????????????????????????,???????????????????????????????????????,
             * ????????????????????????????????????,??????????????????????????????,?????????????????????????????????null???Size?????????
             * ,??????????????????????????????????????????????????????
             */
            for (int j = 1; j < 41; j++) {
                for (int i = 0; i < sizes.length; i++) { //????????????Size
                    Size itemSize = sizes[i];
                    //????????????Size????????????????????????+j*5  &&  ????????????Size????????????????????????-j*5  &&  ????????????Size??????????????????????????????
                    if (itemSize.getHeight() < (deviceWidth + j * 5) && itemSize.getHeight() > (deviceWidth - j * 5)) {
                        if (selectSize != null) { //?????????????????????????????????????????????
                            if (Math.abs(deviceHeight - itemSize.getWidth()) < Math.abs(deviceHeight - selectSize.getWidth())) { //????????????????????????????????????????????????
                                selectSize = itemSize;
                                continue;
                            }
                        } else {
                            selectSize = itemSize;
                        }
                    }
                }
                if (selectSize != null) { //???????????????null ????????????????????? ????????????
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "getMatchingSize: ????????????????????????=" + selectSize.getWidth());
        Log.d("TAG", "getMatchingSize: ????????????????????????=" + selectSize.getHeight());
        return selectSize;
    }
    private void configMediaRecorder() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        File saveLocation = new File(Environment.getExternalStorageDirectory(),"/DCIM/stereo");
        saveLocation.mkdirs();
        //????????????????????????????????????????????????????????????
        File file = new File(Environment.getExternalStorageDirectory() +
                "/DCIM/stereo/" + simpleDateFormat.format(date) + "_left.mp4");
        if (file.exists()) {
            file.delete();
        }
        fileName=file.getAbsolutePath();
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        Size size = getMatchingSize();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//??????????????????
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//??????????????????
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//??????????????????
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//??????????????????????????????AAC
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//????????????????????????????????????H264
        mMediaRecorder.setVideoEncodingBitRate(8 * 1024 * 1920);//??????????????????
        mMediaRecorder.setVideoFrameRate(30);//????????????
        mMediaRecorder.setVideoSize(size.getWidth(), size.getHeight());
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());

//        Surface surface = new Surface(v1.getSurfaceTexture());
//        mMediaRecorder.setPreviewDisplay(surface);

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
                mCameraCaptureSession.stopRepeating();//??????????????????????????????????????????
                mCameraCaptureSession.close();//???????????????????????????????????????????????????????????????
                mCameraCaptureSession = null;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        configMediaRecorder();
        Size cameraSize = getMatchingSize();
        SurfaceTexture surfaceTexture;
        if (isSwithCam[0]==1)
            surfaceTexture = v2.getSurfaceTexture();
        else
            surfaceTexture = v1.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        Surface recorderSurface = mMediaRecorder.getSurface();//??????????????????????????????Surface
        //?????????2
        try {
            mPreViewBuidler = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreViewBuidler.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreViewBuidler.addTarget(previewSurface);
            mPreViewBuidler.addTarget(recorderSurface);
            //????????????????????????Arrays.asList(previewSurface,recorderSurface) 2???Surface??????????????????????????????????????????????????????
            // ?????????????????????Surface????????????????????????????????????Surface
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
            try {
                Size cameraSize = getMatchingSize();
                mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                SurfaceTexture texture1 = v1.getSurfaceTexture();
                SurfaceTexture texture2 = v2.getSurfaceTexture();
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
            //??????????????????  ?????????????????????????????????
            Size cameraSize = getMatchingSize();
            List<OutputConfiguration> configurations = new ArrayList<>();
            mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //??????????????????????????????
            SurfaceTexture texture = v1.getSurfaceTexture();
            texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
            OutputConfiguration outputConfiguration = new OutputConfiguration(new Surface(texture));
            outputConfiguration.setPhysicalCameraId(cameraID[0]);
            configurations.add(outputConfiguration);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration.getSurface()));

            //?????????2??????????????????
            SurfaceTexture texture2 = v2.getSurfaceTexture();
            texture2.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
            OutputConfiguration outputConfiguration2 = new OutputConfiguration(new Surface(texture2));
            outputConfiguration2.setPhysicalCameraId(cameraID[1]);
            configurations.add(outputConfiguration2);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration2.getSurface()));

            //???????????????
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
            if (text.getSelectedString().equals("????????????")){
                Intent intent=new Intent(MainActivity.this, PicActivity.class);
                startActivity(intent);
            }
            if (text.getSelectedString().equals("????????????")){
                Intent intent=new Intent(MainActivity.this,VideoActivity.class);
                startActivity(intent);
            }
        }
        else if (id==R.id.flash_button){
            switchFlash();
        }
        else if (id==R.id.dynamicImage){
            imageView.setVisibility(imageView.GONE);
            textView.setVisibility(textView.GONE);
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
            Log.i("TAG", "???????????? ");
            mCameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(), null, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void stopPreview() {
        //????????????????????????????????????
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }
}
