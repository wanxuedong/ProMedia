package com.simple.filmfactory.egl.listener;

import android.graphics.SurfaceTexture;

/**
 * @author wan
 * 创建日期：2022/08/06
 * 描述：surface创建监听
 */
public interface OnSurfaceCreateListener {

    /**
     * surface创建回调
     *
     * @param surfaceTexture
     * @param tid
     * **/
    void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid);
}
