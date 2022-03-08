package com.play.opengl.egl.simple;

import android.content.Context;
import android.util.AttributeSet;

import com.play.opengl.egl.base.WLEGLSurfaceView;

/**
 * 基本的EGLSurfaceView实现
 **/
public class WlGlSurfaceView extends WLEGLSurfaceView {

    public WlGlSurfaceView(Context context) {
        this(context, null);
    }

    public WlGlSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlGlSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new WlRender());
        setRenderMode(WLEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
