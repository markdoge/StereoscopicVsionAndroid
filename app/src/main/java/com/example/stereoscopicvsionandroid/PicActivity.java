package com.example.stereoscopicvsionandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import albumFun.PhotoLoader;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PicActivity extends AppCompatActivity implements OnClickAction {
    private ImageButton out;
    private ImageButton picSetting;
    private ImageButton del;
    private ImageButton lastPic;
    private ImageButton nextPic;
    private ImageView imageView;
    private int photoNum;
    private int currentNum=0;
    final int[] isSetting={1};
    private ArrayList<Bitmap> resources;
    private ArrayList<String> fileList;
    private TextView fText;
    private static final String TAG = "PicTAG";
    private SeekBar FData;
    private SeekBar setDestece;
    private PhotoLoader photoLoader;
    private Intent intent;
    private static final String picPath=Environment.getExternalStorageDirectory().getPath()+File.separator+
            Environment.DIRECTORY_PICTURES
            + File.separator +"stereo/pic";
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
        nextPic=findViewById(R.id.picTurnRight);
        lastPic=findViewById(R.id.picTurnLeft);
        imageView=findViewById(R.id.picView);
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
        try {
            Log.d(TAG,"path is "+picPath);
            photoLoader=new PhotoLoader(picPath);
            resources =photoLoader.getBitmap();
            fileList=photoLoader.getPicLocation();
            photoNum= resources.size();
            imageView.setImageBitmap(resources.get(0));
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }
    }
    private void appFunction(){
        picSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSetting[0]==1){
                    setDestece.setVisibility(setDestece.VISIBLE);
                    FData.setVisibility(FData.VISIBLE);
                    fText.setVisibility(fText.VISIBLE);
                    del.setVisibility(del.GONE);
                    lastPic.setVisibility(lastPic.GONE);
                    nextPic.setVisibility(nextPic.GONE);
                    picSetting.setBackgroundResource(R.mipmap.save);
                    isSetting[0]=0;
                }
                else{
                    picSetting.setBackgroundResource(R.mipmap.undo);
                    isSetting[0]=1;
                    setDestece.setVisibility(setDestece.GONE);
                    FData.setVisibility(FData.GONE);
                    fText.setVisibility(fText.GONE);
                    del.setVisibility(del.VISIBLE);
                    lastPic.setVisibility(lastPic.VISIBLE);
                    nextPic.setVisibility(nextPic.VISIBLE);
                }
            }
        });
        del.setOnClickListener(new View.OnClickListener() {//删除照片的监听器
            @Override
            public void onClick(View v) {
                Log.d(TAG,"del "+fileList.get(currentNum));
                new File(fileList.get(currentNum)).delete();
                photoNum--;
                fileList.remove(currentNum);
                resources.remove(currentNum);
                if(currentNum>0) {
                    currentNum--;
                    imageView.setImageBitmap(resources.get(currentNum));
                }
                else{
                    if(photoNum>0) {
                        imageView.setImageBitmap(resources.get(currentNum));
                    }
                    else finish();
                }

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
        nextPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentNum<photoNum){
                    imageView.setImageBitmap(resources.get(currentNum+1));
                    currentNum+=1;
                }
                else {
                    Toast toast=Toast.makeText(PicActivity.this, "已经是最后一张照片", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 1920);
                    toast.show();
                }
            }
        });
        lastPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentNum!=0){
                    imageView.setImageBitmap(resources.get(currentNum-1));
                    currentNum-=1;
                }
                else {
                    Toast toast=Toast.makeText(PicActivity.this, "已经是第一张照片", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 1920);
                    toast.show();
                }
            }
        });
    }
}
