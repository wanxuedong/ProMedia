package media.share.audioplayer;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.HashMap;

import media.share.audioplayer.bean.MuteEnum;
import media.share.audioplayer.bean.PlayTime;
import media.share.audioplayer.code.PlayState;
import media.share.audioplayer.listener.OnPlayerErrorListener;
import media.share.audioplayer.listener.OnPlayerInfoListener;
import media.share.audioplayer.listener.OnPlayerProgressListener;
import media.share.audioplayer.listener.OnPlayerStateListener;
import media.share.audioplayer.listener.OnPlayerVolumeDBListener;

/**
 * @author wan
 * 创建日期：2021/12/10
 * 描述：音频处理器
 */
public class AudioEngine {

    static {
        System.loadLibrary("audio-lib");
    }

    /**
     * 播放状态回调
     **/
    private OnPlayerStateListener onPlayerStateListener;
    /**
     * 播放错误回调
     **/
    private OnPlayerErrorListener onPlayerErrorListener;
    /**
     * 播放进度回调
     **/
    private OnPlayerProgressListener onPlayerProgressListener;
    /**
     * 播放声音振幅回调
     **/
    private OnPlayerVolumeDBListener onPlayerVolumeDBListener;
    /**
     * 播放头部信息回调
     **/
    private OnPlayerInfoListener onPlayerInfoListener;

    /**
     * 当前播放音量，范围0-100
     **/
    private int volumePercent = 100;

    /**
     * 音频总时长
     **/
    private int duration = -1;

    /**
     * 停止模式，true，主动停止，false，正常播放停止
     * 用于防止主动调用stop后被onComplete再次调用stop，防重复调用
     **/
    private boolean activeStop = false;

    /**
     * 当前音频存储的头部信息
     **/
    private HashMap<String, String> soundMap = new HashMap<>();

    /**
     * 当前声道，分为左声道，右声道和立体声
     **/
    private MuteEnum muteEnum = MuteEnum.MUTE_CENTER;

    /**
     * 音频播放进度获取
     **/
    private final int TIME_INFO = 100;

    /**
     * 音频振幅获取
     **/
    private final int VOLUME_DB = 101;

    /**
     * 音频头部信息获取
     **/
    private final int SOUND_INFO = 102;

    /**
     * 停止播放器
     **/
    private final int STOP_AUDIO = 103;

    /**
     * 回调到主线程
     **/
    private Handler handler;

    /**
     * 当前播放时间回调
     **/
    private PlayTime playTime;

    /**
     * 即将播放的音频资源，url或本地文件
     **/
    private String source;

    /**
     * 下一个将要播放的音频资源，url或本地文件,如不为空，上一首播放完直接播放下一首
     **/
    private String nextSource;

