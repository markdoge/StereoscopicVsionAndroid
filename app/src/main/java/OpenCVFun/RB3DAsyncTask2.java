package OpenCVFun;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import java.io.File;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.videoio.Videoio.CAP_PROP_FOURCC;
import static org.opencv.videoio.Videoio.CAP_PROP_FPS;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_COUNT;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH;

public class  RB3DAsyncTask2 extends AsyncTask<String,Integer,String> {
    private Toast toast;
    private ProgressBar rb3dProgressBar;
    private ProgressDialog dialog;
    private static int frame_num;
    private static int frame_count;
    public static void delete(File file) {

        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    static int offset=-50;//如果合成的视频红蓝差异明显，可以适当增大offset，以使左图向右图偏移
    //上一届基于深度图动态计算offset，但实际上和固定的offset几乎没有区别

    public RB3DAsyncTask2(ProgressBar pb,ProgressDialog dialog,Toast toast){
        super();
        this.rb3dProgressBar=pb;
        this.dialog=dialog;
        this.toast=toast;
    }

    //第一阶段————准备阶段让进度条显示
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //rb3dProgressBar.setVisibility(View.VISIBLE);
        dialog.show();
    }

    //第二阶段——执行
    @Override
    protected String doInBackground(String... params) {
        publishProgress(0);

        int rc;
        String leftVideo=params[0];
        String rightVideo=params[1];
        String format=leftVideo.substring(leftVideo.lastIndexOf(".")+1);
        format=".avi";
        Boolean deleted=false;

        String name_left=leftVideo.substring(leftVideo.lastIndexOf("/")+1);
        String name_right=rightVideo.substring(rightVideo.lastIndexOf("/")+1);
        String folder=leftVideo.substring(0,leftVideo.lastIndexOf("/")+1);
        String temp_folder=folder+"temp/";
        String leftVideo_bak=leftVideo;
        String rightVideo_bak=rightVideo;
        File save_location=new File(temp_folder);
        save_location.mkdirs();

        //预处理，转化视频格式为MJPG编码的AVI，同时提取音频
        FFmpeg.execute("-i \""+leftVideo+"\" -vcodec mjpeg \""+temp_folder+"output_left.mjpeg\"");
        String command="-i \""+temp_folder+"output_left.mjpeg\""+" -c:v copy -c:a copy  \""+temp_folder+name_left.substring(0,name_left.lastIndexOf("."))+".avi\"";
        System.out.println(command);
        rc=FFmpeg.execute(command);
        publishProgress(1);

        FFmpeg.execute("-i \""+rightVideo+"\" -vcodec mjpeg \""+temp_folder+"output_right.mjpeg\"");
        command="-i \""+temp_folder+"output_right.mjpeg\""+" -c:v copy -c:a copy  \""+temp_folder+name_right.substring(0,name_right.lastIndexOf("."))+".avi\"";
        System.out.println(command);
        rc=FFmpeg.execute(command);
        publishProgress(3);

        command="-i \""+leftVideo+"\" -q:a 0 -map a \""+temp_folder+"output.aac\"";
        System.out.println(command);
        rc=FFmpeg.execute(command);
        publishProgress(4);

        if (rc == Config.RETURN_CODE_SUCCESS) {
            leftVideo=temp_folder+name_left.substring(0,name_left.lastIndexOf("."))+".avi";
            rightVideo=temp_folder+name_right.substring(0,name_right.lastIndexOf("."))+".avi";
        }
        leftVideo=temp_folder+name_left.substring(0,name_left.lastIndexOf("."))+".avi";
        rightVideo=temp_folder+name_right.substring(0,name_right.lastIndexOf("."))+".avi";
        VideoCapture video_L=new VideoCapture(leftVideo);
        VideoCapture video_R=new VideoCapture(rightVideo);

        //输出视频配置参数
        String name=leftVideo_bak.substring(leftVideo_bak.lastIndexOf("/")+1);
        name=name.substring(0,name.lastIndexOf("_"));//文件名
        name=leftVideo_bak.substring(0,leftVideo_bak.lastIndexOf("/")+1)+name;
        String outputName=name+"_3d";

        int fourcc= (int) video_L.get(CAP_PROP_FOURCC);//编码格式,此参数读取异常，原因未知
        double fps=video_L.get(CAP_PROP_FPS);//帧率
        int width= (int) video_L.get(CAP_PROP_FRAME_WIDTH),height= (int) video_L.get(CAP_PROP_FRAME_HEIGHT);//分辨率
        frame_num= (int) (Math.min(video_L.get(CAP_PROP_FRAME_COUNT), video_R.get(CAP_PROP_FRAME_COUNT)));//帧数

        //设置输出格式
        format=format.toLowerCase();
        if (format.equals("avi")) {
            name = name + ".avi";
            fourcc= VideoWriter.fourcc('M', 'J', 'P', 'G');
        }
        else if(format.equals("mp4")){
            name=name+".mp4";
            fourcc=VideoWriter.fourcc('X', '2', '6', '4');
        }
        else{
            name = name + ".avi";
            fourcc=VideoWriter.fourcc('M', 'J', 'P', 'G');
        }
        //outputName=outputName+name.substring(name.lastIndexOf("."));
        outputName=outputName+".mp4";

        VideoWriter outputVideo=new VideoWriter(
                name,
                fourcc,
                fps,
                new Size(width,height),
                true
        );
        System.out.println(name);
        //逐帧读取视频
        Mat leftFrame=new Mat();
        Mat rightFrame=new Mat();
        frame_count=0;
        Log.d("TAG","共有"+frame_num+"帧");
        publishProgress(20);

        int progress_i=20;
        while (true){
            frame_count++;
            if(video_L.read(leftFrame)&&video_R.read(rightFrame)){
                if(frame_count%20==0){
                    Log.d("TAG","正在处理第"+frame_count+"帧");
                }

                Mat tmp=createFromImg(leftFrame,rightFrame);
                if (tmp==null||tmp.empty()){
                    Log.d("TAG",""+frame_count+" null");
                    continue;
                }

                //imwrite(String.format("%s%s%4d.jpg",temp,name,frame_count),tmp);
                outputVideo.write(tmp);
                publishProgress(frame_count*50/frame_num+20);
            }
            else{
                break;
            }
        }

        video_L.release();
        video_R.release();
        outputVideo.release();
        publishProgress(99);

        //还原音频流
        if(new File(name).exists()){
            command="-y -i \""+name+"\" -i \""+temp_folder+"output.aac\""+" -map 0:v -map 1:a -c:v libx264 -c:a aac \""+outputName+"\"";
            System.out.println(command);
            FFmpeg.execute(command);
            publishProgress(99);
            delete(new File(name));
        }
        else{
            System.out.println(name+" not exist");
        }

        //删除中间文件
        if(deleted){
            File file=new File(leftVideo_bak);
            delete(file);
            file=new File(rightVideo_bak);
            delete(file);
        }
        Log.d("TAG","合成完成");
        delete(save_location);
        publishProgress(100);
        return "";
    }

