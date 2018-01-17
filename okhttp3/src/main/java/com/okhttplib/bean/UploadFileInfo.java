package com.okhttplib.bean;


import com.okhttplib.annotation.ContentType;
import com.okhttplib.callback.ProgressCallback;

import java.io.File;

/**
 * 上传文件信息体
 * @author zhousf
 */
public class UploadFileInfo {

    //上传文件接口地址
    private String url;
    //上传的文件路径：包含文件名
    private String filePathWithName;
    //上传的文件
    private byte[] fileByte;
    //上传的文件
    private File file;
    //媒体类型
    private @ContentType String contentType;
    //接口参数名称
    private String interfaceParamName;
    //上传进度回调接口
    private ProgressCallback progressCallback;

    public UploadFileInfo() {
    }

    public UploadFileInfo setInterfaceParamName(String interfaceParamName){
        this.interfaceParamName = interfaceParamName;
        return this;
    }

    public UploadFileInfo setProgressCallback(ProgressCallback progressCallback){
        this.progressCallback = progressCallback;
        return this;
    }


    public UploadFileInfo setContentType(@ContentType String contentType){
        this.contentType = contentType;
        return this;
    }

    public UploadFileInfo setFile(byte[] fileByte){
        this.fileByte = fileByte;
        return this;
    }

    public UploadFileInfo setFile(File file){
        this.file = file;
        return this;
    }

    public UploadFileInfo setFilePathWithName(String filePathWithName){
        this.filePathWithName = filePathWithName;
        return this;
    }
    public UploadFileInfo setUrl(String url){
        this.url = url;
        return this;
    }


    public UploadFileInfo(String filePathWithName, String interfaceParamName, ProgressCallback progressCallback) {
        this.filePathWithName = filePathWithName;
        this.interfaceParamName = interfaceParamName;
        this.progressCallback = progressCallback;
    }

    public UploadFileInfo(String url, String filePathWithName, String interfaceParamName, ProgressCallback progressCallback) {
        this.url = url;
        this.filePathWithName = filePathWithName;
        this.interfaceParamName = interfaceParamName;
        this.progressCallback = progressCallback;
    }


    public String getUrl() {
        return url;
    }

    public String getFilePathWithName() {
        return filePathWithName;
    }

    public byte[] getFileByte() {
        return fileByte;
    }

    public File getFile() {
        return file;
    }

    public String getContentType() {
        return contentType;
    }

    public String getInterfaceParamName() {
        return interfaceParamName;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }
}
