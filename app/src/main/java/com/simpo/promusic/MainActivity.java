package com.simpo.promusic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simpo.promusic.music.bean.MuteEnum;
import com.simpo.promusic.music.bean.WlTimeInfoBean;
import com.simpo.promusic.music.listener.WlOnCompleteListener;
import com.simpo.promusic.music.listener.WlOnErrorListener;
import com.simpo.promusic.music.listener.WlOnLoadListener;
import com.simpo.promusic.music.listener.WlOnPreparedListener;
import com.simpo.promusic.music.listener.WlOnPauseResumeListener;
import com.simpo.promusic.music.listener.WlOnTimeInfoListener;
import com.simpo.promusic.music.listener.WlOnValumeDBListener;
import com.simpo.promusic.music.log.MyLog;
import com.simpo.promusic.music.player.MusicPlayer;
import com.simpo.promusic.utils.WlTimeUtil;


/**
 * @author simpo
 */
public class MainActivity extends AppCompatActivity {

    private MusicPlayer wlPlayer;
    private TextView tvTime;
    private TextView tvVolume;
    private TextView volumeShow;
    private SeekBar seekBarSeek;
    private SeekBar seekBarVolume;
    private int position = 0;
    private boolean isSeekBar = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTime = findViewById(R.id.tv_time);
        tvVolume = findViewById(R.id.tv_volume);
        seekBarSeek = findViewById(R.id.seekbar_seek);
        seekBarVolume = findViewById(R.id.seekbar_volume);
        volumeShow = findViewById(R.id.volume_show);
        wlPlayer = new MusicPlayer();
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
                if (wlPlayer.getDuration() > 0 && isSeekBar) {
                    position = wlPlayer.getDuration() * progress / 100;
                }
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
        wlPlayer.setSource("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
        wlPlayer.prepare();
    }

    public void pause(View view) {

        wlPlayer.pause();

    }

    public void resume(View view) {
        wlPlayer.resume();
    }

    public void stop(View view) {
        wlPlayer.stop();
    }

    public void seek(View view) {
        wlPlayer.seek(200);
    }

    public void next(View view) {
        wlPlayer.playNext("http://ngcdn004.cnr.cn/live/dszs/index.m3u8");
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
        wlPlayer.setSpeed(1.5f);
        wlPlayer.setPitch(1.0f);
    }

    public void pitch(View view) {
        wlPlayer.setPitch(1.5f);
        wlPlayer.setSpeed(1.0f);
    }

    public void speedpitch(View view) {
        wlPlayer.setSpeed(1.5f);
        wlPlayer.setPitch(1.5f);
    }

    public void normalspeedpitch(View view) {
        wlPlayer.setSpeed(1.0f);
        wlPlayer.setPitch(1.0f);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (!isSeekBar) {
                    WlTimeInfoBean wlTimeInfoBean = (WlTimeInfoBean) msg.obj;
                    tvTime.setText(WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getTotalTime(), wlTimeInfoBean.getTotalTime())
                            + "/" + WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getCurrentTime(), wlTimeInfoBean.getTotalTime()));
                    seekBarSeek.setProgress(wlTimeInfoBean.getCurrentTime() * 100 / wlTimeInfoBean.getTotalTime());
                }
            }
        }
    };
}
