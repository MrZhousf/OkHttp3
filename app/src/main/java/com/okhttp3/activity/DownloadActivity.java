package com.okhttp3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.okhttp3.R;
import com.okhttp3.util.LogUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.ProgressCallback;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * 文件下载：支持批量下载、进度显示
 * @author zhousf
 */
public class DownloadActivity extends BaseActivity {

    private final String TAG = DownloadActivity.class.getSimpleName();
    @Bind(R.id.tvResult)
    TextView tvResult;
    @Bind(R.id.downloadProgress)
    ProgressBar downloadProgress;

    /**
     * 文件网络地址
     */
    private String fileURL = "http://dldir1.qq.com/qqfile/qq/QQ8.9.1/20453/QQ8.9.1.exe";
    private final String requestTag = "download-tag-1001";//请求标识


    @Override
    protected int initLayout() {
        return R.layout.activity_download;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @OnClick({R.id.downloadBtn,R.id.downloadCancelBtn})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.downloadBtn:
                downloadFile();
                break;
            case R.id.downloadCancelBtn://取消请求
                OkHttpUtil.getDefault().cancelRequest(requestTag);
                break;
        }
    }


    private void downloadFile() {
        final HttpInfo info = HttpInfo.Builder()
                .addDownloadFile(fileURL, "qq.exe", new ProgressCallback() {
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        downloadProgress.setProgress(percent);
                        tvResult.setText(percent+"%");
                        LogUtil.d(TAG, "下载进度：" + percent);
                    }

                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        tvResult.setText(info.getRetDetail());
                        LogUtil.d(TAG, "下载结果：" + info.getRetDetail());
                        OkHttpUtil.getDefault().cancelRequest(requestTag);
                    }
                })
                .build();
        OkHttpUtil.Builder()
                .setReadTimeout(120)
                .build(requestTag)//绑定请求标识
                .doDownloadFileAsync(info);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
