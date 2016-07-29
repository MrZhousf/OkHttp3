package com.okhttplib.bean;

import com.okhttplib.HttpInfo;
import com.okhttplib.callback.ProgressCallback;

/**
 * 上传响应回调信息封装实体类
 */
public class UploadMessage extends OkMessage{

    public String filePath;
    public HttpInfo info;
    public ProgressCallback progressCallback;

    public UploadMessage(int what, String filePath, HttpInfo info, ProgressCallback progressCallback) {
        this.what = what;
        this.filePath = filePath;
        this.info = info;
        this.progressCallback = progressCallback;
    }
}