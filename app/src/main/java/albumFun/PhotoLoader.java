package albumFun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PhotoLoader {
    private String path;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> picLocation;
    public PhotoLoader(String file){
        path=file;
        File[] fileList=new File(path).listFiles();
        bitmaps=new ArrayList<Bitmap>();
        picLocation=new ArrayList<String>();
        for (int i=0;i<fileList.length;i++){
            try {
                Bitmap b= BitmapFactory.decodeStream(new FileInputStream(fileList[i]));
                picLocation.add(fileList[i].getAbsolutePath());
                bitmaps.add(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    public ArrayList<Bitmap> getBitmap(){

        return bitmaps;
    }

    public ArrayList<String> getPicLocation() {
        return picLocation;
    }
}

