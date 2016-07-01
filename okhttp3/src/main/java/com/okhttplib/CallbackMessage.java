package com.okhttplib;

import android.os.Message;

/**
 * 回调信息实体类
 */
public class CallbackMessage{
    public int what;
    public CallbackOk callback;
    public HttpInfo info;
    public CallbackMessage(int what, CallbackOk callback, HttpInfo info) {
        this.what = what;
        this.callback = callback;
        this.info = info;
    }
    public Message build(){
        Message msg = new Message();
        msg.what = this.what;
        msg.obj = this;
        return msg;
    }
}