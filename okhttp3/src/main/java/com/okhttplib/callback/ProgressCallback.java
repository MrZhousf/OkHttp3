package com.okhttplib.callback;

import com.okhttplib.HttpInfo;

/**
 * 进度回调
 */
public class ProgressCallback extends ProgressCallbackAbs {

    /**
     * UI线程
     * @param filePath 长传文件途径
     * @param info 上传结果信息类
     */
    @Override
    public void onResponse(String filePath, HttpInfo info) {

    }

    /**
     * 非UI线程：除了更新ProgressBar进度外进行其他UI操作
     * @param bytesWritten 已经写入的字节数
     * @param contentLength 文件总长度
     * @param done 是否完成即：bytesWritten==contentLength
     */
    @Override
    public void onProgressAsync(long bytesWritten, long contentLength, boolean done) {

    }

    /**
     * UI线程：可以直接操作UI
     * @param bytesWritten 已经写入的字节数
     * @param contentLength 文件总长度
     * @param done 是否完成即：bytesWritten==contentLength
     */
    @Override
    public void onProgressMain(long bytesWritten, long contentLength, boolean done) {

    }





}
