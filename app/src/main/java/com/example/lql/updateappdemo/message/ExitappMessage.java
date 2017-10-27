package com.example.lql.updateappdemo.message;

/**
 * 类描述：
 * 作  者：Admin or 李小米
 * 时  间：2017/10/27
 * 修改备注：
 */
public class ExitappMessage {
    String Message;

    public ExitappMessage(String message) {
        Message = message;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
