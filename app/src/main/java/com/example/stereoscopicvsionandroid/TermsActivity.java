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
    private Boolean isClick=false;
    private final String LANGUAGE_CN = "zh-CN";
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
            Log.d("TAG","user click the stdpic");
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.stdmap);
            save.saveLensFormatPic(TermsActivity.this,bitmap);
        }
    }

}
