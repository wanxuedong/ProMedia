package com.simple.filmfactory.bean;

import android.hardware.Camera;

import androidx.annotation.NonNull;

import com.simple.filmfactory.utils.CameraDetecte;

import java.io.Serializable;
import java.util.List;

/**
 * 关于摄像机相关配置
 **/
public class CameraSets implements Serializable {

    /**
     * 拍照或者录制的图片视频宽度(反面摄像头)
     **/
    private int previewWidth = 0;
    /**
     * 拍照或者录制的图片视频高度(反面摄像头)
     **/
    private int previewHeight = 0;
    /**
     * 拍照或者录制的图片视频宽度(正面摄像头)
     **/
    private int selfieWidth = 0;
    /**
     * 拍照或者录制的图片视频高度(正面摄像头)
     **/
    private int selfieHeight = 0;
    /**
     * 是否启用自动水印功能
     **/
    private boolean waterOpen = true;

    public int getPreviewWidth() {
        if (previewWidth == 0){
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(true,null);
            if (supportList != null && supportList.size() > 0){
                previewWidth = supportList.get(supportList.size() - 1).width;
            }else {
                previewWidth = 0;
            }
        }
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        if (previewHeight == 0){
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(true,null);
            if (supportList != null && supportList.size() > 0){
                previewHeight = supportList.get(supportList.size() - 1).height;
            }else {
                previewHeight = 0;
            }
        }
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public boolean isWaterOpen() {
        return waterOpen;
    }

    public void setWaterOpen(boolean waterOpen) {
        this.waterOpen = waterOpen;
    }

    public void setSelfieHeight(int selfieHeight) {
        this.selfieHeight = selfieHeight;
    }

    public void setSelfieWidth(int selfieWidth) {
        this.selfieWidth = selfieWidth;
    }

    public int getSelfieHeight() {
        if (selfieHeight ==0){
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(false,null);
            if (supportList != null && supportList.size() > 0){
                selfieHeight = supportList.get(supportList.size() - 1).height;
            }else {
                selfieHeight = 0;
            }
        }
        return selfieHeight;
    }

    public int getSelfieWidth() {
        if (selfieWidth == 0){
            List<Camera.Size> supportList = CameraDetecte.getCameraSupportSize(false,null);
            if (supportList != null && supportList.size() > 0){
                selfieWidth = supportList.get(supportList.size() - 1).width;
            }else {
                selfieWidth = 0;
            }
        }
        return selfieWidth;
    }

    @NonNull
    @Override
    public String toString() {
        return "previewWidth " + previewWidth + " : " + "previewHeight " + previewHeight + " : " + "selfieWidth " + selfieWidth + " : " + "selfieHeight " + selfieHeight + " : " + waterOpen;
    }
}
