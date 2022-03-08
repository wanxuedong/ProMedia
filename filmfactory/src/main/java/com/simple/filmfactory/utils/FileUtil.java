package com.simple.filmfactory.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.simple.filmfactory.utils.FileCataLog.BASE_NAME;

/**
 * 用于进行一些文件操作
 *
 * @author 万学冬
 */
public class FileUtil {

    /**
     * 默认生成的文件父文件夹
     */
    private static final String FILES_NAME = BASE_NAME;

    /**
     * 获取的时间格式
     */
    private static final String TIME_STYLE = "yyyyMMddHHmmss";

    /**
     * 用于在SD卡中生成并获取一个文件路径,注意会直接生成一个文件，所以后续操作不需要重复创建
     *
     * @param presentPath 希望生成文件的父级文件名称，如果传null，默认为FILES_NAME
     * @param name        需要生成的文件名，如果传null，默认以时间戳命名
     * @param suffix      希望生成的文件后缀，如果传null，则没有文件后缀
     *                    todo 未添加SD等前提检查
     **/
    public static String getPath(String presentPath, String name, String suffix) {
        String productPath = "";
        SimpleDateFormat format = new SimpleDateFormat(TIME_STYLE, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        if (suffix == null || "".equals(suffix)) {
            suffix = "";
        }
        if (name == null || "".equals(name)) {
            name = format.format(date) + suffix;
        } else {
            name += suffix;
        }
        if (presentPath == null || "".equals(presentPath)) {
            presentPath = FILES_NAME;
        }
        String child_name = "";
        if (suffix.endsWith("mp4")) {
            child_name = "video" + File.separator;
        } else if (suffix.endsWith("png")) {
            child_name = "image" + File.separator;
        } else {
            child_name = "audio" + File.separator;
        }
        //Environment.getExternalStorageDirectory().getAbsolutePath()在29的版本中编译是失效
        File presentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + presentPath + File.separator + child_name);
        // 判断父级文件是否已经存在，不存在则创建
        if (!presentFile.exists()) {
            presentFile.mkdirs();
        }
        File childFile = new File(presentFile.getPath(), name);
        //如果存在，就先删除再创建
        if (childFile.exists()) {
            childFile.delete();
        }
        try {
            childFile.createNewFile();
            productPath = childFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return productPath;
    }

}
