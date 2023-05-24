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
     * 水印位置
     * 1:左上,2:左下,3:右上,4右下
     * **/
    private int waterPosition = 4;

    /**
     * 水印大小
     * 1:大,2:中,3:小
     * **/
    private int waterSize = 3;

    /**
     * 水印内容
     * **/
    private String waterString = "";

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

    public void setWaterString(String waterString) {
        this.waterString = waterString;
    }

    public String getWaterString() {
        return waterString;
    }

    public void setWaterPosition(int waterPosition) {
        this.waterPosition = waterPosition;
    }

    public int getWaterPosition() {
        return waterPosition;
    }

    public void setWaterSize(int waterSize) {
        this.waterSize = waterSize;
    }

    public int getWaterSize() {
        switch (waterSize){
            case 1:
                return 300;
            case 2:
                return 100;
            case 3:
                return 40;
        }
        return 30;
    }


}
