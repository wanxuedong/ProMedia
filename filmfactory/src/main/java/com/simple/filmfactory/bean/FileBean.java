package com.simple.filmfactory.bean;

import com.simple.filmfactory.utils.DateTimeUtils;

import io.realm.RealmObject;

/**
 * 本地已加入的文件部分属性
 **/
public class FileBean extends RealmObject implements Comparable<FileBean> {

    /**
     * 文件id,时间戳代替
     **/
    private long fileId;

    /**
     * 添加到数据库中的时间
     **/
    private String addTime;

    /**
     * 文件类型，1视频，2音频
     **/
    private String fileType;

    /**
     * 文件名称
     **/
    private String name = "";

    /**
     * 文件的重命名
     **/
    private String nickName = "";

    /**
     * 文件的系统路径
     **/
    private String filePath;

    /**
     * 文件大小
     **/
    private String fileSize = "";

    /**
     * 文件时长
     **/
    private int playTime;

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public static FileBean newInstance() {
        FileBean bean = new FileBean();
        bean.setAddTime(DateTimeUtils.convertTimestamp2Date(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        bean.setFileId(System.currentTimeMillis());
        return bean;
    }

    @Override
    public int compareTo(FileBean o) {
        return -(int) (this.fileId - o.fileId);
    }
}
