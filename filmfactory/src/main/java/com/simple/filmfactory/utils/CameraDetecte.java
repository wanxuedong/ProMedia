package com.simple.filmfactory.utils;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.List;

import static com.simple.filmfactory.utils.CameraConfig.AUTO_FOCUS_TIME;

public class CameraDetecte {


    /**
     * 检测设备是否含有摄像头
     */
    public static boolean hasCamera(Context context) {
        // 不兼容Android 5.0以下版本
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIds = new String[0];
        try {
            cameraIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (cameraIds != null && cameraIds.length > 0) {
            return true;
        }
        return false;
    }

    /**
     * 调整摄像头合适预览图片清晰度大小，个人化分三个等级，实际上不同设备有很多种
     * todo 存在调整不同清晰度不显示的问题
     * FLUENT   最低清晰度
     * NORMAL   正常清晰度
     * HIGH     最高清晰度
     **/
    public static void setDefaultSize(Camera camera, DEFINITION set) {
        int width = 0, height = 0;
        Camera.Parameters parameters = camera.getParameters();
        //获取支持的预览各种分辨率
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        if (sizes.size() > 0) {
            switch (set) {
                case FLUENT:
                    width = sizes.get(sizes.size() - 1).width;
                    height = sizes.get(sizes.size() - 1).height;
                    break;
                case NORMAL:
                    int center = sizes.size() / 2;
                    width = sizes.get(center).width;
                    height = sizes.get(center).height;
                    break;
                default:
                    width = sizes.get(0).width;
                    height = sizes.get(0).height;
            }
        }
        if (width != 0 && height != 0) {
            //设置预览尺寸，注意要在摄像头支持的范围内选择
            parameters.setPreviewSize(width, height);
            //设置照片分辨率，注意要在摄像头支持的范围内选择
            parameters.setPictureSize(width, height);
            //设置照相机参数
            camera.setParameters(parameters);
        }
    }

    /**
     * 摄像头三种清晰度,这里我是人为划分了等级，清晰度根据预览分辨率来，实际上一个设备可能支持多组预览宽高
     **/
    public enum DEFINITION {
        FLUENT, NORMAL, HIGH
    }

    /**
     * 设置相机聚焦模式
     * todo 貌似没用，待优化
     **/
    public static boolean setFocusMode(Camera camera, String mode) {
        if (camera == null || mode == null || "".equals(mode)) {
            return false;
        }
        camera.getParameters().setFocusMode(mode);
        return true;
    }

    /**
     * 是否已经开启定时聚焦
     **/
    private boolean hasAuto = false;
    private Handler handler;

    /**
     * 给摄像头添加定时聚焦功能
     **/
    public void autoFocus(final Camera camera) {
        if (hasAuto) {
            return;
        }
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                hasAuto = true;
                if (handler != null) {
                    handler.sendMessageDelayed(handler.obtainMessage(), AUTO_FOCUS_TIME);
                    toFocus(camera, null);
                }
            }
        };
        handler.sendMessageDelayed(handler.obtainMessage(), AUTO_FOCUS_TIME);
    }

    /**
     * 停止自动聚焦
     **/
    public void stopAutoFocus() {
        hasAuto = false;

        handler = null;
    }

    /**
     * 手动对相机进行聚焦,需要相机处于运行状态才可以，比如调用拍照后，就不可以
     **/
    public static boolean toFocus(Camera camera, final CallBack callBack) {
        if (camera == null) {
            return false;
        }
        try {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (callBack != null) {
                        callBack.call("focus_on_success");
                    }
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 返回相机支持的图片尺寸,反面和正面支持的尺寸列表
     **/
    private static List<Camera.Size> backSupportList;
    private static List<Camera.Size> SupportList;

    /**
     * 返回相机支持的图片尺寸
     **/
    public static List<Camera.Size> getCameraSupportSize(boolean isBack, Camera.Parameters parameters) {
        //获取支持的图片尺寸，由小到大
        if (isBack) {
            if (parameters != null) {
                backSupportList = parameters.getSupportedPictureSizes();
            } else {
                return backSupportList;
            }
            return backSupportList;
        } else {
            if (parameters != null) {
                SupportList = parameters.getSupportedPictureSizes();
            } else {
                return SupportList;
            }
            return SupportList;
        }
    }

    /**
     * 返回相机支持的预览尺寸
     **/
    public static List<Camera.Size> getCameraPreviewSize(Camera.Parameters parameters) {
        //获取支持的拍照尺寸，由大到小
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        return list;
    }

}
