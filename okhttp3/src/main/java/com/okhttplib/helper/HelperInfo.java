package com.okhttplib.helper;

import com.okhttplib.OkHttpUtil;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;

import java.util.List;

import okhttp3.OkHttpClient;

/**
 * 业务类信息体
 * @author zhousf
 */
public class HelperInfo {

    private String LogTAG;//打印日志标识
    private long timeStamp;//时间戳
    private boolean showHttpLog;//是否显示Http请求日志
    private OkHttpUtil okHttpUtil;
    private boolean isDefault;//是否默认请求
    private OkHttpClient.Builder clientBuilder;
    private String requestTag;//请求标识
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

    public String getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(String requestTag) {
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

    public OkHttpClient.Builder getClientBuilder() {
        return clientBuilder;
    }

    public void setClientBuilder(OkHttpClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public OkHttpUtil getOkHttpUtil() {
        return okHttpUtil;
    }

    public void setOkHttpUtil(OkHttpUtil okHttpUtil) {
        this.okHttpUtil = okHttpUtil;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
