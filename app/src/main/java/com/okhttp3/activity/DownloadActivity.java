package com.okhttp3.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
 */
public class DownloadActivity extends BaseActivity {

    private final String TAG = DownloadActivity.class.getSimpleName();
    @Bind(R.id.downloadProgressOne)
    ProgressBar downloadProgressOne;
    @Bind(R.id.tvResultTwo)
    TextView tvResultTwo;
    @Bind(R.id.downloadProgressTwo)
    ProgressBar downloadProgressTwo;
    @Bind(R.id.tvResultOne)
    TextView tvResultOne;

    /**
     * 文件网络地址
     */
    private String url = "http://www.jcodecraeer.com/uploads/allimg/160602/223U25642-0.jpg";

    @Override
    protected int initLayout() {
        return R.layout.activity_download;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @OnClick(R.id.downloadBtn)
    public void onClick() {
        downloadFile(url);
    }


    private void downloadFile(String url) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "文件网络地址为空！", Toast.LENGTH_LONG).show();
            return;
        }
        final HttpInfo info = HttpInfo.Builder()
                .addDownloadFile("http://downmp413.ffxia.com/mp413/%E7%8E%8B%E5%AD%90%E6%96%87-%E7%94%9F%E5%A6%82%E5%A4%8F%E8%8A%B1[68mtv.com].mp4", "file1", new ProgressCallback() {
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        downloadProgressOne.setProgress(percent);
                        tvResultOne.setText(percent+"%");
                        LogUtil.d(TAG, "下载进度1：" + percent);
                    }

                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        tvResultOne.setText(info.getRetDetail());
                        LogUtil.d(TAG, "下载结果1：" + info.getRetDetail());
                    }
                })
//                .addDownloadFile(url, "file2", new ProgressCallback() {
//                    @Override
//                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
//                        downloadProgressTwo.setProgress(percent);
//                        tvResultTwo.setText(percent+"%");
//                        LogUtil.d(TAG, "下载进度2：" + percent);
//                    }
//
//                    @Override
//                    public void onResponseMain(String filePath, HttpInfo info) {
//                        tvResultTwo.setText(info.getRetDetail());
//                        LogUtil.d(TAG, "下载结果2：" + info.getRetDetail());
//                    }
//                })
                .build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);



    }


}
