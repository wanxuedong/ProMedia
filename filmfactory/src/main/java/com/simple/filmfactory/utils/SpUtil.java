package com.simple.filmfactory.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户app存储一些临时文件
 * 使用get获取数据，put设置数据
 **/

public class SpUtil extends BaseUtil{

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    //SharedPreferences存储数据文件名称
    public static final String SP_INFO = "app_sp_edit";

    private SpUtil() {
    }

    /**
     * 初始化SpUtil工具类
     **/
    private static void init(Application app) {
        if (editor != null) {
            return;
        }
        sharedPreferences = app.getSharedPreferences(SP_INFO, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 获取key对应的value
     **/
    public static String get(String key) {
        checkInit();
        init(context);
        return get(key, "");
    }

    /**
     * 获取key对应的value，并设置默认返回值
     **/
    public static String get(String key, String value) {
        checkInit();
        init(context);
        if (isNullOrEmpty(key)) {
            return "";
        }
        if (value == null) {
            value = "";
        }
        return sharedPreferences.getString(key, value);
    }


    /**
     * 存储key对应value
     **/
    public static void put(String key, String value) {
        checkInit();
        init(context);
        if (isNullOrEmpty(key)) {
            return;
        }
        if (value == null) {
            value = "";
        }
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 存储key对应value
     **/
    public static void put(String key, boolean value) {
        checkInit();
        init(context);
        if (isNullOrEmpty(key)) {
            return;
        }
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 获取key对应的value
     **/
    public static boolean getBoolean(String key) {
        checkInit();
        init(context);
        return getBoolean(key, "");
    }

    /**
     * 获取key对应的value，并设置默认返回值
     **/
    public static boolean getBoolean(String key, String value) {
        checkInit();
        init(context);
        if (isNullOrEmpty(key)) {
            return false;
        }
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * 检查字符串是否为空
     **/
    private static boolean isNullOrEmpty(String value) {
        if (value == null || "".equals(value) || value.length() == 0) {
            return true;
        }
        return false;
    }


}
