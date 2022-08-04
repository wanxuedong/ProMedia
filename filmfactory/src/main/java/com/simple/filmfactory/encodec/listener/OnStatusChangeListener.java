package com.simple.filmfactory.encodec.listener;

import com.simple.filmfactory.encodec.WlBaseMediaEncoder;

/**
 * @author wan
 * 创建日期：2022/08/05
 * 描述：录制状态监听
 */
public interface OnStatusChangeListener {

    /**
     * 录制状态变化监听
     *
     * @param status 当前录制状态
     **/
    void onStatusChange(OnStatusChangeListener.STATUS status);

    enum STATUS {
        //开始录制
        START,
        //录制结束
        END
    }

}