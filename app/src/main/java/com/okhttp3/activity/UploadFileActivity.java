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

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * 上传文件：支持批量上传、进度显示
 * @author zhousf
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
    private String url = "http://192.168.120.206:8080/office/upload/uploadFile";
    private String filePath;
    private List<String> imgList = new ArrayList<>();

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
                intent.setType("*/*");//文件
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.uploadFileBtn:
                uploadFile(filePath);
//                doUploadBatch();
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
                .addUploadFile("uploadFile",path,new ProgressCallback(){
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgress.setProgress(percent);
                        LogUtil.d(TAG, "上传进度：" + percent);
                    }

                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        tvResult.setText(info.getRetDetail());
                    }
                })
                .build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
    }

    /**
     * 单次批量上传：一次请求上传多个文件
     */
    private void doUploadBatch(){
        imgList.clear();
        imgList.add("/storage/emulated/0/okHttp_download/test.apk");
//        imgList.add("/storage/emulated/0/okHttp_download/test.rar");
        HttpInfo.Builder builder = HttpInfo.Builder()
                .setUrl(url);
        //循环添加上传文件
        for (String path: imgList  ) {
            //若服务器为php，接口文件参数名称后面追加"[]"表示数组，示例：builder.addUploadFile("uploadFile[]",path);
            builder.addUploadFile("uploadFile",path);
        }
        final HttpInfo info = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.getDefault(UploadFileActivity.this).doUploadFileSync(info,new ProgressCallback(){
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgress.setProgress(percent);
                        LogUtil.d(TAG, "上传进度：" + percent);
                    }

                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        LogUtil.d(TAG, "上传结果：" + info.getRetDetail());
                        tvResult.setText(info.getRetDetail());
                    }
                });
            }
        }).start();

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