    //第三阶段，拿到结果，更新ui
    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
        toast.show();
        dialog.dismiss();
        rb3dProgressBar.setVisibility(View.GONE);
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        rb3dProgressBar.setProgress(values[0]);
        dialog.setProgress(values[0]);
    }


    //合成单张图片为红蓝3D,作为供用户调用的API
    public static String createFromImg(String leftImg,String rightImg,String format,Boolean deleted){

        Mat left=imread(leftImg);
        Mat right=imread(rightImg);

        if (left.empty()||right.empty())
            return "";

        Mat res=createFromImg(left,right);
        String name=leftImg.substring(leftImg.lastIndexOf("\\")+1);
        name=name.substring(0,name.lastIndexOf("_"));


        format=format.toLowerCase();
        if (format.equals("jpg")) {
            name = name + ".jpg";
        }
        else if(format.equals("png")){
            name=name+".png";
        }
        else{
            name=name+".jpg";
        }

        if (res==null||res.empty())
            return "";
        imwrite(name,res);

        if(deleted){
            File file=new File(leftImg);
            file.delete();
            file=new File(rightImg);
            file.delete();
        }

        return name;
    }

    public static String createFromImg(String leftImg,String rightImg){
        String format=leftImg.substring(leftImg.lastIndexOf(".")+1);
        return createFromImg(leftImg,rightImg,format,true);
    }

    public static Mat createFromImg(Mat leftImg, Mat rightImg){

        if (leftImg.empty()||rightImg.empty())
            return null;
        //Mat depth=DepthMap.createDepthMapFromImg(leftImg,rightImg);
        Mat res=new Mat(leftImg.size(), leftImg.type());
        byte[] rowDataLeft=new byte[leftImg.channels()*leftImg.width()];
        byte[] rowDataRight=new byte[leftImg.channels()*leftImg.width()];

        for(int row=0;row<leftImg.height();row++){

            leftImg.get(row,0,rowDataLeft);
            rightImg.get(row,0,rowDataRight);
            //每次读取一行数据

            for(int col=0;col<rowDataLeft.length/leftImg.channels();col++){
                //顺序为BGR,将左图的R通道覆盖到右图上
                //一般红蓝眼镜都是左红右蓝
                if(col+offset>=0&&col+offset<rowDataLeft.length/leftImg.channels()) {
                    rowDataRight[col * 3 + 2] = rowDataLeft[(col+offset) * 3 + 2];
                }

            }
            res.put(row,0,rowDataRight);
        }

        return res;
    }
}
