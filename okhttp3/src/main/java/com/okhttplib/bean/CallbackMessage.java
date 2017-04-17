package com.okhttplib.bean;

import com.okhttplib.HttpInfo;
import com.okhttplib.callback.BaseCallback;

import okhttp3.Call;

/**
 * 响应回调信息体
 * @author zhousf
 */
public class CallbackMessage extends OkMessage{

    public BaseCallback callback;
    public HttpInfo info;
    public Call call;


    public CallbackMessage(int what, BaseCallback callback, HttpInfo info,
                           String requestTag, Call call) {
        this.what = what;
        this.callback = callback;
        this.info = info;
        super.requestTag = requestTag;
        this.call = call;
    }


}