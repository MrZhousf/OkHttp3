package com.okhttplib.bean;

import com.okhttplib.HttpInfo;
import com.okhttplib.callback.CallbackOk;

/**
 * 响应回调信息封装实体类
 */
public class CallbackMessage extends OkMessage{

    public CallbackOk callback;
    public HttpInfo info;

    public CallbackMessage(int what, CallbackOk callback, HttpInfo info) {
        this.what = what;
        this.callback = callback;
        this.info = info;
    }


}