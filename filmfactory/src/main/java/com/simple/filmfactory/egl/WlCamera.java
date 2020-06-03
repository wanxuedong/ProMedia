package com.simple.filmfactory.egl;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.NonNull;

import com.simple.filmfactory.bean.CameraSets;
import com.simple.filmfactory.utils.CameraDetecte;
import com.simple.filmfactory.utils.FileSaveUtil;
import com.simple.filmfactory.utils.logutils.LogUtil;

import java.io.IOException;
import java.util.List;

public class WlCamera {

    private Camera camera;


    private SurfaceTexture surfaceTexture;
    private CameraDetecte cameraDetecte;

    /**
     * 是否使用反面摄像头
     **/
    public boolean isBack = true;

    private Context context;

    /**
     * 预览界面的宽高，如果没有传入指定的宽高，就用这个数据
     **/
    private int suggestWidth = 0;
    private int suggestHeight = 0;

    /**
     * 指定的拍照后的图片宽高
     **/
    private int appointWidth = 0;
    private int appointHeight = 0;
    /**
     * 相机界面的设置
     **/
    private CameraSets cameraSets;

    public WlCamera(Context context, int suggestWidth, int suggestHeight) {
        this.context = context;
        this.suggestWidth = suggestWidth;
        this.suggestHeight = suggestHeight;
    }

    public void initCamera(SurfaceTexture surfaceTexture, boolean isBack) {
        this.surfaceTexture = surfaceTexture;
        switchCamera(isBack);
    }

    public Camera getCamera() {
        return camera;
    }

    /**
     * 切换摄像头
     **/
    public void switchCamera() {
        stopPreview();
        switchCamera(!isBack);
    }

    /**
     * 切换摄像头,预览和图片的像素都使用了设备最高支持的分辨率
     **/
    public void switchCamera(boolean isBack) {
        try {
            cameraSets = (CameraSets) FileSaveUtil.readSerializable("camera_setting.txt");
            if (cameraSets != null) {
                if (isBack) {
                    appointWidth = cameraSets.getPreviewWidth();
                    appointHeight = cameraSets.getPreviewHeight();
                } else {
                    appointWidth = cameraSets.getSelfieWidth();
                    appointHeight = cameraSets.getSelfieHeight();
                }
            }
            camera = openCamera(isBack);
            cameraDetecte = new CameraDetecte();
            camera.setPreviewTexture(surfaceTexture);
            Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode("off");
            parameters.setPreviewFormat(ImageFormat.NV21);

            //获取支持的图片尺寸，由小到大
            List<Camera.Size> picList = CameraDetecte.getCameraSupportSize(isBack,parameters);
            if (appointWidth == 0 || appointHeight == 0) {
                parameters.setPictureSize(picList.get(picList.size() - 1).width, picList.get(picList.size() - 1).height);
            } else {
                parameters.setPictureSize(appointWidth, appointHeight);
            }

            //获取支持的预览尺寸，由大到小
            List<Camera.Size> previewList = CameraDetecte.getCameraPreviewSize(parameters);
            Camera.Size previewSize = getOptimalSize(previewList, suggestWidth, suggestHeight);
            //设置预览的宽高需要主要和预览的布局宽高比保持一致预览时画面才不会被拉伸，
            // 并且由于预览宽高只能摄像头支持的数据，所以这里传入的宽高是经过和控件的宽高比选择误差最小的数据
            parameters.setPreviewSize(previewSize.width, previewSize.height);

            camera.setParameters(parameters);
            camera.startPreview();
            cameraDetecte.autoFocus(camera);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     **/
    public void stopPreview() {
        cameraDetecte.stopAutoFocus();
        if (camera != null) {
            camera.startPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 是否打开并获取摄像头
     *
     * @param isBack 打开后置还是前置摄像头
     */
    private Camera openCamera(boolean isBack) {
        this.isBack = isBack;
        int cameraCount;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        if (cameraCount == 0) {
            return null;
        }
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            //判断是否打开后置摄像头
            if (cameraInfo.facing == (isBack ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                try {
                    camera = Camera.open(camIdx);
                    Log.d("openCamera", camIdx + "");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return camera;
    }

    /**
     * 根据目标的宽高比，获取一组数据中宽高比最接近的一组数据
     * 但是需要注意的是，这样获取到的一组数据可能是分辨率很低的一组，所以最好控制好控件的宽高
     **/
    private static Camera.Size getOptimalSize(@NonNull List<Camera.Size> sizes, float w, float h) {
        //需要靠近的宽高比
        float originalScale = w / h;
        //最小宽高比误差
        float errorValue = 0;
        //获取到的最接近的一组宽高比数据
        Camera.Size result = null;

        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size target = sizes.get(i);
            float proportion = (float) target.height / (float) target.width;
            if (i == 0) {
                errorValue = Math.abs(proportion - originalScale);
                result = target;
            } else {
                if (Math.abs(proportion - originalScale) < errorValue) {
                    errorValue = Math.abs(proportion - originalScale);
                    result = target;
                }
            }
        }

        return result;
    }

}
