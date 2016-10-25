package com.okhttplib;

import android.app.Activity;
import android.text.TextUtils;

import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.ProgressCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http请求实体类
 * @author zhousf
 */
public class HttpInfo {

    //**请求参数定义**/
    private String url;//请求地址
    private Map<String,String> params;//请求参数
    private List<UploadFileInfo> uploadFiles;//上传文件参数
    private List<DownloadFileInfo> downloadFiles;//下载文件参数
    private Map<String,String> heads;//请求头参数http head


    //**响应返回参数定义**/
    private int retCode;//返回码
    private String retDetail;//返回结果

    private Class<?> tag;

    public HttpInfo(Builder builder) {
        this.url = builder.url;
        this.params = builder.params;
        this.tag = builder.tag;
        this.uploadFiles = builder.uploadFiles;
        this.downloadFiles = builder.downloadFiles;
        this.heads = builder.heads;
    }

    public static Builder Builder() {
        return new Builder();
    }


    public static final class Builder {

        private String url;
        private Map<String,String> params;
        private List<UploadFileInfo> uploadFiles;
        private List<DownloadFileInfo> downloadFiles;
        private Map<String,String> heads;
        private Class<?> tag;


        public Builder() {
        }

        public HttpInfo build(Object object){
            setTag(object);
            return new HttpInfo(this);
        }

        public HttpInfo build(){
            return new HttpInfo(this);
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        /**
         * 添加接口参数
         * @param params 参数集合
         */
        public Builder addParams(Map<String, String> params) {
            if(null == params)
                return this;
            if(null == this.params){
                this.params = params;
            }else{
                this.params.putAll(params);
            }
            return this;
        }

        /**
         * 添加接口参数
         * @param key 参数名
         * @param value 参数值
         */
        public Builder addParam(String key, String value){
            if(null == this.params)
                this.params = new HashMap<String,String>();
            if(!TextUtils.isEmpty(key)){
                value = value == null ? "" : value;
                this.params.put(key,value);
            }
            return this;
        }

        /**
         * 添加协议头参数
         * @param heads 头参数集合
         */
        public Builder addHeads(Map<String, String> heads) {
            if(null == heads)
                return this;
            if(null == this.heads){
                this.heads = heads;
            }else{
                this.heads.putAll(heads);
            }
            return this;
        }

        /**
         * 添加协议头参数
         * @param key 头参数名
         * @param value 头参数值
         */
        public Builder addHead(String key, String value){
            if(null == this.heads)
                this.heads = new HashMap<String,String>();
            if(!TextUtils.isEmpty(key)){
                value = value == null ? "" : value;
                this.heads.put(key,value);
            }
            return this;
        }

        /**
         * 添加上传文件
         * @param interfaceParamName 接口参数名称
         * @param filePathWithName 上传的文件路径：包含文件名
         */
        public Builder addUploadFile(String interfaceParamName, String filePathWithName) {
            addUploadFile(filePathWithName,interfaceParamName,null);
            return this;
        }

        /**
         * 添加上传文件
         * @param interfaceParamName 接口参数名称
         * @param filePathWithName 上传的文件路径：包含文件名
         * @param progressCallback 上传进度回调接口
         */
        public Builder addUploadFile(String interfaceParamName, String filePathWithName, ProgressCallback progressCallback) {
            if(null == this.uploadFiles){
                this.uploadFiles = new ArrayList<UploadFileInfo>();
            }
            if(!TextUtils.isEmpty(filePathWithName)){
                this.uploadFiles.add(new UploadFileInfo(filePathWithName,interfaceParamName,progressCallback));
            }
            return this;
        }

        /**
         * 添加上传文件
         * @param url 上传文件接口地址
         * @param interfaceParamName 接口参数名称
         * @param filePathWithName 上传的文件路径：包含文件名
         * @param progressCallback 上传进度回调接口
         */
        public Builder addUploadFile(String url, String interfaceParamName, String filePathWithName, ProgressCallback progressCallback) {
            if(null == this.uploadFiles){
                this.uploadFiles = new ArrayList<UploadFileInfo>();
            }
            if(!TextUtils.isEmpty(filePathWithName)){
                this.uploadFiles.add(new UploadFileInfo(url,filePathWithName,interfaceParamName,progressCallback));
            }
            return this;
        }

        public Builder addUploadFiles(List<UploadFileInfo> uploadFiles){
            if(null == uploadFiles)
                return this;
            if(null == this.uploadFiles){
                this.uploadFiles = uploadFiles;
            }else{
                this.uploadFiles.addAll(uploadFiles);
            }
            return this;
        }

        /**
         * 添加下载文件
         * @param url 下载文件的网络地址
         * @param saveFileName 文件保存名称：不包括扩展名
         */
        public Builder addDownloadFile(String url,String saveFileName){
            addDownloadFile(url,null,saveFileName,null);
            return this;
        }

        /**
         * 添加下载文件
         * @param url 下载文件的网络地址
         * @param saveFileName 文件保存名称：不包括扩展名
         * @param progressCallback 下载进度回调接口
         */
        public Builder addDownloadFile(String url,String saveFileName,ProgressCallback progressCallback){
            addDownloadFile(url,null,saveFileName,progressCallback);
            return this;
        }

        /**
         * 添加下载文件
         * @param url 下载文件的网络地址
         * @param saveFileDir 文件保存目录路径：不包括名称
         * @param saveFileName 文件保存名称：不包括扩展名
         * @param progressCallback 下载进度回调接口
         */
        public Builder addDownloadFile(String url,String saveFileDir,String saveFileName,ProgressCallback progressCallback){
            if(null == this.downloadFiles){
                this.downloadFiles = new ArrayList<DownloadFileInfo>();
            }
            if(!TextUtils.isEmpty(url)){
                this.downloadFiles.add(new DownloadFileInfo(url,saveFileDir,saveFileName,progressCallback));
            }
            return this;
        }

        public Builder addDownloadFile(DownloadFileInfo downloadFile){
            if(null == downloadFile)
                return this;
            if(null == this.downloadFiles){
                this.downloadFiles = new ArrayList<DownloadFileInfo>();
            }
            this.downloadFiles.add(downloadFile);
            return this;
        }

        public Builder addDownloadFiles(List<DownloadFileInfo> downloadFiles){
            if(null == downloadFiles)
                return this;
            if(null == this.downloadFiles){
                this.downloadFiles = downloadFiles;
            }else {
                this.downloadFiles.addAll(downloadFiles);
            }
            return this;
        }

        public Builder setTag(Object object) {
            if(object instanceof Activity){
                Activity activity = (Activity) object;
                this.tag = activity.getClass();
            }
            if(object instanceof android.support.v4.app.Fragment){
                android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) object;
                this.tag = fragment.getActivity().getClass();
            }
            if(object instanceof android.app.Fragment){
                android.app.Fragment fragment = (android.app.Fragment) object;
                this.tag = fragment.getActivity().getClass();
            }
            return this;
        }

    }


