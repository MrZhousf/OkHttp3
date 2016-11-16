package com.okhttplib.bean;

import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;

import java.util.List;

import okhttp3.OkHttpClient;

/**
 * 辅助类信息体
 * @author: zhousf
 */
public class HelperInfo {

    private String LogTAG;//打印日志标识
    private long timeStamp;//时间戳
    private boolean showHttpLog;//是否显示Http请求日志
    private OkHttpClient httpClient;
    private Class<?> requestTag;//请求标识
    private List<ResultInterceptor> resultInterceptors;//请求结果拦截器
    private List<ExceptionInterceptor> exceptionInterceptors;//请求链路异常拦截器
    private String downloadFileDir;//下载文件保存目录


    public String getLogTAG() {
        return LogTAG;
    }

    public void setLogTAG(String logTAG) {
        LogTAG = logTAG;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isShowHttpLog() {
        return showHttpLog;
    }

    public void setShowHttpLog(boolean showHttpLog) {
        this.showHttpLog = showHttpLog;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Class<?> getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(Class<?> requestTag) {
        this.requestTag = requestTag;
    }

    public List<ResultInterceptor> getResultInterceptors() {
        return resultInterceptors;
    }

    public void setResultInterceptors(List<ResultInterceptor> resultInterceptors) {
        this.resultInterceptors = resultInterceptors;
    }

    public List<ExceptionInterceptor> getExceptionInterceptors() {
        return exceptionInterceptors;
    }

    public void setExceptionInterceptors(List<ExceptionInterceptor> exceptionInterceptors) {
        this.exceptionInterceptors = exceptionInterceptors;
    }

    public String getDownloadFileDir() {
        return downloadFileDir;
    }

    public void setDownloadFileDir(String downloadFileDir) {
        this.downloadFileDir = downloadFileDir;
    }

}
