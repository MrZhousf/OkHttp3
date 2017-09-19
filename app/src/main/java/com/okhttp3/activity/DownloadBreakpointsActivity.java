package com.okhttp3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.okhttp3.R;
import com.okhttp3.util.LogUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.DownloadStatus;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.callback.ProgressCallback;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;


/**
 * 文件下载：支持断点下载、进度显示
 * @author zhousf
 */
public class DownloadBreakpointsActivity extends BaseActivity {

    private final String TAG = DownloadBreakpointsActivity.class.getSimpleName();

    @Bind(R.id.downloadProgress)
    ProgressBar downloadProgress;
    @Bind(R.id.tvResult)
    TextView tvResult;

    private DownloadFileInfo fileInfo;
    private String url = "http://dldir1.qq.com/qqfile/qq/QQ8.9.1/20453/QQ8.9.1.exe";


    @Override
    protected int initLayout() {
        return R.layout.activity_download_breakpoints;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.downloadBtn, R.id.pauseBtn, R.id.continueBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.downloadBtn://下载
                download();
                break;
            case R.id.pauseBtn://暂停下载
                if(null != fileInfo)
                    fileInfo.setDownloadStatus(DownloadStatus.PAUSE);
                break;
            case R.id.continueBtn://继续下载
                download();
                break;
        }
    }

    private void download(){
        if(null == fileInfo)
            fileInfo = new DownloadFileInfo(url,"QQ8.9.1.exe",new ProgressCallback(){
                @Override
                public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                    downloadProgress.setProgress(percent);
                    tvResult.setText(percent+"%");
                    LogUtil.d(TAG, "下载进度：" + percent);
                }
                @Override
                public void onResponseMain(String filePath, HttpInfo info) {
                    if(info.isSuccessful()){
                        tvResult.setText(info.getRetDetail()+"\n下载状态："+fileInfo.getDownloadStatus());
                    }else{
                        Toast.makeText(DownloadBreakpointsActivity.this,info.getRetDetail(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        HttpInfo info = HttpInfo.Builder().addDownloadFile(fileInfo).build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
    }





}
