package com.simple.livepusher;

import android.app.Activity;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.simple.livepusher.camera.WlCameraView;
import com.simple.livepusher.encodec.WlBaseMediaEncoder;
import com.simple.livepusher.encodec.WlMediaEncodec;


public class VideoActivity extends Activity {


    private WlCameraView wlCameraView;
    private Button btnRecord;

    private WlMediaEncodec wlMediaEncodec;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        wlCameraView = findViewById(R.id.cameraview);
        btnRecord = findViewById(R.id.btn_record);
    }

    public void record(View view) {

        if (wlMediaEncodec == null) {
            Log.d("com_simple_livepusher", "textureid is " + wlCameraView.getTextureId());
            wlMediaEncodec = new WlMediaEncodec(this, wlCameraView.getTextureId());
            wlMediaEncodec.initEncodec(wlCameraView.getEglContext(),
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4", MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);
            wlMediaEncodec.setOnMediaInfoListener(new WlBaseMediaEncoder.OnMediaInfoListener() {
                @Override
                public void onMediaTime(int times) {
                    Log.d("com_simple_livepusher", "time is : " + times);
                }
            });

            wlMediaEncodec.startRecord();
            btnRecord.setText("正在录制");
        } else {
            wlMediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            wlMediaEncodec = null;
        }
    }

}
