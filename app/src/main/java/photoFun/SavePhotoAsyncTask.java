package photoFun;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.Date;

public class SavePhotoAsyncTask extends AsyncTask {
    private Bitmap bitmapLeft;
    private Bitmap bitmapRight;
    private Context context;
    private BitmapSaver bitmapSaver;
    private String fileName = "stereo/pic";
    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private String picNameL;
    private String picNameR;

    public SavePhotoAsyncTask(Context context, Bitmap bitmapLeft, Bitmap bitmapRight) {
        this.context = context;
        this.bitmapLeft = bitmapLeft;
        this.bitmapRight = bitmapRight;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d("AsynTask","doInBackround");
        date = new Date();
        simpleDateFormat = new SimpleDateFormat("yyMMdd HH:mm:ss");
        String times = simpleDateFormat.format(date);
        picNameL = times + "Left.png";
        picNameR = times + "Right.png";
        bitmapSaver.saveLensFormatPic(context, bitmapLeft, picNameL, fileName);
        bitmapSaver.saveLensFormatPic(context, bitmapRight, picNameR, fileName);
        return null;
    }
}
