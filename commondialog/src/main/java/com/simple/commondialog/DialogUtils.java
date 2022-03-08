package com.simple.commondialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import java.util.HashMap;


/**
 * 用于显示弹框通用工具类
 * 直接调用show方法，传入对话框标识,上下文，需要显示的资源文件即可
 * <p>
 * <p>
 * 需要注意的是，一定要记得不用的时候要调用remove方法移除
 **/

public class DialogUtils {

    private static HashMap<String, CommonDialog> dialogHashMap = new HashMap<>();

    //显示弹框，默认底部弹出,不用时务必调用remove，一般在onDestroy中
    public static CommonDialog show(String tag, Context context, int dialogView) {
        return show(tag, context, dialogView, new DialogConfig().setStyle(R.style.DownToUpDialog));
    }

    //显示弹框，默认底部弹出,不用时务必调用remove，一般在onDestroy中
    public static CommonDialog show(String tag, Context context, View dialogView) {
        return show(tag, context, dialogView, new DialogConfig().setStyle(R.style.DownToUpDialog));
    }

    //可设置style显示弹框,不用时务必调用remove，一般在onDestroy中
    public static CommonDialog show(String tag, Context context, int dialogView, DialogConfig dialogConfig) {
        return show(tag, context, LayoutInflater.from(context).inflate(dialogView, null), dialogConfig);
    }

    //可设置style显示弹框,不用时务必调用remove，一般在onDestroy中
    public static CommonDialog show(String tag, Context context, View dialogView, DialogConfig dialogConfig) {
        CommonDialog dialog;
        if (dialogHashMap.get(tag) == null) {
            dialog = new CommonDialog(context);
            dialog.setView(dialogView, 0, 0, 0, 0);
            dialog.setConfig(dialogConfig);
            dialog.show();
            dialogHashMap.put(tag, dialog);
        } else {
            dialog = dialogHashMap.get(tag);
            if (dialog != null) {
                dialog.show();
            }
        }
        return dialog;
    }

    //移除弹框引用
    public static boolean remove(String tag) {
        if (tag == null || dialogHashMap.get(tag) == null) {
            return false;
        }
        //防止Activity被finish，弹框未关闭会报错
        CommonDialog commonDialog = dialogHashMap.get(tag);
        if (commonDialog != null) {
            commonDialog.dismiss();
        }
        dialogHashMap.remove(tag);
        return true;
    }

    //获取弹框引用
    public static CommonDialog get(String tag) {
        if (dialogHashMap.get(tag) == null) {
            return null;
        }
        return dialogHashMap.get(tag);
    }

    public static void disMiss(String tag) {
        if (dialogHashMap.get(tag) == null) {
            return;
        }
        dialogHashMap.get(tag).dismiss();
    }

}
