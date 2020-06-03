package com.simple.livepusher;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.simple.livepusher.camera.WlCameraView;

public class CameraActivity extends AppCompatActivity {

    private WlCameraView wlCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        wlCameraView = findViewById(R.id.cameraview);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        wlCameraView.previewAngle(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wlCameraView.onDestroy();
    }
}
