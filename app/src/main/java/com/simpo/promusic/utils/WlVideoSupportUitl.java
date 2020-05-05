package com.simpo.promusic.utils;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于获取当前设备支持的硬解码器列表，以此判断是否支持对某种格式视频的硬解码
 **/
public class WlVideoSupportUitl {

    private static Map<String, String> codecMap = new HashMap<>();

    static {
        codecMap.put("h264", "video/avc");
    }

    public static String findVideoCodecName(String ffcodename) {
        if (codecMap.containsKey(ffcodename)) {
            return codecMap.get(ffcodename);
        }
        return "";
    }

    /**
     * 判断是否支持传入格式的硬解码
     *
     * @param ffcodecname 传入视频的编解码器实现的名称
     * @return
     */
    public static boolean isSupportCodec(String ffcodecname) {
        Log.d("isSupportCodec1", ffcodecname);
        boolean supportvideo = false;
        // 获取所有支持编解码器数量
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            // 编解码器相关性信息存储在MediaCodecInfo中
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            // 判断是否为编码器
            if (!codecInfo.isEncoder()) {
                continue;
            }
            // 获取编码器支持的MIME类型，并进行匹配
            String[] tyeps = codecInfo.getSupportedTypes();
            for (int j = 0; j < tyeps.length; j++) {
                Log.d("isSupportCodec2", tyeps[j]);
                if (tyeps[j].equals(findVideoCodecName(ffcodecname))) {
                    supportvideo = true;
                    break;
                }
            }
            if (supportvideo) {
                break;
            }
        }
        return supportvideo;
    }
}
