package OpenCVFun;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.videoio.Videoio.*;

public class RB3D {


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



    final static String temp="temp\\";
    static int offset=-50;//如果合成的视频红蓝差异明显，可以适当增大offset，以使左图向右图偏移
    //上一届基于深度图动态计算offset，但实际上和固定的offset几乎没有区别

    //leftVideo:左视频路径，rightVideo右视频路径，format：输出视频的格式，deleted：任务完成后是否删除左右视频
    public static String createFromVideo(String leftVideo, String rightVideo,String format,boolean deleted){
        int rc;

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

        FFmpeg.execute("-i \""+rightVideo+"\" -vcodec mjpeg \""+temp_folder+"output_right.mjpeg\"");
        command="-i \""+temp_folder+"output_right.mjpeg\""+" -c:v copy -c:a copy  \""+temp_folder+name_right.substring(0,name_right.lastIndexOf("."))+".avi\"";
        System.out.println(command);
        rc=FFmpeg.execute(command);

        command="-i \""+leftVideo+"\" -q:a 0 -map a \""+temp_folder+"output.mp3\"";
        System.out.println(command);
        rc=FFmpeg.execute(command);


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
        int frame_num= (int) (Math.min(video_L.get(CAP_PROP_FRAME_COUNT), video_R.get(CAP_PROP_FRAME_COUNT)));//帧数

        //设置输出格式
        format=format.toLowerCase();
        if (format.equals("avi")) {
            name = name + ".avi";
            fourcc=VideoWriter.fourcc('M', 'J', 'P', 'G');
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
        int frame_count=0;
        System.out.println("共有"+frame_num+"帧");
        while (true){
            frame_count++;
            if(video_L.read(leftFrame)&&video_R.read(rightFrame)){
                if(frame_count%20==0){
                    System.out.println("正在处理第"+frame_count+"帧");
                }

                Mat tmp=createFromImg(leftFrame,rightFrame);
                if (tmp==null||tmp.empty()){
                    System.out.println(""+frame_count+" null");
                    continue;
                }

                //imwrite(String.format("%s%s%4d.jpg",temp,name,frame_count),tmp);
                outputVideo.write(tmp);
            }
            else{
                break;
            }
        }

        video_L.release();
        video_R.release();
        outputVideo.release();


        //还原音频流
        if(new File(name).exists()){
            command="-y -i \""+name+"\" -i \""+temp_folder+"output.mp3\""+" -c copy -map 0:v:0 -map 1:a:0 \""+outputName+"\"";
            System.out.println(command);
            FFmpeg.execute(command);
            delete(new File(name));
        }

        //删除中间文件
        if(deleted){
            File file=new File(leftVideo_bak);
            delete(file);
            file=new File(rightVideo_bak);
            delete(file);
        }
        System.out.println("合成完成");
        delete(save_location);

        return "";
    }

    //默认以leftVideo的格式作为合成视频的输出格式并删除中间文件,若无法支持该格式则采用mp4输出
    public static String createFromVideo(String leftVideo, String rightVideo){
        return createFromVideo(leftVideo,rightVideo,true);
    }

    public static String createFromVideo(String leftVideo, String rightVideo,String format){
        return createFromVideo(leftVideo,rightVideo,format,true);
    }

    public static String createFromVideo(String leftVideo, String rightVideo,boolean deleted){
        String format=leftVideo.substring(leftVideo.lastIndexOf(".")+1);
        return createFromVideo(leftVideo,rightVideo,format,deleted);
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

    public static Mat createFromImg(Mat leftImg,Mat rightImg){

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
