package com.simple.filmfactory.encodec.listener;

/**
 * @author wan
 * 创建日期：2022/08/05
 * 描述：录制时长监听
 */
public interface OnMediaInfoListener {
    /**
     * 录制时长变化监听
     *
     * @param times 当前录制总时间，单位秒
     **/
    void onMediaTime(int times);
}