    public AudioEngine() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case TIME_INFO:
                        //播放进度
                        if (playTime != null && onPlayerProgressListener != null) {
                            onPlayerProgressListener.progress((PlayTime) msg.obj);
                        }
                        break;
                    case VOLUME_DB:
                        //声音振幅
                        if (onPlayerVolumeDBListener != null) {
                            onPlayerVolumeDBListener.onDbValue((Integer) msg.obj);
                        }
                        break;
                    case SOUND_INFO:
                        //头部描述信息
                        if (onPlayerInfoListener != null) {
                            onPlayerInfoListener.soundInfo((HashMap<String, String>) msg.obj);
                        }
                        break;
                    case STOP_AUDIO:
                        //停止播放
                        stop(false);
                        break;
                    default:
                }
            }
        };
        playTime = new PlayTime();
    }

    /**
     * 设置播放音频源
     *
     * @param source 音频网络路径或本地文件路径
     **/
    public void setSource(String source) {
        if (TextUtils.isEmpty(source)) {
            return;
        }
        this.source = source;
        n_setSource(source);
    }

    /**
     * 设置下一个播放音频源
     *
     * @param nextSource 音频网络路径或本地文件路径，如不为空，上一首播放完直接播放下一首
     **/
    public void setNextSource(String nextSource) {
        if (TextUtils.isEmpty(nextSource)) {
            return;
        }
        this.nextSource = nextSource;
    }

    /**
     * 设置播放状态监听器
     *
     * @param onPlayerStateListener 播放状态监听回调
     **/
    public void setPlayerStateListener(OnPlayerStateListener onPlayerStateListener) {
        this.onPlayerStateListener = onPlayerStateListener;
    }

    /**
     * 设置播放错误监听器
     *
     * @param onPlayerErrorListener 播放错误监听回调
     **/
    public void setPlayerErrorListener(OnPlayerErrorListener onPlayerErrorListener) {
        this.onPlayerErrorListener = onPlayerErrorListener;
    }

    /**
     * 设置播放进度监听器
     *
     * @param onPlayerProgressListener 播放进度监听回调
     **/
    public void setPlayerProgressListener(OnPlayerProgressListener onPlayerProgressListener) {
        this.onPlayerProgressListener = onPlayerProgressListener;
    }

    /**
     * 设置播放声音振幅监听
     *
     * @param onPlayerVolumeDBListener 播放声音振幅监听回调
     **/
    public void setOnPlayerVolumeDBListener(OnPlayerVolumeDBListener onPlayerVolumeDBListener) {
        this.onPlayerVolumeDBListener = onPlayerVolumeDBListener;
    }

    /**
     * 设置播放头部信息回调
     *
     * @param onPlayerInfoListener 播放头部信息回调
     **/
    public void setOnPlayerInfoListener(OnPlayerInfoListener onPlayerInfoListener) {
        this.onPlayerInfoListener = onPlayerInfoListener;
    }

    /**
     * 开始播放音频
     **/
    public void start() {
        if (TextUtils.isEmpty(source)) {
            onPlayerErrorListener.onError(2002, "please set media source before play");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_start();
            }
        }).start();
    }

    /**
     * 暂停播放音频
     **/
    public void pause() {
        n_pause();
        onPause();
    }

    /**
     * 恢复播放音频
     **/
    public void resume() {
        n_resume();
        onResume();
    }

    /**
     * 设置音频播放进度
     *
     * @param progress 指定音频播放位置，对直播不生效
     **/
    public void seek(int progress) {
        if (progress < 0) {
            onPlayerErrorListener.onError(2003, "seek must be bigger than zero,otherwise it invalid");
            return;
        }
        n_seek(progress);
    }

    /**
     * 停止音频播放
     **/
    public void stop() {
        stop(true);
    }

    /**
     * 停止音频播放
     *
     * @param activeStop true，主动调用停止，false，正常播放停止
     **/
    private void stop(boolean activeStop) {
        this.activeStop = activeStop;
        duration = -1;
        n_stop();
    }

    /**
     * 音频开始播放前的加载回调
     *
     * @param load true准备，false加载完毕
     **/
    public void onLoad(boolean load) {
        if (onPlayerStateListener != null) {
            if (load) {
                onPlayerStateListener.play(PlayState.ON_LOAD);
            } else {
                Message message = handler.obtainMessage();
                message.what = SOUND_INFO;
                message.obj = soundMap;
                handler.sendMessage(message);
                onPlayerStateListener.play(PlayState.LOAD_OVER);
            }
        }
    }

    /**
     * 音频开始播放的回调
     **/
    public void onStart() {
        if (onPlayerStateListener != null) {
            onPlayerStateListener.play(PlayState.ON_START);
        }
    }

    /**
     * 音频播放过程中加载回调
     *
     * @param load true加载中，false加载完毕
     **/
    public void onLoading(boolean load) {
        if (onPlayerStateListener != null) {
            if (load) {
                onPlayerStateListener.play(PlayState.ON_LOADING);
            } else {
                onPlayerStateListener.play(PlayState.LOADING_OVER);
            }
        }
    }

    /**
     * 音频开始暂停播放的回调
     **/
    public void onPause() {
        if (onPlayerStateListener != null) {
            onPlayerStateListener.play(PlayState.ON_PAUSE);
        }
    }

    /**
     * 音频开始恢复播放的回调
     **/
    public void onResume() {
        if (onPlayerStateListener != null) {
            onPlayerStateListener.play(PlayState.ON_RESUME);
        }
    }

    /**
     * 音频播放结束回调
     **/
    public void onComplete() {
        if (onPlayerStateListener != null) {
            onPlayerStateListener.play(PlayState.ON_STOP);
        }
        if (!activeStop) {
            Message message = new Message();
            message.what = STOP_AUDIO;
            handler.sendMessage(message);
        }
        if (onPlayerStateListener != null) {
            onPlayerStateListener.play(PlayState.ON_DESTROY);
        }
    }

    /**
     * 音频播放错误回调
     *
     * @param code 错误编码
     * @param msg  错误信息
     **/
    public void onError(int code, String msg) {
        if (!activeStop) {
            Message message = new Message();
            message.what = STOP_AUDIO;
            handler.sendMessage(message);
        }
        if (onPlayerStateListener != null) {
            onPlayerErrorListener.onError(code, msg);
        }
    }

    /**
     * 音频播放时间进度回调
     *
     * @param currentTime 当前播放进度时间
     * @param totalTime   音频最大播放时间长度
     **/
    public void onTime(int currentTime, int totalTime) {
        Message message = handler.obtainMessage();
        message.what = TIME_INFO;
        playTime.currentTime = currentTime;
        playTime.totalTime = totalTime;
        message.obj = playTime;
        handler.sendMessage(message);
    }

    /**
     * 获取当音频信息
     *
     * @param key   标题
     * @param value 内容
     **/
    public void onSoundInfo(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (TextUtils.isEmpty(value)) {
            return;
        }
        soundMap.put(key, value);
    }

    /**
     * 获取音频总时长
     *
     * @return 返回总时长
     **/
    public int getDuration() {
        if (duration < 0) {
            duration = n_duration();
        }
        return duration;
    }

    public HashMap<String, String> getSoundInfo() {
        return soundMap;
    }

    /**
     * 资源销毁后回调
     **/
    public void releaseOver() {
        this.activeStop = false;
        soundMap.clear();
        playNext();
    }

    /**
     * 判断是否继续播放下一首
     **/
    private void playNext() {
        if (!TextUtils.isEmpty(nextSource)) {
            source = nextSource;
            nextSource = "";
            setSource(source);
            start();
        }
    }

    /**
     * 设置播放音量
     *
     * @param percent 音量大小，范围0-100
     **/
    public void setVolume(int percent) {
        if (percent >= 0 && percent <= 100) {
            volumePercent = percent;
            n_volume(percent);
        }
    }

    /**
     * 设置播放声道
     *
     * @param mute 0右声道，1左声道，2立体声
     **/
    public void setMute(MuteEnum mute) {
        muteEnum = mute;
        n_mute(mute.getValue());
    }

    /**
     * 设置播放音调
     *
     * @param pitch 音调，正常为1.0f
     **/
    public void setPitch(float pitch) {
        n_pitch(pitch);
    }

    /**
     * 设置播放速度
     *
     * @param speed 音速，正常为1.0f
     **/
    public void setSpeed(float speed) {
        n_speed(speed);
    }

    /**
     * 声音振幅回调
     *
     * @param db 声音振幅大小
     **/
    public void onCallVolumeDB(int db) {
        Message message = handler.obtainMessage();
        message.what = VOLUME_DB;
        message.obj = db;
        handler.sendMessage(message);
    }

    private native void n_setSource(String source);

    private native void n_start();

    private native void n_pause();

    private native void n_resume();

    private native void n_seek(int seconds);

    private native void n_stop();

    private native void n_volume(int percent);

    private native void n_mute(int mute);

    private native void n_pitch(float pitch);

    private native void n_speed(float speed);

    private native int n_duration();

}
