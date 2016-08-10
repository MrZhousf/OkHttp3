package com.okhttplib.bean;


import com.okhttplib.callback.ProgressCallback;

/**
 * 下载文件信息体
 */
public class DownloadFileInfo {

    //下载文件接口地址
    private String url;
    //文件保存目录
    private String saveFileDir;
    //文件保存名称
    private String saveFileName;
    //上传进度回调接口
    private ProgressCallback progressCallback;

    public DownloadFileInfo(String url, String saveFileDir, String saveFileName, ProgressCallback progressCallback) {
        this.url = url;
        this.saveFileDir = saveFileDir;
        this.saveFileName = saveFileName;
        this.progressCallback = progressCallback;
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

    public String getSaveFileDir() {
        return saveFileDir;
    }

    public void setSaveFileDir(String saveFileDir) {
        this.saveFileDir = saveFileDir;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }
}
