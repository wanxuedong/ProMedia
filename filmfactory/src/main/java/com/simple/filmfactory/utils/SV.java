package com.simple.filmfactory.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 快速设置TextView和ImageView本地资源
 **/
public class SV {

    public static void set(TextView view, int content) {
        set(view, content + "");
    }

    public static void set(TextView view, long content) {
        set(view, content + "");
    }

    public static void set(TextView view, double content) {
        set(view, content + "");
    }

    public static void set(EditText view, int content) {
        set(view, content + "");
    }

    public static void set(EditText view, long content) {
        set(view, content + "");
    }

    public static void set(EditText view, double content) {
        set(view, content + "");
    }

    public static void set(TextView view, String content) {
        if (isNotNull(view) && !TextUtils.isEmpty(content)) {
            view.setText(content);
        }
    }

    public static void set(EditText view, String content) {
        if (isNotNull(view) && !TextUtils.isEmpty(content)) {
            view.setText(content);
        }
    }

    public static void set(ImageView view, int content) {
        if (isNotNull(view) && !TextUtils.isEmpty(content + "")) {
            view.setImageResource(content);
        }
    }

    private static boolean isNotNull(View view) {
        if (view == null) {
            return false;
        }
        return true;
    }

    public static String toString(TextView textView) {
        if (textView == null || textView.getText() == null) {
            return "";
        }
        return textView.getText().toString().trim();
    }

    public static String toString(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

}
