package photoFun;

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.android.Utils.matToBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.calib3d.StereoBM;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Random;

public class StereoBMUtil {
    private static final String TAG = StereoBMUtil.class.getName();
    private final int imageWidth = 1280;                      // 单目图像的宽度
    private final int imageHeight = 720;                      // 单目图像的高度
    private Mat Q = new Mat();

    //映射表
    private Mat mapLx = new Mat();
    private Mat mapLy = new Mat();
    private Mat mapRx = new Mat();
    private Mat mapRy = new Mat();

    private StereoBM bm = StereoBM.create();
    private Mat xyz;

    public StereoBMUtil() {
        Mat cameraMatrixL = new Mat(3, 3, CvType.CV_64F);
        Mat distCoeffL = new Mat(5, 1, CvType.CV_64F);
        Mat cameraMatrixR = new Mat(3, 3, CvType.CV_64F);
        Mat distCoeffR = new Mat(5, 1, CvType.CV_64F);
        Mat T = new Mat(3, 1, CvType.CV_64F);
        Mat rec = new Mat(3, 1, CvType.CV_64F);
        // 左目相机标定参数 fc_left_x  0  cc_left_x  0  fc_left_y  cc_left_y  0  0  1
        cameraMatrixL.put(0, 0, 849.38718, 0, 720.28472, 0, 850.60613, 373.88887, 0, 0, 1);
        //左目相机标定参数 kc_left_01,  kc_left_02,  kc_left_03,  kc_left_04,   kc_left_05
        distCoeffL.put(0, 0, 0.01053, 0.02881, 0.00144, 0.00192, 0.00000);
        //右目相机标定参数 fc_right_x  0  cc_right_x  0  fc_right_y  cc_right_y  0  0  1
        cameraMatrixR.put(0, 0, 847.54814, 0, 664.36648, 0, 847.75828, 368.46946, 0, 0, 1);
        //右目相机标定参数 kc_right_01,  kc_right_02,  kc_right_03,  kc_right_04,   kc_right_05
        distCoeffR.put(0, 0, 0.00905, 0.02094, 0.00082, 0.00183, 0.00000);
        //T平移向量
        T.put(0, 0, -59.32102, 0.27563, -0.79807);
        //rec旋转向量
        rec.put(0, 0, -0.00927, -0.00228, -0.00070);

        Size imageSize = new Size(imageWidth, imageHeight);
        Mat R = new Mat();
        Mat Rl = new Mat();
        Mat Rr = new Mat();
        Mat Pl = new Mat();
        Mat Pr = new Mat();
        Rect validROIL = new Rect();
        Rect validROIR = new Rect();
        Calib3d.Rodrigues(rec, R);                                   //Rodrigues变换
        //图像校正之后，会对图像进行裁剪，这里的validROI指裁剪之后的区域
        Calib3d.stereoRectify(cameraMatrixL, distCoeffL, cameraMatrixR, distCoeffR, imageSize, R, T, Rl, Rr, Pl, Pr, Q, Calib3d.CALIB_ZERO_DISPARITY,
                0, imageSize, validROIL, validROIR);
        Imgproc.initUndistortRectifyMap(cameraMatrixL, distCoeffL, Rl, Pl, imageSize, CvType.CV_32FC1, mapLx, mapLy);
        Imgproc.initUndistortRectifyMap(cameraMatrixR, distCoeffR, Rr, Pr, imageSize, CvType.CV_32FC1, mapRx, mapRy);

        int blockSize = 18;
        int numDisparities = 11;
        int uniquenessRatio = 5;
        bm.setBlockSize(2 * blockSize + 5);                           //SAD窗口大小
        bm.setROI1(validROIL);                                        //左右视图的有效像素区域
        bm.setROI2(validROIR);
        bm.setPreFilterCap(61);                                       //预处理滤波器
        bm.setMinDisparity(32);                                       //最小视差，默认值为0, 可以是负值，int型
        bm.setNumDisparities(numDisparities * 16);                    //视差窗口，即最大视差值与最小视差值之差,16的整数倍
        bm.setTextureThreshold(10);
        bm.setUniquenessRatio(uniquenessRatio);                       //视差唯一性百分比,uniquenessRatio主要可以防止误匹配
        bm.setSpeckleWindowSize(100);                                 //检查视差连通区域变化度的窗口大小
        bm.setSpeckleRange(32);                                       //32视差变化阈值，当窗口内视差变化大于阈值时，该窗口内的视差清零
        bm.setDisp12MaxDiff(-1);
    }

    public Bitmap compute(Bitmap left, Bitmap right) {
        Mat rgbImageL = new Mat();
        Mat rgbImageR = new Mat();
        Mat grayImageL = new Mat();
        Mat rectifyImageL = new Mat();
        Mat rectifyImageR = new Mat();
        Mat grayImageR = new Mat();
        //用于存放每个像素点距离相机镜头的三维坐标
        xyz = new Mat();
        Mat disp = new Mat();
        bitmapToMat(left, rgbImageL);
        bitmapToMat(right, rgbImageR);
        Imgproc.cvtColor(rgbImageL, grayImageL, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rgbImageR, grayImageR, Imgproc.COLOR_BGR2GRAY);

        Imgproc.remap(grayImageL, rectifyImageL, mapLx, mapLy, Imgproc.INTER_LINEAR);
        Imgproc.remap(grayImageR, rectifyImageR, mapRx, mapRy, Imgproc.INTER_LINEAR);

        bm.compute(rectifyImageL, rectifyImageR, disp);
        Calib3d.reprojectImageTo3D(disp, xyz, Q, true);
        Core.multiply(xyz, new Mat(xyz.size(), CvType.CV_32FC3, new Scalar(16, 16, 16)), xyz);

        // 用于显示处理
        Mat disp8U = new Mat(disp.rows(), disp.cols(), CvType.CV_8UC1);
        disp.convertTo(disp, CvType.CV_32F, 1.0 / 16);               //除以16得到真实视差值
        Core.normalize(disp, disp8U, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
        Imgproc.medianBlur(disp8U, disp8U, 9);
        Bitmap resultBitmap = Bitmap.createBitmap(disp8U.cols(), disp8U.rows(), Bitmap.Config.ARGB_8888);
        matToBitmap(disp8U, resultBitmap);
        return resultBitmap;
    }

    public double getCoordinate(int dstX, int dstY) {
        double f1;
        try {
            f1 = xyz.get(dstX,dstY)[2];
        }
        catch (Exception e){
            f1 = Double.MAX_VALUE;
        }
        return f1;
    }
}
