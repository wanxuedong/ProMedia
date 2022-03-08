package com.simple.filmfactory.utils;

import android.os.Environment;

import java.io.File;

/**
 * 文件统一目录类
 **/
public class FileCataLog {

    public static final String BASE_NAME = "AAFileFactory";

    public static final String BASE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + BASE_NAME + File.separator;

    public static final String FILE_SAVE = BASE_FILE + "temp" + File.separator;

    public static final String FILE_LOG = BASE_FILE + "log" + File.separator;

}
