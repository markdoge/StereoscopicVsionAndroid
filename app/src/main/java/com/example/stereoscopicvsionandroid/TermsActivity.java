package com.example.stereoscopicvsionandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import privacyPolicyTool.*;
import photoFun.BitmapSaver;


public class TermsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TAG";
    private ImageButton saveLenFormatPic;
    private ImageButton saveLenFormatPic2;
    private Boolean isClick=false;
    private BitmapSaver save = new BitmapSaver(TAG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        initView();
    }

    private void initView() {
        saveLenFormatPic = findViewById(R.id.lenstadmap);
        saveLenFormatPic.setOnClickListener(this);
        saveLenFormatPic2 = findViewById(R.id.formatpic2);
        saveLenFormatPic2.setOnClickListener(this);
        String language = AppUtil.getLanguage(TermsActivity.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==saveLenFormatPic.getId()){
            //save photo here
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.stdmap);
            save.saveLensFormatPic(TermsActivity.this,bitmap);
        }
        if(id==saveLenFormatPic2.getId()){
            //save photo here
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.acircles_pattern);
            save.saveLensFormatPic(TermsActivity.this,bitmap);
        }
    }

}
