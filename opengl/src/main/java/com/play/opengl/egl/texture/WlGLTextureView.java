package com.play.opengl.egl.texture;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.play.opengl.egl.base.WLEGLSurfaceView;

/**
 * 实现图片渲染的视图
 **/
public class WlGLTextureView extends WLEGLSurfaceView {

    private WlTextureRender wlTextureRender;

    public WlGLTextureView(Context context) {
        this(context, null);
    }

    public WlGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wlTextureRender = new WlTextureRender(context);
        setRender(wlTextureRender);
    }

    public WlTextureRender getWlTextureRender() {
        return wlTextureRender;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("WlGLTextureView", MeasureSpec.getSize(widthMeasureSpec) + " : " + MeasureSpec.getSize(heightMeasureSpec));
        wlTextureRender.setScreenWidth(MeasureSpec.getSize(widthMeasureSpec));
        wlTextureRender.setScreenHeight(MeasureSpec.getSize(heightMeasureSpec));
    }
}
