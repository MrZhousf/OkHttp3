package com.okhttplib.bean;

import com.okhttplib.callback.ProgressCallback;

/**
 * 上传/下载进度回调信息体
 */
public class ProgressMessage extends OkMessage{

    public ProgressCallback progressCallback;
    public int percent;
    public long bytesWritten;
    public long contentLength;
    public boolean done;

    public ProgressMessage(int what, ProgressCallback progressCallback, int percent, long bytesWritten, long contentLength, boolean done) {
        this.what = what;
        this.percent = percent;
        this.bytesWritten = bytesWritten;
        this.contentLength = contentLength;
        this.done = done;
        this.progressCallback = progressCallback;
    }



}
