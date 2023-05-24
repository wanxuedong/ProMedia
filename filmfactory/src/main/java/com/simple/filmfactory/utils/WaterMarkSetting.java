package com.simple.filmfactory.utils;

import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;

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
     * 水印颜色
     * 1:黑,2:红,3:绿，4:白
     * **/
    private int waterColor = 4;

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
        if (TextUtils.isEmpty(waterString)){
            return Build.DEVICE;
        }
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

    /**
     * 最终影响水印大小的尺寸，照目前的设计，水印最大长度不会超过半个屏幕的百分之90
     * 每个字符占据的大小,最多可以有10个字符，但是总大小不能超过0.9f,因为边距还有0.1f
     * **/
    public float getWaterSize() {
        switch (waterSize){
            case 1:
                return 0.09f;
            case 2:
                return 0.08f;
            case 3:
                return 0.07f;
        }
        return 0.06f;
    }

    public void setWaterColor(int waterColor) {
        this.waterColor = waterColor;
    }

    public String getWaterColor() {
        switch (waterColor){
            case 1:
                //黑
                return "#000000";
            case 2:
                //红
                return "#ff0000";
            case 3:
                //绿
                return "#00ff66";
            case 4:
                //百
                return "#ffffff";
        }
        return "#ffffff";
    }

    public int getWaterIntColor(){
        switch (waterColor){
            case 1:
                //黑
                return Color.BLACK;
            case 2:
                //红
                return Color.RED;
            case 3:
                //绿
                return Color.GREEN;
            case 4:
                //百
                return Color.WHITE;
        }
        return Color.WHITE;
    }

    public int getWaterGravity(){
        switch (waterPosition){
            case 1:
                return Gravity.LEFT | Gravity.TOP;
            case 2:
                return Gravity.LEFT | Gravity.BOTTOM;
            case 3:
                return Gravity.RIGHT | Gravity.TOP;
            case 4:
                return Gravity.RIGHT | Gravity.BOTTOM;
        }
        return Gravity.RIGHT | Gravity.BOTTOM;
    }

}
