package media.share.audioplayer.bean;

public enum MuteEnum {

    MUTE_RIGHT("右声道", 0),
    MUTE_LEFT("左声道", 1),
    MUTE_CENTER("立体声", 2);

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
