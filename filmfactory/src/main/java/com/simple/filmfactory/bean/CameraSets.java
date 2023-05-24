package com.simple.filmfactory.bean;

import android.hardware.Camera;

import androidx.annotation.NonNull;

import com.simple.filmfactory.utils.CameraDetecte;
import com.simple.filmfactory.utils.FileSaveUtil;

import java.io.Serializable;
import java.util.List;

/**
 * 关于摄像机相关配置
 **/
public class CameraSets implements Serializable {

    /**
     * 后置摄像头录制的视频宽度
     **/
    private int backVideoWidth = 0;
    /**
     * 后置摄像头录制的视频高度
     **/
    private int backVideoHeight = 0;
    /**
     * 后置摄像头拍照图片宽度
     **/
    private int backPictureWidth = 0;
    /**
     * 后置摄像头拍照图片高度
     **/
    private int backPictureHeight = 0;

    /**
     * 后置摄像头录制的视频宽度
     **/
    private int frontVideoWidth = 0;
    /**
     * 后置摄像头录制的视频高度
     **/
    private int frontVideoHeight = 0;
    /**
     * 后置摄像头拍照图片宽度
     **/
    private int frontPictureWidth = 0;
    /**
     * 后置摄像头拍照图片高度
     **/
    private int frontPictureHeight = 0;
    /**
     * 是否启用自动水印功能
     **/
    private boolean waterOpen = true;

    /**
     * 水印位置
     * 1:左上,2:左下,3:右上,4右下
     **/
    private int waterPosition = 4;

    /**
     * 水印大小
     * 1:大,2:中,3:小
     **/
    private int waterSize = 3;

    /**
     * 水印颜色
     * 1:黑,2:红,3:绿，4:白
     **/
    private int waterColor = 4;

    /**
     * 水印内容
     **/
    private String waterString = "";

    public int getBackVideoWidth() {
        if (backVideoWidth == 0) {
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(true, null);
            if (supportList != null && supportList.size() > 0) {
                backVideoWidth = supportList.get(supportList.size() / 2).width;
            } else {
                backVideoWidth = 0;
            }
        }
        return backVideoWidth;
    }

    public void setBackVideoWidth(int backVideoWidth) {
        this.backVideoWidth = backVideoWidth;
    }

    public int getBackVideoHeight() {
        if (backVideoHeight == 0) {
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(true, null);
            if (supportList != null && supportList.size() > 0) {
                backVideoHeight = supportList.get(supportList.size() / 2).height;
            } else {
                backVideoHeight = 0;
            }
        }
        return backVideoHeight;
    }

    public void setBackVideoHeight(int backVideoHeight) {
        this.backVideoHeight = backVideoHeight;
    }

    public boolean isWaterOpen() {
        return waterOpen;
    }

    public void setWaterOpen(boolean waterOpen) {
        this.waterOpen = waterOpen;
    }

    public void setBackPictureHeight(int backPictureHeight) {
        this.backPictureHeight = backPictureHeight;
    }

    public void setBackPictureWidth(int backPictureWidth) {
        this.backPictureWidth = backPictureWidth;
    }

    public void setFrontPictureHeight(int frontPictureHeight) {
        this.frontPictureHeight = frontPictureHeight;
    }

    public void setFrontPictureWidth(int frontPictureWidth) {
        this.frontPictureWidth = frontPictureWidth;
    }

    public void setFrontVideoHeight(int frontVideoHeight) {
        this.frontVideoHeight = frontVideoHeight;
    }

    public void setFrontVideoWidth(int frontVideoWidth) {
        this.frontVideoWidth = frontVideoWidth;
    }

    public int getFrontPictureHeight() {
        return frontPictureHeight;
    }

    public int getFrontPictureWidth() {
        return frontPictureWidth;
    }

    public int getFrontVideoHeight() {
        return frontVideoHeight;
    }

    public int getFrontVideoWidth() {
        return frontVideoWidth;
    }

    public void setWaterPosition(int waterPosition) {
        this.waterPosition = waterPosition;
    }

    public int getWaterPosition() {
        return waterPosition;
    }

    public void setWaterSize(int waterSize) {
        this.waterSize = waterSize;
    }

    public int getWaterSize() {
        return waterSize;
    }

    public void setWaterString(String waterString) {
        this.waterString = waterString;
    }

    public String getWaterString() {
        return waterString;
    }

    public void setWaterColor(int waterColor) {
        this.waterColor = waterColor;
    }

    public int getWaterColor() {
        return waterColor;
    }

    public int getBackPictureHeight() {
        if (backPictureHeight == 0) {
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(false, null);
            if (supportList != null && supportList.size() > 0) {
                backPictureHeight = supportList.get(supportList.size() / 2).height;
            } else {
                backPictureHeight = 0;
            }
        }
        return backPictureHeight;
    }

    public int getBackPictureWidth() {
        if (backPictureWidth == 0) {
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(false, null);
            if (supportList != null && supportList.size() > 0) {
                backPictureWidth = supportList.get(supportList.size() / 2).width;
            } else {
                backPictureWidth = 0;
            }
        }
        return backPictureWidth;
    }

    public void initCameraSize(boolean isBack, Camera.Parameters parameters) {
        if (isBack) {
            List<Camera.Size> pictureList = CameraDetecte.getCameraSupportSize(false, parameters);
            if (backPictureWidth == 0 && pictureList != null && pictureList.size() > 0) {
                backPictureWidth = pictureList.get(pictureList.size() / 2).width;
                backPictureHeight = pictureList.get(pictureList.size() / 2).height;
            }
            List<Camera.Size> videoList = CameraDetecte.getCameraSupportSize(true, parameters);
            if (backVideoWidth == 0 && videoList != null && videoList.size() > 0) {
                backVideoWidth = videoList.get(videoList.size() / 2).width;
                backVideoHeight = videoList.get(videoList.size() / 2).height;
            }
        } else {
            List<Camera.Size> pictureList = CameraDetecte.getCameraSupportSize(false, parameters);
            if (frontPictureWidth == 0 && pictureList != null && pictureList.size() > 0) {
                frontPictureWidth = pictureList.get(pictureList.size() / 2).width;
                frontPictureHeight = pictureList.get(pictureList.size() / 2).height;
            }
            List<Camera.Size> videoList = CameraDetecte.getCameraSupportSize(true, parameters);
            if (frontVideoWidth == 0 && videoList != null && videoList.size() > 0) {
                frontVideoWidth = videoList.get(videoList.size() / 2).width;
                frontVideoHeight = videoList.get(videoList.size() / 2).height;
            }
        }
        FileSaveUtil.saveSerializable("camera_setting.txt", this);
    }

    @NonNull
    @Override
    public String toString() {
        return "previewWidth " + backVideoWidth + " : " + "previewHeight " + backVideoHeight + " : " + "selfieWidth " + backPictureWidth + " : " + "selfieHeight " + backPictureHeight + " : " + waterOpen;
    }
}
