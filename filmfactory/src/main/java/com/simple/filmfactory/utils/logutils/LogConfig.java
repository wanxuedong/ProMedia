package com.simple.filmfactory.utils.logutils;

import android.os.Environment;

import com.simple.filmfactory.utils.ApkUtil;

import java.io.File;

import static com.simple.filmfactory.utils.FileCataLog.FILE_LOG;

/**
 * 配置Log工具类的基本配置信息
 **/

public class LogConfig {

    /**
     * 普通日志写在本地文件夹的名称
     **/
    public static final String NORMAL_LOG_NAME = FILE_LOG;
    /**
     * 崩溃日志写在本地文件夹的名称
     **/
    public static final String CRASH_LOG_NAME = Environment.getExternalStorageDirectory().getPath() + File
            .separator + ApkUtil.getPackageName() + File.separator + "crash" + File.separator;

    /**
     * 日志创建时间超过该设置时间，会进行删除，重新创建新的日志文件
     * **/
    public static final int DELETE_INTERVER = 7;

    /**
     * 是否在控制台展示log信息，一般开发时设置，上线时设为false
     **/
    public static final boolean writeLocal = true;

    /**
     * 是否将级别verbose的写入本地文件中
     **/
    public static final boolean writeV = false;
    /**
     * 是否将级别debug的写入本地文件中
     **/
    public static final boolean writeD = true;
    /**
     * 是否将级别info的写入本地文件中
     **/
    public static final boolean writeI = false;
    /**
     * 是否将级别warn的写入本地文件中
     **/
    public static final boolean writeW = false;
    /**
     * 是否将级别error的写入本地文件中,一般这个设置为true
     **/
    public static final boolean writeE = true;

}
