package com.okhttp3.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.okhttp3.R;
import com.okhttp3.util.FilePathUtil;
import com.okhttp3.util.LogUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.ProgressCallback;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * 上传文件：支持批量上传、进度显示
 */
public class UploadFileActivity extends BaseActivity {

    private final String TAG = UploadFileActivity.class.getSimpleName();

    @Bind(R.id.uploadProgress)
    ProgressBar uploadProgress;
    @Bind(R.id.tvResult)
    TextView tvResult;
    @Bind(R.id.tvFile)
    TextView tvFile;

    /**
     * 文件上传地址
     */
    private String url = "";
    private String filePath;

    @Override
    protected int initLayout() {
        return R.layout.activity_upload_file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.chooseFileBtn, R.id.uploadFileBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chooseFileBtn:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*;image/*");//图片和视频
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.uploadFileBtn:
                uploadFile(filePath);
                break;
        }
    }

    private void uploadFile(String path){
        if(TextUtils.isEmpty(path)){
            Toast.makeText(this, "请选择上传文件！", Toast.LENGTH_LONG).show();
            return;
        }
        HttpInfo info = HttpInfo.Builder()
                .setUrl(url)
                .addUploadFile("file",path,new ProgressCallback(){
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgress.setProgress(percent);
                        LogUtil.d(TAG, "上传进度：" + percent);
                    }

                    @Override
                    public void onResponse(String filePath, HttpInfo info) {
                        tvResult.setText(info.getRetDetail());
                    }
                })
                .build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri contentUri = data.getData();
            filePath = FilePathUtil.getFilePathFromUri(this, contentUri);
            if (TextUtils.isEmpty(filePath)) {
                Toast.makeText(this, "获取文件地址失败", Toast.LENGTH_LONG).show();
                return;
            }
            tvFile.setText("上传文件：" + filePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}
