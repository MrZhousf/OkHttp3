package com.okhttp3.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.okhttp3.R;
import com.okhttp3.util.LogUtil;
import com.okhttp3.util.ToastUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.ProgressCallback;

import base.BaseActivity;
import base.networkstate.NetSpeedService;
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
        Intent intent = new Intent(DownloadActivity.this, NetSpeedService.class);
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
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
                        ToastUtil.show(DownloadActivity.this,info.getRetDetail());
                        tvResult.setText(info.getRetDetail());
                        LogUtil.d(TAG, "下载结果：" + info.getRetDetail());
                    }
                })
                .build();
        OkHttpUtil.Builder()
                .setReadTimeout(120)
                .build(requestTag)//绑定请求标识
                .doDownloadFileAsync(info);

    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {
        OkHttpUtil.getDefault().cancelRequest(requestTag);
        super.onDestroy();
        unbindService(conn);
    }
}
