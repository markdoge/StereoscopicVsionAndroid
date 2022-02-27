package photoFun;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.SortedSet;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean isPreview;
    private Context context;
    private final SizeMap mPreviewSizes = new SizeMap();
    private final SizeMap mPictureSizes = new SizeMap();
    private int mDisplayOrientation;
    private AspectRatio mAspectRatio;

    public CameraPreview(Context context, Camera mCamera) {
        super(context);
        this.context = context;
        this.mCamera = mCamera;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mDisplayOrientation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        mAspectRatio = AspectRatio.of(9, 16);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            //设置设备高宽比
            mAspectRatio = getDeviceAspectRatio((Activity) context);
            //设置预览方向
            mCamera.setDisplayOrientation(getDisplayOrientation());
            Camera.Parameters parameters = mCamera.getParameters();
            //获取所有支持的预览尺寸
            mPreviewSizes.clear();
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                int width = Math.min(size.width, size.height);
                int heigth = Math.max(size.width, size.height);
                mPreviewSizes.add(new Size(width, heigth));
            }
            //获取所有支持的图片尺寸
            mPictureSizes.clear();
            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                int width = Math.min(size.width, size.height);
                int heigth = Math.max(size.width, size.height);
                mPictureSizes.add(new Size(width, heigth));
            }
            Size previewSize = chooseOptimalSize(mPreviewSizes.sizes(mAspectRatio));
            Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
            //设置相机参数
            parameters.setPreviewSize(Math.max(previewSize.getWidth(), previewSize.getHeight()), Math.min(previewSize.getWidth(), previewSize.getHeight()));
            parameters.setPictureSize(Math.max(pictureSize.getWidth(), pictureSize.getHeight()), Math.min(pictureSize.getWidth(), pictureSize.getHeight()));
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setRotation(getDisplayOrientation());
            mCamera.setParameters(parameters);
            //把这个预览效果展示在SurfaceView上面
            mCamera.setPreviewDisplay(holder);
            //开启预览效果
            mCamera.startPreview();
            isPreview = true;
        } catch (Exception e) {
            Log.e("CameraPreview", "相机预览错误: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        //停止预览效果
        mCamera.stopPreview();
        //重新设置预览效果
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            if (isPreview) {
                //正在预览
                mCamera.stopPreview();
                mCamera.release();
            }
        }
    }

    private AspectRatio getDeviceAspectRatio(Activity activity) {
        int width = activity.getWindow().getDecorView().getWidth();
        int height = activity.getWindow().getDecorView().getHeight();
        return AspectRatio.of(Math.min(width, height), Math.max(width, height));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = getWidth();
        final int surfaceHeight = getHeight();
        if (isLandscape(mDisplayOrientation)) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = new Size(desiredWidth, desiredHeight);
        if (sizes != null && !sizes.isEmpty()) {
            for (Size size : sizes) {
                if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                    return size;
                }
                result = size;
            }
        }
        return result;
    }

    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == Surface.ROTATION_90 ||
                orientationDegrees == Surface.ROTATION_270);
    }

    private int getDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int orientation;
        int degrees = 0;
        if (mDisplayOrientation == Surface.ROTATION_0) {
            degrees = 0;
        } else if (mDisplayOrientation == Surface.ROTATION_90) {
            degrees = 90;
        } else if (mDisplayOrientation == Surface.ROTATION_180) {
            degrees = 180;
        } else if (mDisplayOrientation == Surface.ROTATION_270) {
            degrees = 270;
        }
        orientation = (degrees + 45) / 90 * 90;
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation - orientation + 360) % 360;
        } else {
            // back-facing
            result = (info.orientation + orientation) % 360;
        }
        return result;
    }
}
