package com.simpo.promusic.music.bean;

/**
 * @author wanxuedong
 * https://github.com/wanxuedong/PrimaryExercises
 **/
public class WlTimeInfoBean {

    /**
     * 当前播放的时间，单位秒
     **/
    private int currentTime;
    /**
     * 音频的总时长，单位秒
     **/
    private int totalTime;

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "WlTimeInfoBean{" +
                "currentTime=" + currentTime +
                ", totalTime=" + totalTime +
                '}';
    }

}
