package com.example.lql.updateappdemo.message;

/**
 * 类描述：
 * 作  者：Admin or 李小米
 * 时  间：2017/10/27
 * 修改备注：
 */
public class EventMessage {
    public final static int Exitapp = 0x11;
    public final static int CheckApp = 0x12;
    int MessageType;
    boolean DownLoading = false;

    public EventMessage(int messageType, boolean downLoading) {
        MessageType = messageType;
        DownLoading = downLoading;
    }

    public EventMessage(int messageType) {
        MessageType = messageType;
    }

    public int getMessageType() {
        return MessageType;
    }

    public void setMessageType(int messageType) {
        MessageType = messageType;
    }

    public boolean isDownLoading() {
        return DownLoading;
    }

    public void setDownLoading(boolean downLoading) {
        DownLoading = downLoading;
    }
}
