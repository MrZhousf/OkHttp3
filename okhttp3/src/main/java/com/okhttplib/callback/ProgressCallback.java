package com.okhttplib.callback;


public interface ProgressCallback {

    /**
     * 非UI线程：除了更新ProgressBar进度外不要操作UI
     * @param bytesWritten 已经写入的字节数
     * @param contentLength 文件总长度
     * @param done 是否完成即：bytesWritten==contentLength
     */
    void onProgress(long bytesWritten, long contentLength, boolean done);



}
