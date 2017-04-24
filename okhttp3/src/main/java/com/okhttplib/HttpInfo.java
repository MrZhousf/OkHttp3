package com.okhttplib;

import android.text.TextUtils;

import com.okhttplib.annotation.Encoding;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.ProgressCallback;

import java.io.File;
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
    private String url;
    private Map<String,String> params;
    private byte[] paramBytes;
    private File paramFile;
    private List<UploadFileInfo> uploadFiles;
    private List<DownloadFileInfo> downloadFiles;
    private Map<String,String> heads;
    private @Encoding String responseEncoding ;//服务器响应编码

    //**响应返回参数定义**/
    private int retCode;//返回码
    private String retDetail;//返回结果
    private int netCode;//网络返回码

    public HttpInfo(Builder builder) {
        this.url = builder.url;
        this.params = builder.params;
        this.paramBytes = builder.paramBytes;
        this.paramFile = builder.paramFile;
        this.uploadFiles = builder.uploadFiles;
        this.downloadFiles = builder.downloadFiles;
        this.heads = builder.heads;
        this.responseEncoding = builder.responseEncoding;
    }

    public static Builder Builder() {
        return new Builder();
    }


    public static final class Builder {

        private String url;//请求地址
        private Map<String,String> params;//请求参数
        private byte[] paramBytes;//请求参数（字节数组）
        private File paramFile;//请求参数（文件）
        private List<UploadFileInfo> uploadFiles;//上传文件参数
        private List<DownloadFileInfo> downloadFiles;//下载文件参数
        private Map<String,String> heads;//请求头参数http head
        private @Encoding String responseEncoding ;//服务器响应编码


        public Builder() {
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
                this.params = new HashMap<>();
            if(!TextUtils.isEmpty(key)){
                value = value == null ? "" : value;
                this.params.put(key,value);
            }
            return this;
        }

        /**
         * 添加接口参数（字节数组/二进制流）
         * 请采用POST请求方式
         * MediaType.parse("application/octet-stream")
         * @param paramBytes 参数值
         */
        public Builder addParamBytes(byte[] paramBytes){
            this.paramBytes = paramBytes;
            return this;
        }

        /**
         * 添加接口参数（字节数组/二进制流）
         * 请采用POST请求方式
         * MediaType.parse("application/octet-stream")
         * @param paramBytes 参数值
         */
        public Builder addParamBytes(String paramBytes){
            if(TextUtils.isEmpty(paramBytes)){
                throw new IllegalArgumentException("paramBytes must not be null");
            }
            this.paramBytes = paramBytes.getBytes();
            return this;
        }

        /**
         * 添加接口参数（文件）
         * 请采用POST请求方式
         * 该方法可上传文件，建议上传文件采用标准方法：addUploadFile
         * MediaType.parse("text/x-markdown; charset=utf-8")
         * @param file 上传文件
         */
        public Builder addParamFile(File file){
            if(file == null || !file.exists()){
                throw new IllegalArgumentException("file must not be null");
            }
            this.paramFile = file;
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
                this.heads = new HashMap<>();
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
            addUploadFile(interfaceParamName,filePathWithName,null);
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
                this.uploadFiles = new ArrayList<>();
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
                this.downloadFiles = new ArrayList<>();
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
                this.downloadFiles = new ArrayList<>();
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

        /**
         * 设置服务器响应编码格式（默认：UTF-8）
         * @param responseEncoding 编码格式
         */
        public Builder setResponseEncoding(@Encoding String responseEncoding) {
            this.responseEncoding = responseEncoding;
            return this;
        }
    }


    //**请求返回常量定义**/
    public final static int SUCCESS = 1;
    public final static int NonNetwork = 2;
    public final static int ProtocolException = 3;
    public final static int NoResult = 4;
    public final static int CheckURL = 5;
    public final static int CheckNet = 6;
    public final static int ConnectionTimeOut = 7;
    public final static int WriteAndReadTimeOut = 8;
    public final static int ConnectionInterruption = 9;
    public final static int NetworkOnMainThreadException = 10;
    public final static int Message = 11;
    public final static int GatewayTimeOut = 12;
    public final static int GatewayBad = 13;
    public final static int ServerNotFound = 14;


    public HttpInfo packInfo(int netCode,int retCode, String retDetail){
        this.netCode = netCode;
        this.retCode = retCode;
        switch (retCode){
            case NonNetwork:
                this.retDetail = "网络中断";
                break;
            case SUCCESS:
                this.retDetail = "发送请求成功";
                break;
            case ProtocolException:
                this.retDetail = "请检查协议类型是否正确";
                break;
            case NoResult:
                this.retDetail = "无法获取返回信息(服务器内部错误)";
                break;
            case CheckURL:
                this.retDetail = "请检查请求地址是否正确";
                break;
            case CheckNet:
                this.retDetail = "请检查网络连接是否正常";
                break;
            case ConnectionTimeOut:
                this.retDetail = "连接超时";
                break;
            case WriteAndReadTimeOut:
                this.retDetail = "读写超时";
                break;
            case ConnectionInterruption:
                this.retDetail = "连接中断";
                break;
            case NetworkOnMainThreadException:
                this.retDetail = "不允许在UI线程中进行网络操作";
                break;
            case Message:
                this.retDetail = "";
                break;
            case GatewayTimeOut:
                this.retDetail = "网关超时，请检查请求链路或重试";
                break;
            case GatewayBad:
                this.retDetail = "错误的网关，请检查请求链路";
                break;
            case ServerNotFound:
                this.retDetail = "服务器找不到请求页面(服务器内部错误)";
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

    public byte[] getParamBytes() {
        return paramBytes;
    }

    public File getParamFile() {
        return paramFile;
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

    public int getNetCode() {
        return netCode;
    }

    public String getResponseEncoding() {
        return responseEncoding;
    }

}
