package media.share.audioplayer.listener;

/**
 * @author wan
 * 创建日期：2021/12/10
 * 描述：播放错误回调
 */
public interface OnPlayerErrorListener {

    void onError(int code, String message);

}
