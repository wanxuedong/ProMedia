package media.share.audiorecoder;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * @author mac
 */
public class MainActivity extends Activity implements View.OnClickListener {

    /**
     * 是否开始录制，true：开始录制；false：未开始录制
     **/
    private boolean isStart;

    /**
     * 通道设置
     **/
    private RadioGroup channelSet;

    /**
     * 采样率设置
     **/
    private RadioGroup sampleRateSet;

    /**
     * 采样位数设置
     **/
    private RadioGroup sampleBitSet;

    /**
     * 录制时长
     **/
    private TextView recordTime;

    /**
     * 开始或停止录制音频按钮
     **/
    private TextView audioSet;

    /**
     * 音频录制控制器
     **/
    private AudioRecorder audioRecorder;

    /**
     * 音频源：音频输入-麦克风
     **/
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;

    /**
     * 音频通道
     **/
    private static int audioChannel = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 采样率
     * 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     * 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
     **/
    private static int audioSampleRate = 16000;

    /**
     * 音频格式：PCM编码
     **/
    private static int audioEncoding = AudioFormat.ENCODING_PCM_8BIT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initView() {
        channelSet = findViewById(R.id.channel_set);
        sampleRateSet = findViewById(R.id.sample_rate_set);
        sampleBitSet = findViewById(R.id.sample_bit_set);
        recordTime = findViewById(R.id.record_time);
        audioSet = findViewById(R.id.audio_set);
    }

    private void initEvent() {
        audioSet.setOnClickListener(this);
        channelSet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.channel_one:
                        audioChannel = AudioFormat.CHANNEL_IN_MONO;
                        break;
                    case R.id.channel_two:
                        audioChannel = AudioFormat.CHANNEL_IN_STEREO;
                        break;
                    default:
                }
                resetAudioRecorder();
            }
        });
        sampleRateSet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rate_one:
                        audioSampleRate = 16000;
                        break;
                    case R.id.rate_two:
                        audioSampleRate = 22050;
                        break;
                    case R.id.rate_three:
                        audioSampleRate = 44100;
                        break;
                    case R.id.rate_four:
                        audioSampleRate = 48000;
                        break;
                    default:
                }
                resetAudioRecorder();
            }
        });
        sampleBitSet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.bit_one:
                        audioEncoding = AudioFormat.ENCODING_PCM_8BIT;
                        break;
                    case R.id.bit_two:
                        audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
                        break;
                    default:
                }
                resetAudioRecorder();
            }
        });
    }

    /**
     * 更新录音器录制参数
     **/
    private void resetAudioRecorder() {
        if (!isStart) {
            audioRecorder = new AudioRecorder();
            audioRecorder.initAudioRecord(AUDIO_INPUT, audioChannel, audioSampleRate, audioEncoding);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_set:
                //开始录制或停止录制
                if (PermissionsUtils.getStorgePermission(MainActivity.this)) {
                    if (PermissionsUtils.getRecordPermission(MainActivity.this)) {
                        if (audioRecorder == null) {
                            audioRecorder = new AudioRecorder();
                            audioRecorder.initAudioRecord(AUDIO_INPUT, audioChannel, audioSampleRate, audioEncoding);
                        }
                        if (isStart) {
                            audioSet.setText("开始录制");
                            isStart = false;
                            audioRecorder.stopRecord();
                        } else {
                            audioSet.setText("停止录制");
                            isStart = true;
                            audioRecorder.startRecord();
                        }
                        updateTime();
                    }
                }
                break;
            default:
        }
    }

    /**
     * 音频录制时长
     **/
    private int time = 0;

    /**
     * 更新录制时长
     **/
    private void updateTime() {
        if (isStart) {
            time = 0;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (isStart) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        time++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isStart) {
                                    recordTime.setText("录制时长: " + time + "s");
                                } else {
                                    recordTime.setText("录制时长: 0s");
                                }
                            }
                        });
                    }
                }
            }.start();
        } else {
            time = 0;
        }
    }

}