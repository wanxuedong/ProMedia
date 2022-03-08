package com.simple.filmfactory.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 获取应用部分信息的工具类
 **/

public class ApkUtil extends BaseUtil {

    /**
     * 获取当前应用包名
     */
    public static synchronized String getPackageName() {
        checkInit();
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
