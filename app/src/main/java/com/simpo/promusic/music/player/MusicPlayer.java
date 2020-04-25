package com.simpo.promusic.music.player;

import android.text.TextUtils;

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
import com.simpo.promusic.music.threadXUtil.AbstractLife;
import com.simpo.promusic.music.threadXUtil.ThreadX;

/**
 * @author wanxuedong
 * 使用ffmpeg+openSlES实现音频的播放，暂停，再播，停止，倍速，展示当前播放和总时间长
 **/
public class MusicPlayer {

    /**
     * 加载FFmpeg音视频处理库
     * **/
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
    }

    /**
     * 数据源
     **/
    private String source;
    private WlTimeInfoBean wlTimeInfoBean;
    private boolean playNext = false;
    private static int duration = -1;
    /**
     * 音量大小，0-100
     **/
    private static int volumePercent = 100;
    /**
     * 声道模式，左，右，立体声道，默认立体
     **/
    private static MuteEnum muteEnum = MuteEnum.MUTE_CENTER;
    /**
     * 播放速度，正常为1
     **/
    private static float speed = 1.0f;
    /**
     * 播放音调，正常为1
     **/
    private static float pitch = 1.0f;

    private WlOnPreparedListener wlOnPreparedListener;
    private WlOnLoadListener wlOnLoadListener;
    private WlOnPauseResumeListener wlOnPauseResumeListener;
    private WlOnTimeInfoListener wlOnTimeInfoListener;
    private WlOnCompleteListener wlOnCompleteListener;
    private WlOnErrorListener wlOnErrorListener;
    private WlOnValumeDBListener wlOnValumeDBListener;

    /**
     * 设置音频数据源
     **/
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 设置准备状态监听
     **/
    public void setWlOnPreparedListener(WlOnPreparedListener wlOnPreparedListener) {
        this.wlOnPreparedListener = wlOnPreparedListener;
    }

    public void setWlOnLoadListener(WlOnLoadListener wlOnLoadListener) {
        this.wlOnLoadListener = wlOnLoadListener;
    }

    public void setWlOnPauseResumeListener(WlOnPauseResumeListener wlOnPauseResumeListener) {
        this.wlOnPauseResumeListener = wlOnPauseResumeListener;
    }

    public void setWlOnTimeInfoListener(WlOnTimeInfoListener wlOnTimeInfoListener) {
        this.wlOnTimeInfoListener = wlOnTimeInfoListener;
    }

    public void setWlOnCompleteListener(WlOnCompleteListener wlOnCompleteListener) {
        this.wlOnCompleteListener = wlOnCompleteListener;
    }

    public void setWlOnErrorListener(WlOnErrorListener wlOnErrorListener) {
        this.wlOnErrorListener = wlOnErrorListener;
    }

    public void setWlOnValumeDBListener(WlOnValumeDBListener wlOnValumeDBListener) {
        this.wlOnValumeDBListener = wlOnValumeDBListener;
    }

    /**
     * 等待C++层回调即可，表示数据准备完成
     **/
    public void onCallPrepared() {
        if (wlOnPreparedListener != null) {
            wlOnPreparedListener.onPrepared();
        }
    }

    /**
     * 数据加载中回调
     **/
    public void onCallLoad(boolean load) {
        if (wlOnLoadListener != null) {
            wlOnLoadListener.onLoad(load);
        }
    }

    /**
     * 当前播放时长和总时长回调
     **/
    public void onCallTimeInfo(int currentTime, int totalTime) {
        if (wlOnTimeInfoListener != null) {
            if (wlTimeInfoBean == null) {
                wlTimeInfoBean = new WlTimeInfoBean();
            }
            wlTimeInfoBean.setCurrentTime(currentTime);
            wlTimeInfoBean.setTotalTime(totalTime);
            wlOnTimeInfoListener.onTimeInfo(wlTimeInfoBean);
        }
    }

    /**
     * 音频播放完毕回调
     **/
    public void onCallComplete() {
        stop();
        if (wlOnCompleteListener != null) {
            wlOnCompleteListener.onComplete();
        }
    }

    /**
     * 开始播放下一首的回调
     **/
    public void onCallNext() {
        if (playNext) {
            playNext = false;
            prepare();
        }
    }

    /**
     * 错误信息回调
     **/
    public void onCallError(int code, String msg) {
        if (wlOnErrorListener != null) {
            stop();
            wlOnErrorListener.onError(code, msg);
        }
    }

    /**
     * 声音大小回调
     **/
    public void onCallValumeDB(int db) {
        if (wlOnValumeDBListener != null) {
            wlOnValumeDBListener.onDbValue(db);
        }
    }

    /**
     * 播放器进入准备状态
     **/
    public void prepare() {
        if (TextUtils.isEmpty(source)) {
            MyLog.d("source is empty");
            return;
        }
        onCallLoad(true);
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_prepared(source);
            }
        });
    }

    /**
     * 开始播放音频源
     **/
    public void start() {
        if (TextUtils.isEmpty(source)) {
            MyLog.d("source is empty");
            return;
        }
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_start();
            }
        });
    }

    /**
     * 暂停音频源
     **/
    public void pause() {
        n_pause();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(true);
        }
    }

    /**
     * 再次播放音频源
     **/
    public void resume() {
        n_resume();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(false);
        }
    }

    /**
     * 停止播放音频源并释放内存
     **/
    public void stop() {
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_stop();
            }
        });
    }

    /**
     * 跳转到指定播放位置,单位秒
     **/
    public void seek(final int secds) {
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_seek(secds);
            }
        });
    }

    /**
     * 设置播放下一首，并且传入下一首的url
     **/
    public void playNext(String url) {
        source = url;
        playNext = true;
        stop();
    }

    /**
     * 获取当前的播放时间，单位秒
     **/
    public int getDuration() {
        if (duration < 0) {
            duration = n_duration();
        }
        return duration;
    }

    /**
     * 设置音量大小，0-100
     **/
    public void setVolume(int percent) {
        if (percent >= 0 && percent <= 100) {
            volumePercent = percent;
            n_volume(percent);
        }
    }

    /**
     * 获取当前的音量
     **/
    public int getVolumePercent() {
        return volumePercent;
    }

    /**
     * 设置声道模式
     **/
    public void setMute(MuteEnum mute) {
        muteEnum = mute;
        n_mute(mute.getValue());
    }

    /**
     * 设置声调,1为正常
     **/
    public void setPitch(float p) {
        pitch = p;
        n_pitch(pitch);
    }

    /**
     * 设置播放速度，1为征程
     **/
    public void setSpeed(float s) {
        speed = s;
        n_speed(speed);
    }


    private native void n_prepared(String source);

    private native void n_start();

    private native void n_pause();

    private native void n_resume();

    private native void n_stop();

    private native void n_seek(int secds);

    private native int n_duration();

    private native void n_volume(int percent);

    private native void n_mute(int mute);

    private native void n_pitch(float pitch);

    private native void n_speed(float speed);

}
