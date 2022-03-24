package media.share.audioplayer;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import media.share.audioplayer.bean.MuteEnum;
import media.share.audioplayer.bean.PlayTime;
import media.share.audioplayer.code.PlayState;
import media.share.audioplayer.listener.OnPlayerErrorListener;
import media.share.audioplayer.listener.OnPlayerInfoListener;
import media.share.audioplayer.listener.OnPlayerProgressListener;
import media.share.audioplayer.listener.OnPlayerStateListener;
import media.share.audioplayer.listener.OnPlayerVolumeDBListener;
import media.share.audioplayer.utils.PermissionsUtils;

/**
 * @author mac
 * 音频功能演示
 */
public class DemoActivity extends Activity {

    private AudioPlayer audioPlayer;
    private int position = 0;
    private boolean isSeekBar = false;
    private Button start;
    private Button pause;
    private Button resume;
    private Button stop;
    private SeekBar seek;
    private EditText volume;
    private Button setVolume;
    private Button left;
    private Button right;
    private Button center;
    private EditText inputPitch;
    private Button setPitch;
    private EditText inputSpeed;
    private Button setSpeed;
    private TextView progressTime;
    private TextView volumeDb;
    private TextView soundInfo;
    private EditText musicUrl;
    private EditText nextUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initView();
        initData();
        initListener();

    }

    private void initView() {
        start = findViewById(R.id.start);
        pause = findViewById(R.id.pause);
        resume = findViewById(R.id.resume);
        stop = findViewById(R.id.stop);
        seek = findViewById(R.id.seek);
        volume = findViewById(R.id.volume);
        setVolume = findViewById(R.id.set_volume);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        center = findViewById(R.id.center);
        inputPitch = findViewById(R.id.input_pitch);
        setPitch = findViewById(R.id.set_pitch);
        inputSpeed = findViewById(R.id.input_speed);
        setSpeed = findViewById(R.id.set_speed);
        progressTime = findViewById(R.id.progress_time);
        volumeDb = findViewById(R.id.volume_db);
        soundInfo = findViewById(R.id.sound_info);
        musicUrl = findViewById(R.id.music_url);
        nextUrl = findViewById(R.id.next_url);
    }

    private void initData() {
        audioPlayer = new AudioPlayer();
        audioPlayer.setSource(musicUrl.getText().toString());
        audioPlayer.setNextSource(nextUrl.getText().toString());
    }

    private void initListener() {
        audioPlayer.setPlayerStateListener(new OnPlayerStateListener() {
            @Override
            public void play(PlayState state) {
                if (state == PlayState.ON_STOP) {
                    //设置下一首播放路径
                    if (TextUtils.isEmpty(nextUrl.getText().toString())) {
                        Toast.makeText(DemoActivity.this, "请先输入下一首音频播放rul", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    audioPlayer.setNextSource(nextUrl.getText().toString());
                }
                Log.d("_playState", state.toString());
            }
        });
        audioPlayer.setPlayerProgressListener(new OnPlayerProgressListener() {
            @Override
            public void progress(PlayTime playTime) {
                seek.setProgress(playTime.currentTime * 100 / playTime.totalTime);
                progressTime.setText(playTime.toString());
            }
        });
        audioPlayer.setPlayerErrorListener(new OnPlayerErrorListener() {
            @Override
            public void onError(int code, String message) {

            }
        });
        audioPlayer.setOnPlayerVolumeDBListener(new OnPlayerVolumeDBListener() {
            @Override
            public void onDbValue(int db) {
                volumeDb.setText("振幅:" + db);
            }
        });
        audioPlayer.setOnPlayerInfoListener(new OnPlayerInfoListener() {
            @Override
            public void soundInfo(HashMap<String, String> map) {
                String txt = "";
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    txt += entry.getValue() + "\n";
                }
                soundInfo.setText(txt);
            }
        });
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (audioPlayer.getDuration() > 0 && isSeekBar) {
                    position = audioPlayer.getDuration() * progress / 100;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioPlayer.seek(position);
                isSeekBar = false;
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionsUtils.getStorgePermission(DemoActivity.this)){
                    if (TextUtils.isEmpty(musicUrl.getText().toString())) {
                        Toast.makeText(DemoActivity.this, "请先输入音频播放rul", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    audioPlayer.setSource(musicUrl.getText().toString());
                    audioPlayer.start();
                }
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.pause();
            }
        });
        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.resume();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.stop();
            }
        });
        seek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.seek(210);
            }
        });
        setVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.setVolume(Integer.parseInt(volume.getText().toString()));
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.setMute(MuteEnum.MUTE_LEFT);
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.setMute(MuteEnum.MUTE_RIGHT);
            }
        });
        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.setMute(MuteEnum.MUTE_CENTER);
            }
        });
        setPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputPitch.getText().toString())) {
                    Toast.makeText(DemoActivity.this, "请先输入音调", Toast.LENGTH_SHORT).show();
                    return;
                }
                audioPlayer.setPitch(Float.parseFloat(inputPitch.getText().toString()));
            }
        });
        setSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputSpeed.getText().toString())) {
                    Toast.makeText(DemoActivity.this, "请先输入音速", Toast.LENGTH_SHORT).show();
                    return;
                }
                audioPlayer.setSpeed(Float.parseFloat(inputSpeed.getText().toString()));
            }
        });
    }


}