package com.okhttplib;

import com.okhttplib.callback.CallbackOk;

/**
 *
 * 网络请求工具抽象类
 * @author: zhousf
 */

public abstract class OkHttpUtilAbs {

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    public abstract HttpInfo doPostSync(HttpInfo info);

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    public abstract void doPostAsync(HttpInfo info, CallbackOk callback);

    /**
     * 同步Get请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    public abstract HttpInfo doGetSync(HttpInfo info);

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    public abstract void doGetAsync(HttpInfo info, CallbackOk callback);

    /**
     * 异步上传文件
     * @param info 请求信息体
     */
    public abstract void doUploadFileAsync(final HttpInfo info);

    /**
     * 同步上传文件
     * @param info 请求信息体
     */
    public abstract void doUploadFileSync(final HttpInfo info);

    /**
     * 异步下载文件
     * @param info 请求信息体
     */
    public abstract void doDownloadFileAsync(final HttpInfo info);


    /**
     * 同步下载文件
     * @param info 请求信息体
     */
    public abstract void doDownloadFileSync(final HttpInfo info);


}
