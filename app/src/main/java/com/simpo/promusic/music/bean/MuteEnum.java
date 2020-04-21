package com.simpo.promusic.music.bean;

/**
 * @author wanxuedong
 * https://github.com/wanxuedong/PrimaryExercises
 **/
public enum MuteEnum {

    MUTE_RIGHT("RIGHT", 0),
    MUTE_LEFT("LEFT", 1),
    MUTE_CENTER("CENTER", 2);

    private String name;
    private int value;

    MuteEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
