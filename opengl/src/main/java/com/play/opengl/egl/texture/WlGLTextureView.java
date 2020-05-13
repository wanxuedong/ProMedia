package com.play.opengl.egl.texture;

import android.content.Context;
import android.util.AttributeSet;

import com.play.opengl.egl.base.WLEGLSurfaceView;

/**
 * 实现图片渲染的视图
 **/
public class WlGLTextureView extends WLEGLSurfaceView {

    public WlGLTextureView(Context context) {
        this(context, null);
    }

    public WlGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new WlTextureRender(context));
    }
}
