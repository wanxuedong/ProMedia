package com.simple.filmfactory.egl.base.thread;

import static com.simple.filmfactory.egl.base.BaseEGLSurfaceView.*;

import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.egl.base.EglHelper;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * @author wan
 * 创建日期：2022/08/06
 * 描述：备注
 */


public class EGLThread extends Thread {

    private WeakReference<BaseEGLSurfaceView> wleglSurfaceViewWeakReference;
    private EglHelper eglHelper = null;
    private Object object = null;

    private boolean isExit = false;
    public boolean isCreate = false;
    public boolean isChange = false;
    private boolean isStart = false;

    public int width;
    public int height;

    public EGLThread(WeakReference<BaseEGLSurfaceView> wleglSurfaceViewWeakReference) {
        this.wleglSurfaceViewWeakReference = wleglSurfaceViewWeakReference;
    }

    @Override
    public void run() {
        super.run();
        isExit = false;
        isStart = false;
        object = new Object();
        eglHelper = new EglHelper();
        eglHelper.initEgl(wleglSurfaceViewWeakReference.get().surface, wleglSurfaceViewWeakReference.get().eglContext);

        while (true)
        {
            if(isExit)
            {
                //释放资源
                release();
                break;
            }

            if(isStart)
            {
                if(wleglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY)
                {
                    synchronized (object)
                    {
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(wleglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY)
                {
                    try {
                        Thread.sleep(1000 / 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    throw  new RuntimeException("mRenderMode is wrong value");
                }
            }


            onCreate();
            onChange(width, height);
            onDraw();

            isStart = true;


        }


    }

    private void onCreate()
    {
        if(isCreate && wleglSurfaceViewWeakReference.get().glRender != null)
        {
            isCreate = false;
            wleglSurfaceViewWeakReference.get().glRender.onSurfaceCreated();
        }
    }

    private void onChange(int width, int height)
    {
        if(isChange && wleglSurfaceViewWeakReference.get().glRender != null)
        {
            isChange = false;
            wleglSurfaceViewWeakReference.get().glRender.onSurfaceChanged(width, height);
        }
    }

    private void onDraw()
    {
        if(wleglSurfaceViewWeakReference.get().glRender != null && eglHelper != null)
        {
            wleglSurfaceViewWeakReference.get().glRender.onDrawFrame();
            if(!isStart)
            {
                wleglSurfaceViewWeakReference.get().glRender.onDrawFrame();
            }
            eglHelper.swapBuffers();

        }
    }

    public void requestRender()
    {
        if(object != null)
        {
            synchronized (object)
            {
                object.notifyAll();
            }
        }
    }

    public void onDestory()
    {
        isExit = true;
        requestRender();
    }


    public void release()
    {
        if(eglHelper != null)
        {
            eglHelper.destoryEgl();
            eglHelper = null;
            object = null;
            wleglSurfaceViewWeakReference = null;
        }
    }

    public EGLContext getEglContext()
    {
        if(eglHelper != null)
        {
            return eglHelper.getmEglContext();
        }
        return null;
    }

}
