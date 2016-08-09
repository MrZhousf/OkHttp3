package com.okhttplib.callback;

import com.okhttplib.HttpInfo;

/**
 * 进度回调抽象类
 */
public abstract class ProgressCallbackAbs {

    public abstract void onResponse(String filePath, HttpInfo info);

    public abstract void onProgressAsync(int percent, long bytesWritten, long contentLength, boolean done);

    public abstract void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done);

}
