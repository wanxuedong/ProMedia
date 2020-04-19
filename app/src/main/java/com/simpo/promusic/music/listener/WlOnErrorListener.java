package com.simpo.promusic.music.listener;

/**
 * @author wanxuedong
 * 播放错误提示回调
 **/
public interface WlOnErrorListener {

    void onError(int code, String msg);

}
