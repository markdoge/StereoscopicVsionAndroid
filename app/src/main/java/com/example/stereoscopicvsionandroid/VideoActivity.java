package com.example.stereoscopicvsionandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import albumFun.PhotoLoader;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class VideoActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton outVideo;
    private ImageButton del;
    private ImageButton playBtn;
    private ImageButton lastBtn;
    private ImageButton nextBtn;
    private ImageView imageView;
    private VideoView videoView;
    private PhotoLoader photoLoader;
    private MediaController mediaController;
    private ArrayList<Bitmap> videoPreview;
    private ArrayList<String> videoLocation;
    private int photoNum;
    private int currentNum=0;
    private boolean isPlaying=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        init();
    }
    private void init(){
        outVideo=findViewById(R.id.outVideo);
        del=findViewById(R.id.delVideo);
        outVideo.setBackgroundResource(R.mipmap.ext);
        del.setBackgroundResource(R.mipmap.del);
        playBtn=findViewById(R.id.playBtn);
        lastBtn=findViewById(R.id.lastVideo);
        lastBtn.setVisibility(lastBtn.GONE);
        nextBtn=findViewById(R.id.nextVideo);
        nextBtn.setVisibility(nextBtn.GONE);
        playBtn.setVisibility(playBtn.GONE);
        imageView=findViewById(R.id.videoPreview);
        videoView=findViewById(R.id.videoView);
        videoView.setVisibility(videoView.GONE);

        playBtn.setOnClickListener(this);
        outVideo.setOnClickListener(this);
        del.setOnClickListener(this);
        lastBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        try {
            photoLoader=new PhotoLoader(Environment.getExternalStorageDirectory().getPath());
            videoPreview= photoLoader.getVideoPreview();
            videoLocation=photoLoader.getVideoLocation();
            photoNum=videoLocation.size();
            if (videoPreview.size()>0){
                playBtn.setVisibility(playBtn.VISIBLE);
                nextBtn.setVisibility(nextBtn.VISIBLE);
                lastBtn.setVisibility(nextBtn.VISIBLE);
            }
        }catch (Exception e){
            Log.d("TAG","file not find");
            Toast toast=Toast.makeText(VideoActivity.this, "还没有拍摄视频", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 1920);
            toast.show();
            try {
                Thread.sleep(1000);
                Intent intentPic=new Intent(VideoActivity.this,MainActivity.class);
                startActivity(intentPic);
            }
            catch (InterruptedException IE){
                IE.printStackTrace();
            }


        }
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id==R.id.outVideo){
            if (isPlaying==false){
                Intent intentPic=new Intent(VideoActivity.this,MainActivity.class);
                startActivity(intentPic);
            }
            else if (isPlaying==true){
                mediaController.clearDisappearingChildren();
                videoView.setVisibility(videoView.GONE);
                videoView.clearFocus();
                imageView.setVisibility(imageView.VISIBLE);
            }
        }
        else if (id==R.id.lastVideo){
            if (currentNum!=0){
                imageView.setImageBitmap(videoPreview.get(currentNum-1));
                currentNum-=1;
            }
            else {
                Toast toast=Toast.makeText(VideoActivity.this, "已经是第一个视频", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 1920);
                toast.show();
            }
        }
        else if (id==R.id.nextVideo){
            if (currentNum<photoNum){
                imageView.setImageBitmap(videoPreview.get(currentNum+1));
                currentNum+=1;
            }
            else {
                Toast toast=Toast.makeText(VideoActivity.this, "已经是最后一个视频", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 1920);
                toast.show();
            }
        }
        else if (id==R.id.playBtn){
            imageView.setVisibility(imageView.GONE);
            videoView.setVisibility(videoView.VISIBLE);
            isPlaying=true;
            //播放视频
            videoView.setVideoPath(videoLocation.get(currentNum));
            mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            videoView.requestFocus();
        }
    }
}
