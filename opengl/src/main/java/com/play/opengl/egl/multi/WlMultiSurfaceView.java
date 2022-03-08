package com.play.opengl.egl.multi;

import android.content.Context;
import android.util.AttributeSet;

import com.play.opengl.egl.base.WLEGLSurfaceView;

public class WlMultiSurfaceView extends WLEGLSurfaceView {

    private WlMultiRender wlMultiRender;

    public WlMultiSurfaceView(Context context) {
        this(context, null);
    }

    public WlMultiSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlMultiSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wlMultiRender = new WlMultiRender(context);
        setRender(wlMultiRender);
    }

    public void setTextureId(int textureId, int index)
    {
        if(wlMultiRender != null)
        {
            wlMultiRender.setTextureId(textureId, index);
        }
    }
}
