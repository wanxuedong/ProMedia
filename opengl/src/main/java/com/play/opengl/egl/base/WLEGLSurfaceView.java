package com.play.opengl.egl.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 创建GEL绘制视图的基础环境，通过继承实现绘制过程
 **/
public abstract class WLEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {


    private Surface surface;
    private EGLContext eglContext;

    private WlEGLThread wlEGLThread;
    private WlGLRender wlGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;


    public WLEGLSurfaceView(Context context) {
        this(context, null);
    }

    public WLEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WLEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    /**
     * 设置显示器状态回调
     **/
    public void setRender(WlGLRender wlGLRender) {
        this.wlGLRender = wlGLRender;
    }

    /**
     * 设置画面加载模式，手动和自动
     **/
    public void setRenderMode(int mRenderMode) {

        if (wlGLRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext() {
        if (wlEGLThread != null) {
            return wlEGLThread.getEglContext();
        }
        return null;
    }

    /**
     * 重新刷新界面
     **/
    public void requestRender() {
        if (wlEGLThread != null) {
            wlEGLThread.requestRender();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surface == null) {
            surface = holder.getSurface();
        }
        wlEGLThread = new WlEGLThread(new WeakReference<WLEGLSurfaceView>(this));
        wlEGLThread.isCreate = true;
        wlEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        wlEGLThread.width = width;
        wlEGLThread.height = height;
        wlEGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        wlEGLThread.onDestroy();
        wlEGLThread = null;
        surface = null;
        eglContext = null;
    }

    /**
     * GEL生命周期管理回调
     **/
    public interface WlGLRender {

        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();
    }

    /**
     * GSL是线程相关的，可以使用一个线程维护整个绘制周期
     **/
    static class WlEGLThread extends Thread {

        private WeakReference<WLEGLSurfaceView> wleglSurfaceViewWeakReference;
        private EglHelper eglHelper = null;
        private Object object = null;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public WlEGLThread(WeakReference<WLEGLSurfaceView> wleglSurfaceViewWeakReference) {
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

            while (true) {
                if (isExit) {
                    //释放资源
                    release();
                    break;
                }

                if (isStart) {
                    if (wleglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        //脏加载模式，即需要手动调用刷新方法才会刷新界面
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (wleglSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        //自动加载模式，一帧60毫秒
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }


                onCreate();
                onChange(width, height);
                onDraw();

                isStart = true;


            }


        }

        private void onCreate() {
            if (isCreate && wleglSurfaceViewWeakReference.get().wlGLRender != null) {
                isCreate = false;
                wleglSurfaceViewWeakReference.get().wlGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && wleglSurfaceViewWeakReference.get().wlGLRender != null) {
                isChange = false;
                wleglSurfaceViewWeakReference.get().wlGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (wleglSurfaceViewWeakReference.get().wlGLRender != null && eglHelper != null) {
                wleglSurfaceViewWeakReference.get().wlGLRender.onDrawFrame();
                if (!isStart) {
                    wleglSurfaceViewWeakReference.get().wlGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        /**
         * 重新刷新界面
         **/
        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        /**
         * 退出绘制
         **/
        public void onDestroy() {
            isExit = true;
            requestRender();
        }

        /**
         * 释放资源
         **/
        public void release() {
            if (eglHelper != null) {
                eglHelper.destroyEgl();
                eglHelper = null;
                object = null;
                wleglSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext() {
            if (eglHelper != null) {
                return eglHelper.getEglContext();
            }
            return null;
        }

    }


}
