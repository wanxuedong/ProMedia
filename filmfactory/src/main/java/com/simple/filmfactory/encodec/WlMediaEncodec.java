package com.simple.filmfactory.encodec;

import android.content.Context;

/**
 * 用于实现视频的录制
 * 需要注意的是没有直接使用WlCameraView的EGL环境
 * 是为了实现了预览和录制的分离，保证二者互不干扰
 **/
public class WlMediaEncodec extends WlBaseMediaEncoder {

    private WlEncodecRender wlEncodecRender;

    public WlMediaEncodec(Context context, int textureId) {
        super(context);
        wlEncodecRender = new WlEncodecRender(context, textureId);
        setRender(wlEncodecRender);
        setmRenderMode(WlBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