    //**请求返回常量定义**/
    public final static int SUCCESS = 1;
    final String SUCCESS_Detail = "发送请求成功";
    public final static int NonNetwork = 2;
    final String NonNetwork_Detail = "网络中断";
    public final static int ProtocolException = 3;
    final String ProtocolException_Detail = "请检查协议类型是否正确";
    public final static int NoResult = 4;
    final String NoResult_Detail = "无法获取返回信息(服务器内部错误)";
    public final static int CheckURL = 5;
    final String CheckURL_Detail = "请检查请求地址是否正确";
    public final static int CheckNet = 6;
    final String CheckNet_Detail = "请检查网络连接是否正常";
    public final static int ConnectionTimeOut = 7;
    final String ConnectionTimeOut_Detail = "连接超时";
    public final static int WriteAndReadTimeOut = 8;
    final String WriteAndReadTimeOut_Detail = "读写超时";
    public final static int ConnectionInterruption = 9;
    final String ConnectionInterruption_Detail = "连接中断";
    public final static int NetworkOnMainThreadException = 10;
    final String NetworkOnMainThreadException_Detail = "不允许在UI线程中进行网络操作";
    public final static int Message = 11;
    final String Message_Detail = "";

    public HttpInfo packInfo(int retCode, String retDetail){
        this.retCode = retCode;
        switch (retCode){
            case NonNetwork:
                this.retDetail = NonNetwork_Detail;
                break;
            case SUCCESS:
                this.retDetail = SUCCESS_Detail;
                break;
            case ProtocolException:
                this.retDetail = ProtocolException_Detail;
                break;
            case NoResult:
                this.retDetail = NoResult_Detail;
                break;
            case CheckURL:
                this.retDetail = CheckURL_Detail;
                break;
            case CheckNet:
                this.retDetail = CheckNet_Detail;
                break;
            case ConnectionTimeOut:
                this.retDetail = ConnectionTimeOut_Detail;
                break;
            case WriteAndReadTimeOut:
                this.retDetail = WriteAndReadTimeOut_Detail;
                break;
            case ConnectionInterruption:
                this.retDetail = ConnectionInterruption_Detail;
                break;
            case NetworkOnMainThreadException:
                this.retDetail = NetworkOnMainThreadException_Detail;
                break;
            case Message:
                this.retDetail = Message_Detail;
                break;
        }
        if(!TextUtils.isEmpty(retDetail)){
            this.retDetail = retDetail;
        }
        return this;
    }

    public int getRetCode() {
        return retCode;
    }

    public boolean isSuccessful(){
        return this.retCode == SUCCESS;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRetDetail() {
        return retDetail;
    }

    public void setRetDetail(String retDetail) {
        this.retDetail = retDetail;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Class<?> getTag() {
        return tag;
    }

    public List<UploadFileInfo> getUploadFiles() {
        return uploadFiles;
    }

    public List<DownloadFileInfo> getDownloadFiles() {
        return downloadFiles;
    }

    public Map<String, String> getHeads() {
        return heads;
    }
}
