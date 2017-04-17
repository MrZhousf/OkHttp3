package com.okhttplib.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 下载状态
 * @author zhousf
 */
@StringDef({DownloadStatus.INIT,DownloadStatus.DOWNLOADING,DownloadStatus.PAUSE,DownloadStatus.COMPLETED})
@Retention(RetentionPolicy.SOURCE)
public @interface DownloadStatus {

    /**
     * 初始化状态
     */
    String INIT = "INIT";

    /**
     * 正在下载状态
     */
    String DOWNLOADING = "DOWNLOADING";

    /**
     * 暂停状态
     */
    String PAUSE = "PAUSE";

    /**
     * 下载完成状态
     */
    String COMPLETED = "COMPLETED";

}