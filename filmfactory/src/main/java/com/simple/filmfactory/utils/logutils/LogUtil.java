package com.simple.filmfactory.utils.logutils;

import android.text.TextUtils;
import android.util.Log;

import static com.simple.filmfactory.utils.logutils.LogConfig.NORMAL_LOG_NAME;
import static com.simple.filmfactory.utils.logutils.LogConfig.writeD;
import static com.simple.filmfactory.utils.logutils.LogConfig.writeE;
import static com.simple.filmfactory.utils.logutils.LogConfig.writeI;
import static com.simple.filmfactory.utils.logutils.LogConfig.writeLocal;
import static com.simple.filmfactory.utils.logutils.LogConfig.writeV;
import static com.simple.filmfactory.utils.logutils.LogConfig.writeW;


/**
 * 日志记录工具，用户只需要调用该工具类即可
 * 想要配置信息，可以改变LogConfig类的参数即可
 * 需要注意的是，如果需要写入本地，需要存储权限
 **/

public class LogUtil {

    public static void v(String value) {
        v("verbose", value);
    }

    public static void v(String title, String value) {
        write(title, value, writeV);
    }

    public static void d(String value) {
        d("debug", value);
    }

    public static void d(String title, String value) {
        write(title, value, writeD);
    }

    public static void i(String value) {
        i("info", value);
    }

    public static void i(String title, String value) {
        write(title, value, writeI);
    }

    public static void w(String value) {
        w("warn", value);
    }

    public static void w(String title, String value) {
        write(title, value, writeW);
    }

    public static void e(String value) {
        e("error", value);
    }

    public static void e(String title, String value) {
        write(title, value, writeE);
    }

    /****
     * 单独定制展示log信息的方法
     * @param title    log的标题
     * @param value    log的内容
     * @param isShow   log是否写入本地文件
     */
    public static void show(String title, String value, Boolean isShow) {
        if (writeLocal) {
            show(title, value);
        }
        if (isShow) {
            LogToFile.write(title, value, NORMAL_LOG_NAME);
        }
    }

    /**
     * 将日志信息写到控制台和本地文件中
     */
    private static void write(String title, String value, Boolean isFileShow) {
        if (writeLocal) {
            show(title, value);
        }
        if (isFileShow) {
            LogToFile.write(title, value,NORMAL_LOG_NAME);
        }
    }

    /**
     * 最终调用系统log的封装方法
     **/
    private static boolean show(String title, String value) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(value)) {
            return false;
        }
        switch (title) {
            case "verbose":
                Log.v(title, value);
                break;
            case "info":
                Log.i(title, value);
                break;
            case "warn":
                Log.w(title, value);
                break;
            case "error":
                Log.e(title, value);
                break;
            default:
                Log.d(title, value);
                break;
        }
        return true;
    }

}
