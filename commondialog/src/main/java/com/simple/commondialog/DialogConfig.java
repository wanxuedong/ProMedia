package com.simple.commondialog;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * 用于配置弹框的一些样式
 **/

public class DialogConfig {

    private int gravity = Gravity.BOTTOM;     //对齐方式
    private int style = R.style.DownToUpDialog;           //加载的样式
    private int width, height;   //宽高
    private boolean canceledOnTouchOutside = true;   //点击外部是否会消失
    private boolean cancelable = true;   //点击返回键是否会消失
    private boolean hasSetStyle = false; //是否已经配置过样式

    public boolean isCanceledOnTouchOutside() {
        return canceledOnTouchOutside;
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public int getGravity() {
        return gravity;
    }

    public DialogConfig setGravity(int gravity) {
        this.gravity = gravity;
        if (!hasSetStyle) {
            if (gravity == Gravity.CENTER) {
                style = R.style.CenterDialog;
                return this;
            }
            style = R.style.DownToUpDialog;
        }
        return this;
    }

    public int getStyle() {
        return style;
    }

    public DialogConfig setStyle(int style) {
        this.style = style;
        hasSetStyle = true;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public DialogConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public DialogConfig setHeight(int height) {
        this.height = height;
        return this;
    }

    //获取屏幕宽高
    private static int[] getScreenWidthHeight(Context context) {
        int[] data = new int[2];
        WindowManager manager = ((Activity) context).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        data[0] = outMetrics.widthPixels;
        data[1] = outMetrics.heightPixels;
        return data;
    }

}

