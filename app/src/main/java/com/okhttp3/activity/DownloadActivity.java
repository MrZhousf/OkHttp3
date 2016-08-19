package com.okhttp3.activity;

import android.os.Bundle;
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
    private String url1 = "http://www.jcodecraeer.com/uploads/allimg/160602/223U25642-0.jpg";
    private String url2 = "http://img1.gtimg.com/2016/pics/hv1/220/150/2115/137566345.jpg";
    private String mp4Url = "http://downmp413.ffxia.com/mp413/%E7%8E%8B%E5%AD%90%E6%96%87-%E7%94%9F%E5%A6%82%E5%A4%8F%E8%8A%B1[68mtv.com].mp4";

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
        downloadFile();
    }


    private void downloadFile() {
        final HttpInfo info = HttpInfo.Builder()
                .addDownloadFile(url1, "file1", new ProgressCallback() {
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
                .addDownloadFile(mp4Url, "file2", new ProgressCallback() {
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        downloadProgressTwo.setProgress(percent);
                        tvResultTwo.setText(percent+"%");
                        LogUtil.d(TAG, "下载进度2：" + percent);
                    }

                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        tvResultTwo.setText(info.getRetDetail());
                        LogUtil.d(TAG, "下载结果2：" + info.getRetDetail());
                    }
                })
                .build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);



    }


}
