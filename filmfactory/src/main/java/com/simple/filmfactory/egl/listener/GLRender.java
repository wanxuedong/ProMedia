package com.simple.filmfactory.egl.listener;

/**
 * @author wan
 * 创建日期：2022/08/06
 * 描述：GL渲染器
 */
public interface GLRender {

    /**
     * surface创建回调
     **/
    void onSurfaceCreated();

    /**
     * surface宽高变化回调
     *
     * @param width  屏幕宽度
     * @param height 屏幕高度
     **/
    void onSurfaceChanged(int width, int height);

    /**
     * 渲染回调
     **/
    void onDrawFrame();

}
