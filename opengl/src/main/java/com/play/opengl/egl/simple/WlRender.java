package com.play.opengl.egl.simple;

import android.opengl.GLES20;
import android.util.Log;

import com.play.opengl.egl.base.WLEGLSurfaceView;

/**
 * 按照生命周期绘制视图
 **/
public class WlRender implements WLEGLSurfaceView.WlGLRender {

    public WlRender() {
    }

    @Override
    public void onSurfaceCreated() {
        Log.d("ywl5320", "onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Log.d("pro_music", "onSurfaceChanged");
    }

    @Override
    public void onDrawFrame() {
        Log.d("pro_music", "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
    }
}
