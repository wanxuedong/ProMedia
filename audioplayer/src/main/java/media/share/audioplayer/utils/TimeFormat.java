package media.share.audioplayer.utils;

/**
 * @author wan
 * 创建日期：2021/12/13
 * 描述：时间格式化
 */
public class TimeFormat {

    /**
     * 秒格式化为：小时-分钟-秒，如：01:02:03,如果没有小时，则小时不展示
     *
     * @param _second 秒
     **/
    public static String format(int _second) {
        int hour;
        int minute;
        int second;
        second = _second % 60;
        minute = _second / 60 % 60;
        hour = _second / 60 / 60;
        String strSecond;
        if ((second + "").length() == 1) {
            strSecond = "0" + second;
        } else {
            strSecond = second + "";
        }
        String strMinute = minute + "";
        if (hour > 1) {
            if ((minute + "").length() == 1) {
                strMinute = "0" + minute;
            }
            return hour + ":" + strMinute + ":" + strSecond;
        } else {
            return strMinute + ":" + strSecond;
        }
    }

}
