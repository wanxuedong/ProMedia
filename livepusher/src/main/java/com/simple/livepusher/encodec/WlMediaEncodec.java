package com.simple.livepusher.encodec;

import android.content.Context;

public class WlMediaEncodec extends WlBaseMediaEncoder{

    private WlEncodecRender wlEncodecRender;

    public  WlMediaEncodec(Context context, int textureId) {
        super(context);
        wlEncodecRender = new WlEncodecRender(context, textureId);
        setRender(wlEncodecRender);
        setmRenderMode(WlBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
