package photoFun;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    public static Bitmap setTakePicktrueOrientation(int id, Bitmap bitmap) {
        //如果返回的图片宽度小于高度，说明FrameWork层已经做过处理直接返回即可
        if (bitmap.getWidth() < bitmap.getHeight()) {
            return bitmap;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        bitmap = rotaingImageView(id, info.orientation, bitmap);
        return bitmap;
    }

    private static Bitmap rotaingImageView(int id, int angle, Bitmap bitmap) {
        //矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static boolean saveBitmap(Bitmap bitmap, String path) {

        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            boolean b = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
