package com.simple.filmfactory.egl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;

import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.egl.listener.OnSurfaceCreateListener;
import com.simple.filmfactory.utils.threadXUtil.AbstractLife;
import com.simple.filmfactory.utils.threadXUtil.ThreadX;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：相机视图，通过独立的渲染层实现
 */
public class CameraView extends BaseEGLSurfaceView {

    private CameraRender wlCameraRender;
    private BaseCamera baseCamera;

    /**
     * 是否使用反面摄像头
     **/
    private boolean isBack = true;

    private int textureId = -1;

    private Context context;

    private Surface surface;

    /**
     * 当前是否在拍照中
     * **/
    private boolean isTakePicture = true;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        wlCameraRender = new CameraRender(context);
        wlCameraRender.setOnSurfaceCreateListener(new OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                baseCamera.initCamera(surfaceTexture, isBack);
                textureId = tid;
            }
        });
        baseCamera = new BaseCamera(context, getMeasuredWidth(), getMeasuredHeight());
        baseCamera.setTakePicture(isTakePicture);
        setRender(wlCameraRender);
        //调整预览角度
        previewAngle(context);
        //如果需要录制的话，需要传入MediaCodec的surface
        setSurfaceAndEglContext(surface, null);
    }


    private BaseCamera.CameraParameter cameraParameter;

    public void setCameraParameter(BaseCamera.CameraParameter cameraParameter) {
        this.cameraParameter = cameraParameter;
    }

    public void onDestroy() {
        if (wlCameraRender != null){
            wlCameraRender.clearCanvas();
        }
        if (baseCamera != null) {
            baseCamera.stopPreview();
        }
    }

    public BaseCamera getWlCamera() {
        return baseCamera;
    }

    /**
     * 前后摄像头切换
     * **/
    public void switchCamera() {
        if (baseCamera != null) {
            ThreadX.x().run(new AbstractLife() {
                @Override
                public void run() {
                    super.run();
                    isBack = !isBack;
                    baseCamera.switchCamera();
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
     * 设置是否在拍照中
     * @param takePicture 是否在拍照中，true：拍照，false：录像
     * **/
    public void setTakePicture(boolean takePicture) {
        this.isTakePicture = takePicture;
        if (baseCamera != null){
            baseCamera.setTakePicture(takePicture);
        }
    }

    /**
     * 设置需要打开的摄像头
     * @param isBack 是否是后置摄像头
     * **/
    public void setBack(boolean isBack) {
        this.isBack = isBack;
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
