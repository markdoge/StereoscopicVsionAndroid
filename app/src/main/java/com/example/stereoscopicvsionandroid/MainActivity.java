package com.example.stereoscopicvsionandroid;

import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity implements OnClickAction {
    private Camera camera;
    private boolean isOpen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //SurfaceView surfaceView=findViewById(R.id.camView);
        //SurfaceHolder surfaceHolder=surfaceView.getHolder();
        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //camera=Camera.open();
        ImageButton document=findViewById(R.id.file);
        ImageButton btncam = findViewById(R.id.cam);
        TestScroller text=findViewById(R.id.selecteText);
        final int[] ischange = {1};
        btncam.setBackgroundResource(R.mipmap.init);
        btncam.setOnClickListener(new View.OnClickListener() {
            private static final String TAG = "TAG";
            @Override
            public void onClick(View view) {
                if (ischange[0] ==1){
                    btncam.setBackgroundResource(R.mipmap.shoot);
                    ischange[0] =0;
                    Log.d(TAG, "onClick: ");
                    Log.d(TAG,text.getSelectedString());
                }
                else{
                    btncam.setBackgroundResource(R.mipmap.init);
                    ischange[0]=1;
                    Log.d(TAG,"onCLick:2");
                }
            }
        });
    }
}