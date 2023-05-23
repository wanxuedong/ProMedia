package com.simple.filmfactory.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.simple.filmfactory.egl.CameraView;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：相机预览宽高调整工具
 */
public class CameraViewHelper {

    /**
     * 调整相机预览界面适配选择的拍摄宽高设置
     * 如果预览界面宽高比和选择拍摄的宽高比不一致，最后拍出照片或录像会形变
     *
     * @param context      上下文
     * @param cameraView   相机视图
     * @param selectWidth  设置的视图宽度
     * @param selectHeight 设置的视图高度
     **/
    public static void autoToSize(Context context, CameraView cameraView, int selectWidth, int selectHeight) {
        if (context == null) {
            return;
        }
        if (cameraView == null) {
            return;
        }
        if (selectWidth <= 0) {
            return;
        }
        if (selectHeight <= 0) {
            return;
        }
        int screenWidth = DisplayUtil.getScreenWidth(context);
        int screenHeight = DisplayUtil.getScreenHeight(context);
        int resultWidth = screenWidth;
        int resultHeight;
        resultHeight = (int) (1.0f * screenWidth / selectWidth * selectHeight);
        if (resultHeight > screenHeight) {
            resultHeight = screenHeight;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cameraView.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.width = resultWidth;
        params.height = resultHeight;
        cameraView.setLayoutParams(params);
    }

}
