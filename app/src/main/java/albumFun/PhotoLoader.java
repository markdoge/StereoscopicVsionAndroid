package albumFun;

import android.graphics.Bitmap;
import java.util.ArrayList;

public class PhotoLoader {
    private String path;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<Bitmap> videoPreview;
    private ArrayList<String> videoLocation;
    public PhotoLoader(String file){
        path=file;
    }
    public ArrayList<Bitmap> getBitmap(){

        return bitmaps;
    }

    public ArrayList<Bitmap> getVideoPreview() {

        return videoPreview;
    }
    public ArrayList<String> getVideoLocation(){

        return videoLocation;
    }
}

