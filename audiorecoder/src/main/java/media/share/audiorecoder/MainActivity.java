package media.share.audiorecoder;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import media.share.baselibrary.PermissionsUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 是否开始录制，true：开始录制；false：未开始录制
     **/
    private boolean isStart;

    /**
     * 开始或停止录制音频按钮
     **/
    private TextView audioSet;

    /**
     * 音频录制控制器
     **/
    private AudioRecorder audioRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initView() {
        audioSet = findViewById(R.id.audio_set);
    }

    private void initEvent() {
        audioSet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_set:
                //开始录制或停止录制
                if (PermissionsUtils.getStorgePermission(MainActivity.this)) {
                    checkInit();
                    if (isStart) {
                        audioSet.setText("停止录制");
                        isStart = false;
                        audioRecorder.startRecord();
                    } else {
                        audioSet.setText("开始录制");
                        isStart = true;
                        audioRecorder.stopRecord();
                    }
                }
                break;
            default:
        }
    }

    /**
     * 音频控制器初始化
     * **/
    private void checkInit() {
        if (PermissionsUtils.getRecordPermission(MainActivity.this)) {
            if (audioRecorder == null) {
                audioRecorder = new AudioRecorder(MainActivity.this);
                audioRecorder.initAudioRecord();
            }
        }
    }
}