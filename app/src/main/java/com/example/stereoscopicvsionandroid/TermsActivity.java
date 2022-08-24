package com.example.stereoscopicvsionandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import privacyPolicyTool.*;


public class TermsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TAG";
    private Button btn;
    private final String LANGUAGE_CN = "zh-CN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        initView();
    }

    private void initView() {

        btn = findViewById(R.id.lenFormat);
        btn.setOnClickListener(this);
        String language = AppUtil.getLanguage(TermsActivity.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == btn.getId()){
            Intent intent = new Intent(TermsActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
