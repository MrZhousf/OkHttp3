package com.okhttp3.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.okhttp3.R;
import com.okhttp3.util.FilePathUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.ProgressCallback;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadFileActivity extends ActionBarActivity {

    @Bind(R.id.uploadProgressOne)
    ProgressBar uploadProgressOne;
    @Bind(R.id.ivImageOne)
    ImageView ivImageOne;
    @Bind(R.id.uploadProgressTwo)
    ProgressBar uploadProgressTwo;
    @Bind(R.id.ivImageTwo)
    ImageView ivImageTwo;
    @Bind(R.id.tvResult)
    TextView tvResult;

    private final String TAG = UploadFileActivity.class.getSimpleName();

    /**
     * 文件上传地址
     */
    private String url = "";

    private String filePathOne;
    private String filePathTwo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.uploadImgBtnOne, R.id.chooseImgBtnOne,R.id.uploadImgBtnTwo,
            R.id.chooseImgBtnTwo,R.id.uploadImgBtnMulti})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chooseImgBtnOne:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
                break;
            case R.id.chooseImgBtnTwo:
                Intent intentTwo = new Intent();
                intentTwo.setType("image/*");
                intentTwo.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intentTwo, 2);
                break;
            case R.id.uploadImgBtnOne:
                if (!TextUtils.isEmpty(filePathOne)) {
                    uploadImgOne();
                } else {
                    Toast.makeText(this, "请先选择上传的图片！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.uploadImgBtnTwo:
                if (!TextUtils.isEmpty(filePathTwo)) {
                    uploadImgTwo();
                } else {
                    Toast.makeText(this, "请先选择上传的图片！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.uploadImgBtnMulti:
                if (!TextUtils.isEmpty(filePathOne) && !TextUtils.isEmpty(filePathTwo)) {
                    uploadImgMulti();
                } else {
                    Toast.makeText(this, "请先选择两张图片！", Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    /**
     * 上传第一张图片
     */
    private void uploadImgOne() {
        HttpInfo info = HttpInfo.Builder()
                .setUrl(url)
                .addUploadFile(filePathOne, "file", new ProgressCallback() {
                    @Override
                    public void onProgressMain(long bytesWritten, long contentLength, boolean done) {
                        int percent = (int) ((100 * bytesWritten) / contentLength);
                        uploadProgressOne.setProgress(percent);
                        Log.d(TAG, "上传进度1：" + percent);
                    }
                })
                .build();
        doUploadImg(info);
    }

    /**
     * 上传第二张图片
     */
    private void uploadImgTwo() {
        HttpInfo info = HttpInfo.Builder()
                .setUrl(url)
                .addUploadFile(filePathTwo, "file", new ProgressCallback() {
                    @Override
                    public void onProgressMain(long bytesWritten, long contentLength, boolean done) {
                        int percent = (int) ((100 * bytesWritten) / contentLength);
                        uploadProgressTwo.setProgress(percent);
                        Log.d(TAG, "上传进度2：" + percent);
                    }
                    @Override
                    public void onResponse(String filePath, HttpInfo info) {
                        tvResult.setText(info.getRetDetail());
                    }
                })
                .build();
        doUploadImg(info);
    }

    /**
     * 批量上传
     */
    private void uploadImgMulti() {
        HttpInfo info = HttpInfo.Builder()
                .setUrl(url)
                .addUploadFile(filePathOne, "file", new ProgressCallback() {
                    @Override
                    public void onProgressMain(long bytesWritten, long contentLength, boolean done) {
                        int percent = (int) ((100 * bytesWritten) / contentLength);
                        uploadProgressOne.setProgress(percent);
                        Log.d(TAG, "上传进度1：" + percent);
                    }

                    @Override
                    public void onResponse(String filePath, HttpInfo info) {
                        Log.d(TAG, "上传结果1：\n" + filePath+"\n"+info.getRetDetail());
                    }
                })
                .addUploadFile(filePathTwo, "file", new ProgressCallback() {
                    @Override
                    public void onProgressMain(long bytesWritten, long contentLength, boolean done) {
                        int percent = (int) ((100 * bytesWritten) / contentLength);
                        uploadProgressTwo.setProgress(percent);
                        Log.d(TAG, "上传进度2：" + percent);
                    }
                })
                .build();
        doUploadImg(info);
    }

    /**
     * 上传图片
     */
    private void doUploadImg(HttpInfo info){
        OkHttpUtil.getDefault(this).doUploadFile(info);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri contentUri = data.getData();
            String path = FilePathUtil.getFilePathFromUri(this, contentUri);
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(this, "获取图片地址失败", Toast.LENGTH_LONG).show();
                return ;
            }
            if(requestCode == 1){
                filePathOne = path;
            }
            if(requestCode == 2){
                filePathTwo = path;
            }
            showImage(contentUri,requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 显示图片
     */
    private void showImage(Uri contentUri,int what) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
            if (null != bitmap) {
                if(what == 1){
                    ivImageOne.setImageBitmap(bitmap);
                    ivImageOne.setVisibility(View.VISIBLE);
                }
                if(what == 2){
                    ivImageTwo.setImageBitmap(bitmap);
                    ivImageTwo.setVisibility(View.VISIBLE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
