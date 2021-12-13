package com.example.stereoscopicvsionandroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class VideoActivity extends AppCompatActivity implements OnClickAction {
    private ImageButton outVideo;
    private ImageButton del;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        init();
        jumpOutVideo();
    }
    private void init(){
        outVideo=findViewById(R.id.outVideo);
        del=findViewById(R.id.delVideo);
        outVideo.setBackgroundResource(R.mipmap.ext);
        del.setBackgroundResource(R.mipmap.del);
    }
    private void jumpOutVideo(){
        outVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPic=new Intent(VideoActivity.this,MainActivity.class);
                startActivity(intentPic);
            }
        });
    }
}
