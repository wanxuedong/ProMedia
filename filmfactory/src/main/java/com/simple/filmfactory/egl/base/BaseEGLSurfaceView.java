package com.simple.filmfactory.egl.base;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.simple.filmfactory.egl.base.thread.EGLThread;
import com.simple.filmfactory.egl.listener.GLRender;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 自定义的SurfaceView
 * 创建EGL环境并设置回调，完成普通SurfaceView无法完成多屏渲染和更多处理
 **/
public abstract class BaseEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback{


    public Surface surface;
    public EGLContext eglContext;

    private EGLThread eglThread;
    public GLRender glRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    public int mRenderMode = RENDERMODE_CONTINUOUSLY;


    public BaseEGLSurfaceView(Context context) {
        this(context, null);
    }

    public BaseEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    public void setRender(GLRender glRender) {
        this.glRender = glRender;
    }

    public void setRenderMode(int mRenderMode) {

        if(glRender == null)
        {
            throw  new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext)
    {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext()
    {
        if(eglThread != null)
        {
            return eglThread.getEglContext();
        }
        return null;
    }

    public void requestRender()
    {
        if(eglThread != null)
        {
            eglThread.requestRender();
        }
    }

    private SurfaceHolder holder;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(surface == null)
        {
            surface = holder.getSurface();
        }
        this.holder = holder;
        eglThread = new EGLThread(new WeakReference<BaseEGLSurfaceView>(this));
        eglThread.isCreate = true;
        eglThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        eglThread.width = width;
        eglThread.height = height;
        eglThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        eglThread.onDestory();
        eglThread = null;
        surface = null;
        eglContext = null;
    }

}
