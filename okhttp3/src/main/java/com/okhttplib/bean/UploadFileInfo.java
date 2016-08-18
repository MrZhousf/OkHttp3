package com.okhttplib.bean;


import com.okhttplib.callback.ProgressCallback;

/**
 * 上传文件信息体
 * @author zhousf
 */
public class UploadFileInfo {

    //上传文件接口地址
    private String url;
    //上传的文件路径：包含文件名
    private String filePathWithName;
    //接口参数名称
    private String interfaceParamName;
    //上传进度回调接口
    private ProgressCallback progressCallback;

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

    public String getFilePathWithName() {
        return filePathWithName;
    }

    public void setFilePathWithName(String filePathWithName) {
        this.filePathWithName = filePathWithName;
    }

    public String getInterfaceParamName() {
        return interfaceParamName;
    }

    public void setInterfaceParamName(String interfaceParamName) {
        this.interfaceParamName = interfaceParamName;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
