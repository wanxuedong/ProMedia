package media.share.audioplayer.listener;

import media.share.audioplayer.bean.PlayTime;

/**
 * @author wan
 * 创建日期：2021/12/11
 * 描述：播放进度回调
 */
public interface OnPlayerProgressListener {

    void progress(PlayTime playTime);

}
