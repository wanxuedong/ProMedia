package com.simple.filmfactory.egl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;

import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.utils.threadXUtil.AbstractLife;
import com.simple.filmfactory.utils.threadXUtil.ThreadX;


public class WlCameraView extends BaseEGLSurfaceView {

    private WlCameraRender wlCameraRender;
    private WlCamera wlCamera;

    /**
     * 是否使用反面摄像头
     **/
    private boolean isBack = true;

    private int textureId = -1;

    private Context context;

    private Surface surface;

    public WlCameraView(Context context) {
        this(context, null);
    }

    public WlCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    /**
     * 当重新加载页面或者切换横竖屏的时候，会被调用
     **/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //初始化宽高，保证选择的画面预览宽高与控件宽高比保持最接近
        wlCameraRender = new WlCameraRender(context);
        wlCamera = new WlCamera(context, getMeasuredWidth(), getMeasuredHeight());
        setRender(wlCameraRender);
        //调整预览角度
        previewAngle(context);
        wlCameraRender.setOnSurfaceCreateListener(new WlCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                wlCamera.initCamera(surfaceTexture, isBack);
                textureId = tid;
            }
        });
        //如果需要录制的话，需要传入MediaCodec的surface
        setSurfaceAndEglContext(surface, null);
    }

    public void onDestroy() {
        if (wlCamera != null) {
            wlCamera.stopPreview();
        }
    }

    public WlCamera getWlCamera() {
        return wlCamera;
    }

    public void switchCamera() {
        if (wlCamera != null) {
            ThreadX.x().run(new AbstractLife() {
                @Override
                public void run() {
                    super.run();
                    isBack = !isBack;
                    wlCamera.switchCamera();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void end() {
                    super.end();
                    previewAngle(getContext());
                }
            });
        }
    }

    /**
     * 矩阵调整图像旋转角度
     **/
    public void previewAngle(Context context) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        wlCameraRender.resetMatrix();
        switch (angle) {
            case Surface.ROTATION_0:
                if (isBack) {
                    wlCameraRender.setAngle(90, 0, 0, 1);
                    wlCameraRender.setAngle(180, 1, 0, 0);
                } else {
                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                if (isBack) {
                    wlCameraRender.setAngle(180, 0, 0, 1);
                    wlCameraRender.setAngle(180, 0, 1, 0);
                } else {
                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                if (isBack) {
                    wlCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    wlCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    wlCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if (isBack) {
                    wlCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    wlCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextureId() {
        return textureId;
    }
}
