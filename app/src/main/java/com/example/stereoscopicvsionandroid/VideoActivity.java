package com.example.stereoscopicvsionandroid;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

import albumFun.VideoLoader;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class VideoActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton outVideo;
    private ImageButton del;
    private ImageButton share;
    private ImageButton playBtn;
    private ImageButton lastBtn;
    private ImageButton nextBtn;
    private ImageView imageView;
    private VideoView videoView;
    private VideoLoader videoLoader;
    private MediaController mediaController;
    private ArrayList<Bitmap> videoPreview;
    private ArrayList<String> videoLocation;
    private int videoNum;
    private int currentNum = 0;
    private boolean isPlaying = false;
    private RelativeLayout toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        init();
    }

    private void init() {
        toolbar = findViewById(R.id.video_tool_bar);
        outVideo = findViewById(R.id.outVideo);
        del = findViewById(R.id.delVideo);
        share = findViewById(R.id.shareVideo);
        outVideo.setBackgroundResource(R.mipmap.ext);
        del.setBackgroundResource(R.mipmap.del);
        share.setBackgroundResource(R.mipmap.share_btn);
        playBtn = findViewById(R.id.playBtn);
        lastBtn = findViewById(R.id.lastVideo);
        lastBtn.setVisibility(lastBtn.GONE);
        nextBtn = findViewById(R.id.nextVideo);
        nextBtn.setVisibility(nextBtn.GONE);
        playBtn.setVisibility(playBtn.GONE);
        imageView = findViewById(R.id.videoPreview);
        videoView = findViewById(R.id.videoView);
        videoView.setVisibility(videoView.GONE);

        playBtn.setOnClickListener(this);
        outVideo.setOnClickListener(this);
        del.setOnClickListener(this);
        share.setOnClickListener(this);
        lastBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        try {
            videoLoader = new VideoLoader(Environment.getExternalStorageDirectory().getPath() + "/DCIM/stereo");
            videoPreview = videoLoader.getVideoPreview();
            videoLocation = videoLoader.getVideoLocation();
            videoNum = videoLocation.size();
            Log.d("VideoActivity", "video size: " + videoNum);
            System.out.println(videoNum);
            if (videoPreview.size() > 0) {
                imageView.setImageBitmap(videoPreview.get(0));
                playBtn.setVisibility(playBtn.VISIBLE);
                nextBtn.setVisibility(nextBtn.VISIBLE);
                lastBtn.setVisibility(nextBtn.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("VideoActivity", e.toString());
            Toast toast = Toast.makeText(VideoActivity.this, "还没有拍摄视频", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 1920);
            toast.show();
            try {
                Thread.sleep(1000);
                Intent intentPic = new Intent(VideoActivity.this, MainActivity.class);
                startActivity(intentPic);
            } catch (InterruptedException IE) {
                IE.printStackTrace();
            }


        }
    }

    private Uri getVideoContentUri(Context context, String filePath) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=?",
                new String[]{filePath}, null);
        Uri uri = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                Uri baseUri = Uri.parse("content://media/external/video/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }
            cursor.close();
        }

        //如果使用fileProvider获取失败，则使用此方法
        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }

    private void outVideo() {
        if (isPlaying == false) {
            Intent intentPic = new Intent(VideoActivity.this, MainActivity.class);
            startActivity(intentPic);
        } else if (isPlaying == true) {
            mediaController.clearDisappearingChildren();
            videoView.setVisibility(videoView.GONE);
            videoView.clearFocus();
            imageView.setVisibility(imageView.VISIBLE);
            playBtn.setVisibility(playBtn.VISIBLE);
            lastBtn.setVisibility(lastBtn.VISIBLE);
            nextBtn.setVisibility(nextBtn.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            isPlaying = false;
        }
    }

    private void lastVideo() {
        if (currentNum != 0) {
            imageView.setImageBitmap(videoPreview.get(currentNum - 1));
            currentNum -= 1;
        } else {
            Toast toast = Toast.makeText(VideoActivity.this, "已经是第一个视频", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 1920);
            toast.show();
        }
    }

    private void nextVideo() {
        if (currentNum < videoNum) {
            imageView.setImageBitmap(videoPreview.get(currentNum + 1));
            currentNum += 1;
        } else {
            Toast toast = Toast.makeText(VideoActivity.this, "已经是最后一个视频", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 1920);
            toast.show();
        }
    }

    private void playVideo() {
        imageView.setVisibility(imageView.GONE);
        videoView.setVisibility(videoView.VISIBLE);
        playBtn.setVisibility(playBtn.GONE);
        lastBtn.setVisibility(lastBtn.GONE);
        nextBtn.setVisibility(nextBtn.GONE);
        toolbar.setVisibility(View.GONE);
        isPlaying = true;
        //播放视频
        videoView.setVideoPath(videoLocation.get(currentNum));
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
    }

    private void delVideo() {
        new File(videoLocation.get(currentNum)).delete();
        videoNum--;
        videoLocation.remove(currentNum);
        videoPreview.remove(currentNum);
        if (currentNum > 0) {
            currentNum--;
            imageView.setImageBitmap(videoPreview.get(currentNum));
        } else {
            if (videoNum > 0) {
                imageView.setImageBitmap(videoPreview.get(currentNum));
            } else finish();
        }
    }

    private void shareVideo() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        // 比如发送二进制文件数据流内容（比如图片、视频、音频文件等等）
        // 指定发送的内容 (EXTRA_STREAM 对于文件 Uri )
        Uri fileUri = getVideoContentUri(VideoActivity.this, videoLocation.get(currentNum));
        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        // 指定发送内容的类型 (MIME type)
        sendIntent.setType("video/mp4");
        startActivity(Intent.createChooser(sendIntent, ""));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.outVideo:
                outVideo();
                break;
            case R.id.lastVideo:
                lastVideo();
                break;
            case R.id.nextVideo:
                nextVideo();
                break;
            case R.id.playBtn:
                playVideo();
                break;
            case R.id.delVideo:
                delVideo();
                break;
            case R.id.shareVideo:
                try {
                    shareVideo();
                } catch (Exception e) {
                    Log.e("VideoActivity", e.toString());
                }
                break;
        }
    }
}
