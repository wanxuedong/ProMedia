package com.simple.filmfactory.encodec.mediathread;

import static com.simple.filmfactory.encodec.WlBaseMediaEncoder.RENDERMODE_CONTINUOUSLY;
import static com.simple.filmfactory.encodec.WlBaseMediaEncoder.RENDERMODE_WHEN_DIRTY;

import com.simple.filmfactory.egl.base.EglHelper;
import com.simple.filmfactory.encodec.WlBaseMediaEncoder;

import java.lang.ref.WeakReference;

/**
 * @author wan
 * 创建日期：2022/08/05
 * 描述：开始录制时，启动EGL环境，不断渲染界面并录制到本地
 */
public class EGLMediaThread extends Thread {

    private WeakReference<WlBaseMediaEncoder> encoder;
    private EglHelper eglHelper;
    private Object object;

    private boolean isExit = false;
    public boolean isCreate = false;
    public boolean isChange = false;
    private boolean isStart = false;

    public EGLMediaThread(WeakReference<WlBaseMediaEncoder> encoder) {
        this.encoder = encoder;
    }

    @Override
    public void run() {
        super.run();
        isExit = false;
        isStart = false;
        object = new Object();
        eglHelper = new EglHelper();
        eglHelper.initEgl(encoder.get().surface, encoder.get().eglContext);

        while (true) {
            if (isExit) {
                release();
                break;
            }

            if (isStart) {
                if (encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                    synchronized (object) {
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
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
            onChange(encoder.get().width, encoder.get().height);
            onDraw();
            isStart = true;
        }

    }

    private void onCreate() {
        if (isCreate && encoder.get().wlGLRender != null) {
            isCreate = false;
            encoder.get().wlGLRender.onSurfaceCreated();
        }
    }

    private void onChange(int width, int height) {
        if (isChange && encoder.get().wlGLRender != null) {
            isChange = false;
            encoder.get().wlGLRender.onSurfaceChanged(width, height);
        }
    }

    private void onDraw() {
        if (encoder.get().wlGLRender != null && eglHelper != null) {
            encoder.get().wlGLRender.onDrawFrame();
            if (!isStart) {
                encoder.get().wlGLRender.onDrawFrame();
            }
            eglHelper.swapBuffers();

        }
    }

    private void requestRender() {
        if (object != null) {
            synchronized (object) {
                object.notifyAll();
            }
        }
    }

    public void onDestroy() {
        isExit = true;
        requestRender();
    }

    public void release() {
        if (eglHelper != null) {
            eglHelper.destoryEgl();
            eglHelper = null;
            object = null;
            encoder = null;
        }
    }
}
