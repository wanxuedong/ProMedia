package com.simple.filmfactory.utils;

import android.view.SurfaceHolder;

/**
 * 相机默认参数设置
 *
 * @author wanxuedong
 * 2019/1/3
 */
public class CameraConfig {

    /**
     * 默认打开后置摄像头
     **/
    public static final boolean IS_BACK = true;

    /**
     * 摄像头默认相机清晰度为最高清晰度
     * 调整不同的选项，有时可能预览不到摄像头，这时候我们需要保证图片预览大小在摄像头接受范围内
     * todo 代码需要调整
     **/
    public static final CameraDetecte.DEFINITION DEFINITE = CameraDetecte.DEFINITION.HIGH;

    /**
     * 相机默认对焦模式
     **/
    public static final String FOCUS_MODE = "FOCUS_MODE_AUTO";

    public static final int SURFACE_TYPE = SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;

    /**
     * 相机定时聚焦间隔
     * 相机默认以打开的一瞬间距离物体的距离作为焦距，之后移动摄像头，因为没有自动聚焦功能，
     * 所以过远或过近都会导致拍摄不清晰，所以需要调整聚焦时间，然后调用强制聚焦
     **/
    public static final int AUTO_FOCUS_TIME = 3000;

}
