package com.okhttplib;

import com.okhttplib.callback.BaseCallback;
import com.okhttplib.callback.ProgressCallback;

import okhttp3.OkHttpClient;

/**
 * 网络请求工具接口
 * @author zhousf
 */
public interface OkHttpUtilInterface {

    /**
     * 同步请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doSync(HttpInfo info);

    /**
     * 异步请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doAsync(HttpInfo info, BaseCallback callback);

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doPostSync(HttpInfo info);

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @param callback 进度回调接口
     * @return HttpInfo
     */
    HttpInfo doPostSync(HttpInfo info, ProgressCallback callback);

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doPostAsync(HttpInfo info, BaseCallback callback);

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    void doPostAsync(HttpInfo info, ProgressCallback callback);

    /**
     * 同步Get请求
     * @param info 请求信息体
     */
    HttpInfo doGetSync(HttpInfo info);

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doGetAsync(HttpInfo info, BaseCallback callback);

    /**
     * 异步上传文件
     * @param info 请求信息体
     */
    void doUploadFileAsync(final HttpInfo info);

    /**
     * 批量异步上传文件
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    void doUploadFileAsync(final HttpInfo info, ProgressCallback callback);

    /**
     * 同步上传文件
     * @param info 请求信息体
     */
    void doUploadFileSync(final HttpInfo info);

    /**
     * 批量同步上传文件
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    void doUploadFileSync(final HttpInfo info, ProgressCallback callback);

    /**
     * 异步下载文件
     * @param info 请求信息体
     */
    void doDownloadFileAsync(final HttpInfo info);


    /**
     * 同步下载文件
     * @param info 请求信息体
     */
    void doDownloadFileSync(final HttpInfo info);

    /**
     * 同步Delete请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doDeleteSync(HttpInfo info);

    /**
     * 异步Delete请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doDeleteAsync(HttpInfo info, BaseCallback callback);

    /**
     * 同步Put请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doPutSync(HttpInfo info);

    /**
     * 异步PUT请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doPutAsync(HttpInfo info, BaseCallback callback);

    /**
     * 取消请求
     * @param requestTag 请求标识
     */
    void cancelRequest(Object requestTag);


    /**
     * 获取默认的HttpClient
     */
    OkHttpClient getDefaultClient();

    /**
     * 清理缓存：只清理网络请求的缓存，不清理下载文件
     */
    boolean deleteCache();

}
