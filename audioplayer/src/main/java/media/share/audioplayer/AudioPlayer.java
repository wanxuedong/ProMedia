package media.share.audioplayer;

import media.share.audioplayer.bean.MuteEnum;
import media.share.audioplayer.listener.OnPlayerErrorListener;
import media.share.audioplayer.listener.OnPlayerInfoListener;
import media.share.audioplayer.listener.OnPlayerProgressListener;
import media.share.audioplayer.listener.OnPlayerStateListener;
import media.share.audioplayer.listener.OnPlayerVolumeDBListener;

/**
 * @author wan
 * 创建日期：2021/12/11
 * 描述：音频播放器，用于封装AudioEngine，避免暴露不必要的C++回调方法
 */
public class AudioPlayer {

    /**
     * 音频播放器处理引擎
     **/
    private media.share.audioplayer.AudioEngine audioEngine;

    public AudioPlayer() {
        audioEngine = new media.share.audioplayer.AudioEngine();
    }

    /**
     * 设置播放音频源
     *
     * @param source 音频网络路径或本地文件路径
     **/
    public void setSource(String source) {
        audioEngine.setSource(source);
    }

    /**
     * 设置下一个播放音频源
     *
     * @param nextSource 音频网络路径或本地文件路径
     **/
    public void setNextSource(String nextSource) {
        audioEngine.setNextSource(nextSource);
    }

    /**
     * 开始播放音频
     **/
    public void start() {
        audioEngine.start();
    }

    /**
     * 暂停播放音频
     **/
    public void pause() {
        audioEngine.pause();
    }

    /**
     * 恢复播放音频
     **/
    public void resume() {
        audioEngine.resume();
    }

    /**
     * 设置音频播放进度
     *
     * @param progress 指定音频播放位置，对直播不生效
     **/
    public void seek(int progress) {
        audioEngine.seek(progress);
    }


    /**
     * 设置播放音量
     *
     * @param percent 音量大小，范围0-100
     **/
    public void setVolume(int percent) {
        audioEngine.setVolume(percent);
    }

    /**
     * 设置播放声道
     *
     * @param mute 0右声道，1左声道，2立体声
     **/
    public void setMute(MuteEnum mute) {
        audioEngine.setMute(mute);
    }

    /**
     * 设置播放音调
     *
     * @param pitch 音调，正常为1.0f
     **/
    public void setPitch(float pitch) {
        audioEngine.setPitch(pitch);
    }

    /**
     * 设置播放速度
     *
     * @param speed 音调，正常为1.0f
     **/
    public void setSpeed(float speed) {
        audioEngine.setSpeed(speed);
    }

    /**
     * 停止音频播放
     **/
    public void stop() {
        audioEngine.stop();
    }

    /**
     * 获取音频总时长
     *
     * @return 返回总时长
     **/
    public int getDuration() {
        return audioEngine.getDuration();
    }

    /**
     * 设置播放状态监听器
     *
     * @param onPlayerStateListener 播放状态监听回调
     **/
    public void setPlayerStateListener(OnPlayerStateListener onPlayerStateListener) {
        audioEngine.setPlayerStateListener(onPlayerStateListener);
    }

    /**
     * 设置播放错误监听器
     *
     * @param onPlayerErrorListener 播放错误监听回调
     **/
    public void setPlayerErrorListener(OnPlayerErrorListener onPlayerErrorListener) {
        audioEngine.setPlayerErrorListener(onPlayerErrorListener);
    }

    /**
     * 设置播放进度监听器
     *
     * @param onPlayerProgressListener 播放进度监听回调
     **/
    public void setPlayerProgressListener(OnPlayerProgressListener onPlayerProgressListener) {
        audioEngine.setPlayerProgressListener(onPlayerProgressListener);
    }

    /**
     * 设置播放声音振幅监听
     *
     * @param onPlayerVolumeDBListener 播放声音振幅监听回调
     **/
    public void setOnPlayerVolumeDBListener(OnPlayerVolumeDBListener onPlayerVolumeDBListener) {
        audioEngine.setOnPlayerVolumeDBListener(onPlayerVolumeDBListener);
    }

    /**
     * 设置播放头部信息回调
     *
     * @param onPlayerInfoListener 播放头部信息回调
     **/
    public void setOnPlayerInfoListener(OnPlayerInfoListener onPlayerInfoListener) {
        audioEngine.setOnPlayerInfoListener(onPlayerInfoListener);
    }


}
