package com.okhttplib.callback;

import com.okhttplib.HttpInfo;

/**
 * 进度回调
 * @author zhousf
 */
public class ProgressCallback extends ProgressCallbackAbs {

    /**
     * 异步UI线程：返回请求结果
     * @param filePath 文件网络路径
     * @param info 结果信息类
     */
    @Override
    public void onResponseMain(String filePath, HttpInfo info) {

    }

    /**
     * 同步非UI线程：返回请求结果
     * @param filePath 文件网络路径
     * @param info 结果信息类
     */
    @Override
    public void onResponseSync(String filePath, HttpInfo info) {

    }

    /**
     * 非UI线程：除了更新ProgressBar进度外不要进行其他UI操作
     * @param percent 已经写入的百分比
     * @param bytesWritten 已经写入的字节数
     * @param contentLength 文件总长度
     * @param done 是否完成即：bytesWritten==contentLength
     */
    @Override
    public void onProgressAsync(int percent, long bytesWritten, long contentLength, boolean done) {

    }

    /**
     * UI线程：可以直接操作UI
     * @param percent 已经写入的百分比
     * @param bytesWritten 已经写入的字节数
     * @param contentLength 文件总长度
     * @param done 是否完成即：bytesWritten==contentLength
     */
    @Override
    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {

    }


}
