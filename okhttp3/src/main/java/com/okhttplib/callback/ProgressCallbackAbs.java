package com.okhttplib.callback;

import com.okhttplib.HttpInfo;

/**
 * 进度回调抽象类
 */
public abstract class ProgressCallbackAbs {

    public abstract void onResponse(String filePath, HttpInfo info);

    public abstract void onProgressAsync(long bytesWritten, long contentLength, boolean done);

    public abstract void onProgressMain(long bytesWritten, long contentLength, boolean done);

}
