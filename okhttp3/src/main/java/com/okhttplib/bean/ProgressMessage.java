package com.okhttplib.bean;

import com.okhttplib.callback.ProgressCallback;

/**
 * 上传/下载进度回调信息封装实体类
 */
public class ProgressMessage extends OkMessage{

    public ProgressCallback progressCallback;
    public long bytesWritten;
    public long contentLength;
    public boolean done;

    public ProgressMessage(int what, ProgressCallback progressCallback, long bytesWritten, long contentLength, boolean done) {
        this.what = what;
        this.bytesWritten = bytesWritten;
        this.contentLength = contentLength;
        this.done = done;
        this.progressCallback = progressCallback;
    }



}
