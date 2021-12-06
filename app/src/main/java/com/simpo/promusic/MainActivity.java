package com.simpo.promusic;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simpo.promusic.opengl.WlGLSurfaceView;
import com.simpo.promusic.player.MusicPlayer;
import com.simpo.promusic.player.bean.MuteEnum;
import com.simpo.promusic.player.bean.WlTimeInfoBean;
import com.simpo.promusic.player.listener.WlOnCompleteListener;
import com.simpo.promusic.player.listener.WlOnErrorListener;
import com.simpo.promusic.player.listener.WlOnLoadListener;
import com.simpo.promusic.player.listener.WlOnPauseResumeListener;
import com.simpo.promusic.player.listener.WlOnPreparedListener;
import com.simpo.promusic.player.listener.WlOnTimeInfoListener;
import com.simpo.promusic.player.listener.WlOnValumeDBListener;
import com.simpo.promusic.player.log.MyLog;
import com.simpo.promusic.utils.PermissionsUtils;
import com.simpo.promusic.utils.WlTimeUtil;


/**
 * @author simpo
 */
public class MainActivity extends AppCompatActivity {

    private MusicPlayer wlPlayer;
    private TextView tvTime;
    private TextView tvVolume;
    private TextView volumeShow;
    private EditText musicUrl;
    private EditText musicSpeed;
    private EditText musicTone;
    private SeekBar seekBarSeek;
    private SeekBar seekBarVolume;
    private int position = 0;
    private boolean isSeekBar = false;
    private WlGLSurfaceView wlGLSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        tvTime = findViewById(R.id.tv_time);
        tvVolume = findViewById(R.id.tv_volume);
        musicUrl = findViewById(R.id.music_url);
        musicSpeed = findViewById(R.id.music_speed);
        musicTone = findViewById(R.id.music_tone);
        seekBarSeek = findViewById(R.id.seekbar_seek);
        seekBarVolume = findViewById(R.id.seekbar_volume);
        volumeShow = findViewById(R.id.volume_show);
        wlGLSurfaceView = findViewById(R.id.wlglsurfaceview);
        wlPlayer = new MusicPlayer();
        wlPlayer.setWlGLSurfaceView(wlGLSurfaceView);
        wlPlayer.setVolume(50);
        tvVolume.setText("音量：" + wlPlayer.getVolumePercent() + "%");
        seekBarVolume.setProgress(wlPlayer.getVolumePercent());
        wlPlayer.setWlOnPreparedListener(new WlOnPreparedListener() {
            @Override
            public void onPrepared() {
                MyLog.d("准备好了，可以开始播放声音了");
                wlPlayer.start();
            }
        });
        wlPlayer.setWlOnLoadListener(new WlOnLoadListener() {
            @Override
            public void onLoad(boolean load) {
                if (load) {
                    MyLog.d("加载中...");
                } else {
                    MyLog.d("播放中...");
                }
            }
        });

        wlPlayer.setWlOnPauseResumeListener(new WlOnPauseResumeListener() {
            @Override
            public void onPause(boolean pause) {
                if (pause) {
                    MyLog.d("暂停中...");
                } else {
                    MyLog.d("播放中...");
                }
            }
        });

        wlPlayer.setWlOnTimeInfoListener(new WlOnTimeInfoListener() {
            @Override
            public void onTimeInfo(WlTimeInfoBean timeInfoBean) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = timeInfoBean;
                handler.sendMessage(message);
            }
        });

        wlPlayer.setWlOnErrorListener(new WlOnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                MyLog.d("code:" + code + ", msg:" + msg);
            }
        });
        wlPlayer.setWlOnCompleteListener(new WlOnCompleteListener() {
            @Override
            public void onComplete() {
                MyLog.d("播放完成了");
                isPlay = false;
            }
        });

        wlPlayer.setWlOnValumeDBListener(new WlOnValumeDBListener() {
            @Override
            public void onDbValue(int db) {
                volumeShow.setText("音量大小:" + db);
            }
        });
        seekBarSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position = wlPlayer.getDuration() * progress / 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                wlPlayer.seek(position);
                isSeekBar = false;
            }
        });

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wlPlayer.setVolume(progress);
                tvVolume.setText("音量：" + wlPlayer.getVolumePercent() + "%");
                MyLog.d("progress is " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void begin(View view) {
//        wlPlayer.updateFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AFeel/杞人忧天_标清.flv", "rtmp://192.168.3.98:1935/rtmplive/test");
        if (PermissionsUtils.getStorgePermission(this)) {
            isPlay = true;
//            if (musicUrl.getText() != null && !TextUtils.isEmpty(musicUrl.getText().toString())) {
//                wlPlayer.setSource(musicUrl.getText().toString());
//            } else {
//                wlPlayer.setSource("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
//                wlPlayer.playNext("http://vfx.mtime.cn/Video/2019/03/19/mp4/190319222227698228.mp4");
//                wlPlayer.setSource("https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4");
//                wlPlayer.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AFeel/泰坦尼克号.mkv");
                wlPlayer.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AFeel/WDNMD.mp4");
//            }
            wlPlayer.prepare();
        }
    }

    private boolean hasPause = false;
    private boolean isPlay = false;

    @Override
    protected void onPause() {
        super.onPause();
        hasPause = true;
        wlPlayer.pause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPause && isPlay) {
            wlPlayer.resume();
        }
    }

    public void pause(View view) {
        wlPlayer.pause();
        isPlay = false;

    }

    public void resume(View view) {
        isPlay = true;
        wlPlayer.resume();
    }

    public void stop(View view) {
        wlPlayer.stop();
    }

    public void seek(View view) {
        wlPlayer.seek(200);
    }

    public void next(View view) {
//        wlPlayer.playNext("http://ngcdn004.cnr.cn/live/dszs/index.m3u8");
        wlPlayer.playNext("http://vfx.mtime.cn/Video/2019/03/19/mp4/190319222227698228.mp4");
    }

    public void left(View view) {
        wlPlayer.setMute(MuteEnum.MUTE_LEFT);
    }

    public void right(View view) {
        wlPlayer.setMute(MuteEnum.MUTE_RIGHT);
    }

    public void center(View view) {
        wlPlayer.setMute(MuteEnum.MUTE_CENTER);
    }

    public void speed(View view) {
        if (musicSpeed.getText() != null && !"".equals(musicSpeed.getText().toString())) {
            wlPlayer.setSpeed(Float.parseFloat(musicSpeed.getText().toString()) / 5);
        } else {
            wlPlayer.setSpeed(1.0f);
        }
    }

    public void pitch(View view) {
        if (musicSpeed.getText() != null && !"".equals(musicSpeed.getText().toString())) {
            wlPlayer.setPitch(Float.parseFloat(musicTone.getText().toString()) / 5);
        } else {
            wlPlayer.setPitch(1.0f);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                WlTimeInfoBean wlTimeInfoBean = (WlTimeInfoBean) msg.obj;
                tvTime.setText(WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getTotalTime(), wlTimeInfoBean.getTotalTime())
                        + "/" + WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getCurrentTime(), wlTimeInfoBean.getTotalTime()));


                if (!isSeekBar && wlTimeInfoBean.getTotalTime() > 0) {
                    seekBarSeek.setProgress(wlTimeInfoBean.getCurrentTime() * 100 / wlTimeInfoBean.getTotalTime());
                }
            }
        }
    };
}
