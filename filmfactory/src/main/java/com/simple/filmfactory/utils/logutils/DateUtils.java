package com.simple.filmfactory.utils.logutils;


import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */

public final class DateUtils {


    /**
     * 获取俩个日期时间建和，返回天数
     **/
    public static int getTimeDifference(String timeOne, String timeTwo) {
        if (TextUtils.isEmpty(timeOne) || TextUtils.isEmpty(timeTwo)) {
            return 0;
        }
        //格式日期格式，在此我用的是"2018-01-24"这种格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date now = df.parse(timeOne);
            Date date = df.parse(timeTwo);
            if (now != null && date != null) {
                long l = now.getTime() - date.getTime();       //获取时间差
                long day = l / (24 * 60 * 60 * 1000);
                return (int) day;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
