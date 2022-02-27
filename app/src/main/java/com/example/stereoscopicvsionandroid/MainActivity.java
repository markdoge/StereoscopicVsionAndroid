package com.example.stereoscopicvsionandroid;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.os.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import photoFun.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String KEY_IMAGE_PATH = "imagePath";
    private FrameLayout mPreviewLayout;
    private RelativeLayout mPhotoLayout;
    private RelativeLayout mConfirmLayout;
    private ImageView mFlashButton;
    private ImageView btncam;
    private ImageView mCancleSaveButton;
    private static final String TAG = "TAG";
    private ImageView mSaveButton;
    private TestScroller text;
    private OverCameraView mOverCameraView;
    private Camera mCamera;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private ImageButton document;
    private boolean isFlashing;
    private byte[] imageData;
    final int[] isChange = {1};
    private boolean isTakePhoto;
    private boolean isFoucing;

    @RequiresApi(api = Build.VERSION_CODES.Q)
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void init() {
        document = findViewById(R.id.document);
        document.setVisibility(document.GONE);
        mPreviewLayout = findViewById(R.id.camera_preview_layout);
        mPhotoLayout = findViewById(R.id.ll_photo_layout);
        mConfirmLayout = findViewById(R.id.ll_confirm_layout);
        btncam = findViewById(R.id.btncam);
        btncam.setVisibility(btncam.GONE);
        mCancleSaveButton = findViewById(R.id.undoButton);
        mSaveButton = findViewById(R.id.saveButton);
        mFlashButton = findViewById(R.id.flash_button);
        text=findViewById(R.id.selecteText);

        mCancleSaveButton.setOnClickListener(this);
        mFlashButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);

        text.setOnTouchListener((v, event) -> {
            Log.d(TAG,"TouchBar");
            if (Camera.getNumberOfCameras()<=2){
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
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("ClickableViewAccessibility")
    private void Mcam(){
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
        btncam.setBackgroundResource(R.mipmap.init3);
        document.setBackgroundResource(R.mipmap.document);
        jump('p');
        btncam.setOnTouchListener((v, event) -> {
            Log.d(TAG,"move");
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
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void Mvideo(){
        btncam.setVisibility(btncam.VISIBLE);
        document.setVisibility(document.VISIBLE);
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
        document.setBackgroundResource(R.color.transparent);
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
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
    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open(0);
        CameraPreview preview = new CameraPreview(this, mCamera);
        mOverCameraView = new OverCameraView(this);
        mPreviewLayout.addView(preview);
        mPreviewLayout.addView(mOverCameraView);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isFoucing) {
                float x = event.getX();
                float y = event.getY();
                isFoucing = true;
                if (mCamera != null && !isTakePhoto) {
                    mOverCameraView.setTouchFoucusRect(mCamera, autoFocusCallback, x, y);
                }
                mRunnable = () -> {
                    Toast.makeText(MainActivity.this, "自动聚焦超时,请调整合适的位置拍摄！", Toast.LENGTH_SHORT);
                    isFoucing = false;
                    mOverCameraView.setFoucuing(false);
                    mOverCameraView.disDrawTouchFocusRect();
                };
                //设置聚焦超时
                mHandler.postDelayed(mRunnable, 3000);
            }
        }
        return super.onTouchEvent(event);
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            isFoucing = false;
            mOverCameraView.setFoucuing(false);
            mOverCameraView.disDrawTouchFocusRect();
            //停止聚焦超时回调
            mHandler.removeCallbacks(mRunnable);
        }
    };

    private void takePhoto() {
        isTakePhoto = true;
        //调用相机拍照
        mCamera.takePicture(null, null, null, (data, camera1) -> {
            //视图动画
            mPhotoLayout.setVisibility(View.GONE);
            mConfirmLayout.setVisibility(View.VISIBLE);
            imageData = data;
            //停止预览
            mCamera.stopPreview();
        });
    }

    private void switchFlash() {
        isFlashing = !isFlashing;
        mFlashButton.setImageResource(isFlashing ? R.mipmap.flash_open : R.mipmap.flash_close);
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(isFlashing ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Toast.makeText(this, "该设备不支持闪光灯", Toast.LENGTH_SHORT);
        }
    }

    private void cancleSavePhoto() {
        mPhotoLayout.setVisibility(View.VISIBLE);
        mConfirmLayout.setVisibility(View.GONE);
        //开始预览
        mCamera.startPreview();
        imageData = null;
        isTakePhoto = false;
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
        FileOutputStream fos = null;
        String cameraPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM" + File.separator + "Camera";
        //相册文件夹
        File cameraFolder = new File(cameraPath);
        if (!cameraFolder.exists()) {
            cameraFolder.mkdirs();
        }
        //保存的图片文件
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String imagePath = cameraFolder.getAbsolutePath() + File.separator + "IMG_" + simpleDateFormat.format(new Date()) + ".jpg";
        File imageFile = new File(imagePath);
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(imageData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    Bitmap retBitmap = BitmapFactory.decodeFile(imagePath);
                    retBitmap = BitmapUtils.setTakePicktrueOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, retBitmap);
                    BitmapUtils.saveBitmap(retBitmap, imagePath);
                    Intent intent = new Intent();
                    intent.putExtra(KEY_IMAGE_PATH, imagePath);
                    setResult(RESULT_OK, intent);
                } catch (IOException e) {
                    setResult(RESULT_FIRST_USER);
                    e.printStackTrace();
                }
            }
            mPhotoLayout.setVisibility(View.VISIBLE);
            mConfirmLayout.setVisibility(View.GONE);
            mCamera.startPreview();
            imageData = null;
            isTakePhoto = false;
        }
    }

}
