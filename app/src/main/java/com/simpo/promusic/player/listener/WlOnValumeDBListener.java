package com.simpo.promusic.player.listener;

/**
 * @author wanxuedong
 * 音量大小监听回调,需要注意的是返回的并不是实际播放听到的声音大小，
 * 而是当前设置的音量大小下，播放声音波动大小
 **/
public interface WlOnValumeDBListener {

    void onDbValue(int db);

}
