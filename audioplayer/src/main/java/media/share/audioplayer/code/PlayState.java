package media.share.audioplayer.code;

/**
 * @author wan
 * 创建日期：2021/12/11
 * 描述：播放状态
 */
public enum PlayState {

    /**
     * 首次加载中，包含播放器初始化，多媒体信息读取
     **/
    ON_LOAD(1, "首次播放加载"),
    /**
     * 首次加载中结束
     **/
    LOAD_OVER(2, "首次播放加载完毕"),
    /**
     * 开始播放
     **/
    ON_START(3, "开始播放"),
    /**
     * 播放过程中加载，主要为网络卡顿，为外部环境导致
     **/
    ON_LOADING(4, "播放中加载数据"),
    /**
     * 播放过程中恢复播放，网络卡顿后恢复，为外部环境导致
     **/
    LOADING_OVER(5, "播放中加载数据恢复播放"),
    /**
     * 播放过程中暂停，为手动调用
     **/
    ON_PAUSE(6, "手动调用播放暂停"),
    /**
     * 播放暂停后恢复，为手动调用
     **/
    ON_RESUME(7, "手动调用恢复播放"),
    /**
     * 播放停止
     **/
    ON_STOP(8, "播放结束"),
    /**
     * 销毁播放资源
     **/
    ON_DESTROY(9, "销毁播放资源");

    /**
     * 播放状态
     **/
    private int type;
    /**
     * 播放状态描述
     **/
    private String describe;

    PlayState(int type, String describe) {
        this.type = type;
        this.describe = describe;
    }

    public int getType() {
        return type;
    }

    public String getDescribe() {
        return describe;
    }

    @Override
    public String toString() {
        return "PlayState{" +
                "type=" + type +
                ", describe='" + describe + '\'' +
                '}';
    }
}
