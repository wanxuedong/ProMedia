package media.share.audioplayer.bean;

import media.share.audioplayer.utils.TimeFormat;

/**
 * @author wan
 * 创建日期：2021/12/11
 * 描述：当前播放时间进度
 */
public class PlayTime {

    /**
     * 当前播放进度时间
     **/
    public int currentTime;
    /**
     * 整个音频全部播放时间
     **/
    public int totalTime;

    /**
     * 返回时间进度格式化
     *
     * @return 秒格式化为：小时-分钟-秒，如：2:10 / 1:32:43,如果没有小时，则小时不展示
     **/
    @Override
    public String toString() {
        return TimeFormat.format(currentTime) + "/" + TimeFormat.format(totalTime);
    }
}
