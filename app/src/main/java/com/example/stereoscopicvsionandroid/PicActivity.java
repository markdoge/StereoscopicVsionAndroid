package com.example.stereoscopicvsionandroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PicActivity extends AppCompatActivity implements OnClickAction {
    private ImageButton out;
    private TextView fText;
    private ImageButton picSetting;
    private static final String TAG = "PicTAG";
    private SeekBar FData;
    private SeekBar setDestece;
    private ImageButton del;
    final int[] isSetting={1};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        init();
        appFunction();
    }
    private void init(){
        out=findViewById(R.id.outPic);
        FData=findViewById(R.id.setAperture);
        del=findViewById(R.id.del);
        FData.setProgress(1100);
        picSetting=findViewById(R.id.picSetting);
        del.setBackgroundResource(R.mipmap.del);
        picSetting.setBackgroundResource(R.mipmap.undo);
        fText=findViewById(R.id.fText);
        setDestece=findViewById(R.id.setDestece);
        fText.setText("f11.0");
        setDestece.setVisibility(setDestece.GONE);
        FData.setVisibility(FData.GONE);
        fText.setVisibility(fText.GONE);
    }
    private void appFunction(){
        picSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSetting[0]==1){
                    setDestece.setVisibility(setDestece.VISIBLE);
                    FData.setVisibility(FData.VISIBLE);
                    fText.setVisibility(fText.VISIBLE);
                    del.setVisibility(del.VISIBLE);
                    picSetting.setBackgroundResource(R.mipmap.save);
                    isSetting[0]=0;
                }
                else{
                    picSetting.setBackgroundResource(R.mipmap.undo);
                    isSetting[0]=1;
                    setDestece.setVisibility(setDestece.GONE);
                    FData.setVisibility(FData.GONE);
                    fText.setVisibility(fText.GONE);
                    del.setVisibility(del.GONE);
                }
            }
        });
        del.setOnClickListener(new View.OnClickListener() {//删除照片的监听器
            @Override
            public void onClick(View v) {
                Log.d(TAG,"del");
            }
        });
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPic=new Intent(PicActivity.this,MainActivity.class);
                startActivity(intentPic);
            }
        });
        FData.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress>=970)
                    FData.setProgress(1100);
                if (progress>=660&&progress<970)
                    FData.setProgress(800);
                if (progress>=490&&progress<660)
                    FData.setProgress(560);
                if (progress>=370&&progress<490)
                    FData.setProgress(400);
                if (progress>=240&&progress<370)
                    FData.setProgress(280);
                if (progress>=135&&progress<240)
                    FData.setProgress(180);
                if (progress<135)
                    FData.setProgress(95);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fText.setText("f"+String.valueOf((double)FData.getProgress()/100));}
        });
    }
}
