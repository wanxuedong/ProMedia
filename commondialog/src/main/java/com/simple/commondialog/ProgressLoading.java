package com.simple.commondialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class ProgressLoading extends Dialog {
    private boolean isshowing;

    private static AnimationDrawable animDrawable = null;
    private static ProgressLoading mDialog = null;

    public ProgressLoading(@NonNull Context context) {
        super(context);
        create(context);
    }

    public ProgressLoading(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ProgressLoading(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public static ProgressLoading create(Context context) {
        //样式引入
        mDialog = new ProgressLoading(context, R.style.LightProgressDialog);
        //设置布局
        mDialog.setContentView(R.layout.progress_dialog);
        //返回键是否能让对话框消失
        mDialog.setCancelable(false);
        //点击外界是否可以让对话框消失
        mDialog.setCanceledOnTouchOutside(false);
        //设置对话框位置
        Objects.requireNonNull(mDialog.getWindow()).getAttributes().gravity = Gravity.CENTER;
        //Android中的属相对象就等价于OC中的结构体，想要改变其值，必须用一个中间量来去替换他
        WindowManager.LayoutParams attributes = mDialog.getWindow().getAttributes();
        attributes.dimAmount = 0.2f;
        //设置属性
        mDialog.getWindow().setAttributes(attributes);
        ImageView loadingView = mDialog.findViewById(R.id.iv_loading);
        animDrawable = (AnimationDrawable) loadingView.getBackground();
        return mDialog;
    }

    /*
        显示加载对话框，动画开始
     */
    public void showLoading() {
        if (getContext() != null && getContext() instanceof Activity) {
            if (!((Activity)getContext()).isFinishing()) {
                super.show();
                isshowing = true;
                animDrawable.start();
            }
        }
    }

    /*
        隐藏加载对话框，动画停止
     */
    public void hideLoading() {
        if (getContext() != null && getContext() instanceof Activity) {
            if (!((Activity)getContext()).isFinishing()) {
                super.dismiss();
                isshowing = false;
                animDrawable.stop();
            }
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // 下层界面不可点击
        return isshowing;// @SuppressLint("ClickableViewAccessibility")不可加.
    }
}
