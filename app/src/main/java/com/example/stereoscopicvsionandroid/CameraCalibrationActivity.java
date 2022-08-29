package com.example.stereoscopicvsionandroid;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import albumFun.JsonBuilder;

public class CameraCalibrationActivity extends AppCompatActivity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "calibration";

    private CameraBridgeViewBase mOpenCvCameraView;
    private CameraCalibrator mCalibrator;
    private OnCameraFrameRender mOnCameraFrameRender;
    private int mWidth;
    private int mHeight;
    private org.opencv.android.JavaCameraView javaCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraCalibrationActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public CameraCalibrationActivity() {
        Log.d(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_calibration);
        init();
    }

    private void init() {
        try {
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_calibration_java_surface_view);
        } catch (Exception e) {
            Log.d("TAG", "findViewById Exception: " + e.toString());

        }
        try {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        } catch (Exception e) {
            Log.d("TAG", "setVisibility Exception: " + e.toString());
        }
        mOpenCvCameraView.setCvCameraViewListener(this);
        setTitle(R.string.calibrate_title1);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d(TAG, "menu success");
        getMenuInflater().inflate(R.menu.calibration, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.preview_mode).setEnabled(true);
        if (!mCalibrator.isCalibrated())
            menu.findItem(R.id.preview_mode).setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                onResume();
                Intent intent = new Intent(CameraCalibrationActivity.this, CameraCalibration1Activity.class);
                startActivity(intent);
                return true;
            case R.id.calibration:
                mOnCameraFrameRender =
                        new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
                item.setChecked(true);
                return true;
            case R.id.undistortion:
                mOnCameraFrameRender =
                        new OnCameraFrameRender(new UndistortionFrameRender(mCalibrator));
                item.setChecked(true);
                return true;
            case R.id.comparison:
                mOnCameraFrameRender =
                        new OnCameraFrameRender(new ComparisonFrameRender(mCalibrator, mWidth, mHeight, getResources()));
                item.setChecked(true);
                return true;
            case R.id.calibrate:
                final Resources res = getResources();
                if (mCalibrator.getCornersBufferSize() < 2) {
                    (Toast.makeText(this, res.getString(R.string.more_samples), Toast.LENGTH_SHORT)).show();
                    return true;
                }

                mOnCameraFrameRender = new OnCameraFrameRender(new PreviewFrameRender());
                new AsyncTask<Void, Void, Void>() {
                    private ProgressDialog calibrationProgress;

                    @Override
                    protected void onPreExecute() {
                        calibrationProgress = new ProgressDialog(CameraCalibrationActivity.this);
                        calibrationProgress.setTitle(res.getString(R.string.calibrating));
                        calibrationProgress.setMessage(res.getString(R.string.please_wait));
                        calibrationProgress.setCancelable(false);
                        calibrationProgress.setIndeterminate(true);
                        calibrationProgress.show();
                    }

                    @Override
                    protected Void doInBackground(Void... arg0) {
                        mCalibrator.calibrate();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        calibrationProgress.dismiss();
                        mCalibrator.clearCorners();
                        mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
                        String resultMessage = (mCalibrator.isCalibrated()) ?
                                res.getString(R.string.calibration_successful) + " " + mCalibrator.getAvgReprojectionError() :
                                res.getString(R.string.calibration_unsuccessful);
                        (Toast.makeText(CameraCalibrationActivity.this, resultMessage, Toast.LENGTH_SHORT)).show();
                        Log.d("TAG",resultMessage);

                        if (mCalibrator.isCalibrated()) {
                            CalibrationResult.save(CameraCalibrationActivity.this,
                                    mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients());
                            Log.d("TAG","after save");
                            CalibrationResult.tryLoad(CameraCalibrationActivity.this,
                                    mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients());
                        }
                    }
                }.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCameraViewStarted(int width, int height) {
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            mCalibrator = new CameraCalibrator(mWidth, mHeight,this.getString(R.string.calibrate1));
            if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
                mCalibrator.setCalibrated();
            }

            mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
        }
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return mOnCameraFrameRender.render(inputFrame);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch invoked");

        mCalibrator.addCorners();
        return false;
    }
}
