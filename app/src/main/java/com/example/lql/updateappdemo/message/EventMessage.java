package com.example.lql.updateappdemo.message;

import java.io.File;

/**
 * 类描述：
 * 作  者：Admin or 李小米
 * 时  间：2017/10/27
 * 修改备注：
 */
public class EventMessage {
    public final static int EXIT_APP = 0x11;
    public final static int CHECK_APP = 0x12;
    /**
     * 调用系统下载，下载完成
     */
    public static final int DOWNLOAD_FINISH = 0x515;
    /**
     * 调用系统下载，下载失败
     */
    public static final int DOWNLOAD_FAIL = 0x516;
    /**
     * 调用系统下载，开始下载
     */
    public static final int DOWNLOAD_START = 0x517;

    /**
     * int 类型消息
     */
    private int messageType;
    /**
     * String 类型消息
     */
    private String messageString;
    /**
     * long 类型消息
     */
    private long messageLong;
    /**
     * boolean 类型消息
     */
    private boolean downLoading = false;

    /**
     * 下载好的文件
     */
    private File DownLoadFile;

    public EventMessage(int messageType, boolean downLoading) {
        this.messageType = messageType;
        this.downLoading = downLoading;
    }

    public File getDownLoadFile() {
        return DownLoadFile;
    }

    public void setDownLoadFile(File downLoadFile) {
        DownLoadFile = downLoadFile;
    }

    public long getMessageLong() {
        return messageLong;
    }

    public void setMessageLong(long messageLong) {
        this.messageLong = messageLong;
    }

    public String getMessageString() {
        return messageString;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }

    public EventMessage(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isDownLoading() {
        return downLoading;
    }

    public void setDownLoading(boolean downLoading) {
        this.downLoading = downLoading;
    }
}
