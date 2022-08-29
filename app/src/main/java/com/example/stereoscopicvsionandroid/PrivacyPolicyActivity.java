package com.example.stereoscopicvsionandroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import privacyPolicyTool.AppUtil;

public class PrivacyPolicyActivity extends Activity implements View.OnClickListener {

    private static final String TAG = PrivacyPolicyActivity.class.getSimpleName();
    private ImageButton imageView_my;
    private ImageButton imageView;
    private final String LANGUAGE_CN = "zh-CN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_policy);

        initView();
    }

    private void initView() {
        imageView_my = findViewById(R.id.myico);
        imageView = findViewById(R.id.mobtechico);
        imageView_my.setOnClickListener(this);
        imageView.setOnClickListener(this);
        String language = AppUtil.getLanguage(PrivacyPolicyActivity.this);
        Log.d(TAG, "当前语言：" + language);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageView.destroyDrawingCache();
        imageView_my.destroyDrawingCache();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==imageView.getId()){
            Uri uri = Uri.parse("https://www.mob.com/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        if (id==imageView_my.getId()){
            Uri uri = Uri.parse("https://github.com/markdoge/StereoscopicVsionAndroid.git");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
