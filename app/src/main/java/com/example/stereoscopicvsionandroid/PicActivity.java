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
public class PicActivity extends AppCompatActivity implements OnClickAction {
    private ImageButton out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        init();
        outPic();
    }
    private void init(){
        out=findViewById(R.id.outPic);
    }
    private void outPic(){
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPic=new Intent(PicActivity.this,MainActivity.class);
                startActivity(intentPic);
            }
        });
    }
}
