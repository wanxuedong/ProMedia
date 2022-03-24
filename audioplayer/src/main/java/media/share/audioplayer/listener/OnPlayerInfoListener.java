package media.share.audioplayer.listener;

import java.util.HashMap;

/**
 * @author wan
 * 创建日期：2021/12/15
 * 描述：音频头部信息回调
 */
public interface OnPlayerInfoListener {

    void soundInfo(HashMap<String, String> map);

}
