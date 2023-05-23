package com.simple.filmfactory.utils;

/**
 * @author wan
 * 创建日期：2023/05/23
 * 描述：水印设置类
 */
public class WaterMarkSetting {

    private static WaterMarkSetting instant;


    /**
     * 是否开启水印
     * **/
    private boolean waterMark = false;

    /**
     * 是否开启水印
     * @param waterMark true：开启，false：关闭
     * **/
    public void setWaterMark(boolean waterMark) {
        this.waterMark = waterMark;
    }

    public boolean isWaterMark() {
        return waterMark;
    }

    private WaterMarkSetting(){

    }

    public static WaterMarkSetting getInstant() {
        if (instant == null){
            synchronized (WaterMarkSetting.class){
                instant = new WaterMarkSetting();
            }
        }
        return instant;
    }



}
