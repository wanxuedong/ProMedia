package com.simple.livepusher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.simple.livepusher.utils.PermissionsUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public native String stringFromJNI();

    public void cameraPreview(View view) {
        if (PermissionsUtils.getCameraPermission(this)) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
    }

    public void vodeorecord(View view) {
        if (PermissionsUtils.getStorgePermission(this)) {
            if (PermissionsUtils.getCameraPermission(this)) {
                Intent intent = new Intent(this, VideoActivity.class);
                startActivity(intent);
            }
        }
    }

}
