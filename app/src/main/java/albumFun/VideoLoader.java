package albumFun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.media.ThumbnailUtils;
import android.util.Size;

public class VideoLoader {
    private String path;
    private ArrayList<Bitmap> videoPreview;
    private ArrayList<String> videoLocation;

    public VideoLoader(String file){
        path=file;
        File[] fileList=new File(path).listFiles();
        videoPreview=new ArrayList<Bitmap>();
        videoLocation=new ArrayList<String>();
        for (int i=0;i<fileList.length;i++){
            if(fileList[i].isDirectory())
                continue;
            //Bitmap b= BitmapFactory.decodeStream(new FileInputStream(fileList[i]));
            //Bitmap b=videoThumbnailFromPath(fileList[i].getAbsolutePath(),480,270);
            Bitmap b= null;
            try {
                b = ThumbnailUtils.createVideoThumbnail(fileList[i],new Size(480,270),null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            videoLocation.add(fileList[i].getAbsolutePath());
            videoPreview.add(b);
        }
    }

    public ArrayList<Bitmap> getVideoPreview() {
        return videoPreview;
    }

    public ArrayList<String> getVideoLocation() {
        return videoLocation;
    }
}