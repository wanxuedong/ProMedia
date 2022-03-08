package com.simple.commondialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 自定义Dialog，用于需求扩展
 * 可通过设置DialogConfig定制自己的对话框样式
 **/

public class CommonDialog extends AlertDialog {

    private View dialogView;
    private Context context;
    private DialogConfig dialogConfig;
    private Object object;
    private boolean hasConfig = false;

    public Object getObject() {
        return object;
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        this.dialogView = view;
    }

    public View getView() {
        return dialogView;
    }

    public CommonDialog setObject(Object object) {
        this.object = object;
        return this;
    }


    protected CommonDialog(Context context) {
        super(context);
        this.context = context;
    }

    protected CommonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected CommonDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void show() {
        loadConfig(this);
        super.show();
    }

    //加载配置的对话框样式
    private void loadConfig(CommonDialog commonDialog) {
        if (hasConfig) {
            return;
        } else {
            hasConfig = true;
        }
        if (dialogConfig == null) {
            dialogConfig = new DialogConfig();
        }
        dialogConfig.setWidth(getScreenWidthHeight(context)[0]);
        dialogConfig.setHeight(getScreenWidthHeight(context)[1]);
        commonDialog.setCanceledOnTouchOutside(dialogConfig.isCanceledOnTouchOutside());           //点击外部是否会会消失
        commonDialog.setCancelable(dialogConfig.isCancelable());           //点击返回键是否会消失
        Window window = commonDialog.getWindow();
        if (window != null) {
            window.setGravity(dialogConfig.getGravity());    //设置弹出位置
            window.setBackgroundDrawableResource(android.R.color.transparent);    //这样对话框内部才能透明
            commonDialog.getWindow().setWindowAnimations(dialogConfig.getStyle());     //设置加载动画
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = dialogConfig.getWidth();        // 设置dialog宽度
            lp.height = dialogConfig.getHeight();// 设置dialog高度
            commonDialog.getWindow().setAttributes(lp);

        }
    }

    //设置对话框的配置
    public void setConfig(DialogConfig dialogConfig) {
        this.dialogConfig = dialogConfig;
    }


    //设置弹框的宽高
    private static void setWidthHeight(Context context, AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getScreenWidthHeight(context)[0];// 设置dialog宽度
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;// 设置dialog高度
            window.setAttributes(lp);
        }
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
