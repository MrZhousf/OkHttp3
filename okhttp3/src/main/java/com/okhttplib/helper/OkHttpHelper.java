package com.okhttplib.helper;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.RequestMethod;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.CallbackOk;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * OkHttpUtil业务类：负责处理所有业务
 * @author zhousf
 */
public class OkHttpHelper {

    //** 请求参数定义**/
    private HttpInfo httpInfo;

    //** 系统辅助参数定义**/
    private HttpHelper httpHelper;
    private DownUpLoadHelper downUpLoadHelper;
    private DownloadFileInfo downloadFileInfo;
    private UploadFileInfo uploadFileInfo;
    private OkHttpClient.Builder clientBuilder;
    private @RequestMethod  int requestMethod;
    private CallbackOk callback;
    private Request request;
    private OkHttpClient httpClient;


    private OkHttpHelper(Builder builder) {
        httpInfo = builder.httpInfo;
        downloadFileInfo = builder.downloadFileInfo;
        uploadFileInfo = builder.uploadFileInfo;
        clientBuilder = builder.clientBuilder;
        requestMethod = builder.requestMethod;
        callback = builder.callback;
        httpHelper = new HttpHelper(builder.helperInfo);
        if(null != downloadFileInfo || null != uploadFileInfo)
            downUpLoadHelper = new DownUpLoadHelper(builder.helperInfo);
    }

    public HttpInfo doRequestSync(){
        return httpHelper.doRequestSync(this);
    }

    public void doRequestAsync(){
        httpHelper.doRequestAsync(this);
    }

    public void downloadFile(){
        downUpLoadHelper.downloadFile(this);
    }

    public void uploadFile(){
        downUpLoadHelper.uploadFile(this);
    }


    public static Builder Builder(){
        return new Builder();
    }

    public static final class Builder {
        private HttpInfo httpInfo;
        private HelperInfo helperInfo;
        private DownloadFileInfo downloadFileInfo;
        private UploadFileInfo uploadFileInfo;
        private OkHttpClient.Builder clientBuilder;
        private @RequestMethod  int requestMethod;
        private CallbackOk callback;

        public Builder() {
        }

        public OkHttpHelper build(){
            return new OkHttpHelper(this);
        }

        public Builder httpInfo(HttpInfo httpInfo){
            this.httpInfo = httpInfo;
            return this;
        }

        public Builder helperInfo(HelperInfo helperInfo){
            this.helperInfo = helperInfo;
            return this;
        }

        public Builder downloadFileInfo(DownloadFileInfo downloadFileInfo){
            this.downloadFileInfo = downloadFileInfo;
            return this;
        }

        public Builder uploadFileInfo(UploadFileInfo uploadFileInfo){
            this.uploadFileInfo = uploadFileInfo;
            return this;
        }

        public Builder clientBuilder(OkHttpClient.Builder clientBuilder){
            this.clientBuilder = clientBuilder;
            return this;
        }

        public Builder requestMethod(@RequestMethod  int requestMethod){
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder callbackOk(CallbackOk callback){
            this.callback = callback;
            return this;
        }

    }

    HttpInfo getHttpInfo() {
        return httpInfo;
    }

    HttpHelper getHttpHelper() {
        return httpHelper;
    }

    DownUpLoadHelper getDownUpLoadHelper() {
        return downUpLoadHelper;
    }

    DownloadFileInfo getDownloadFileInfo() {
        return downloadFileInfo;
    }

    UploadFileInfo getUploadFileInfo() {
        return uploadFileInfo;
    }

    OkHttpClient.Builder getClientBuilder() {
        return clientBuilder;
    }

    @RequestMethod int getRequestMethod() {
        return requestMethod;
    }

    CallbackOk getCallback() {
        return callback;
    }

    Request getRequest() {
        return request;
    }

    void setRequest(Request request) {
        this.request = request;
    }

    OkHttpClient getHttpClient() {
        return httpClient;
    }

    void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
