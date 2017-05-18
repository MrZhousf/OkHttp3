package com.okhttp3.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.okhttp3.R;
import com.okhttp3.util.FilePathUtil;
import com.okhttp3.util.LogUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.ProgressCallback;

import java.io.IOException;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * 上传图片：支持批量上传、进度显示
 * @author zhousf
 */
public class UploadImageActivity extends BaseActivity {

    @Bind(R.id.uploadProgressOne)
    ProgressBar uploadProgressOne;
    @Bind(R.id.ivImageOne)
    ImageView ivImageOne;
    @Bind(R.id.uploadProgressTwo)
    ProgressBar uploadProgressTwo;
    @Bind(R.id.ivImageTwo)
    ImageView ivImageTwo;

    private final String TAG = UploadImageActivity.class.getSimpleName();

    /**
     * 文件上传地址
     */
    private String url = "http://192.168.120.206:8080/office/upload/uploadFile";

    private String filePathOne;
    private String filePathTwo;

    @Override
    protected int initLayout() {
        return R.layout.activity_upload_image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                .addUploadFile("uploadFile", filePathOne, new ProgressCallback() {
                    //onProgressMain为UI线程回调，可以直接操作UI
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgressOne.setProgress(percent);
                        LogUtil.d(TAG, "上传进度1：" + percent);
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
                .addUploadFile("uploadFile", filePathTwo,new ProgressCallback() {
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgressTwo.setProgress(percent);
                        LogUtil.d(TAG, "上传进度2：" + percent);
                    }
                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        LogUtil.d(TAG, "上传结果2 Main：\n" + filePath+"\n"+info.getRetDetail());
                    }
                    @Override
                    public void onResponseSync(String filePath, HttpInfo info) {
                        LogUtil.d(TAG, "上传结果2 Sync：\n" + filePath+"\n"+info.getRetDetail());
                    }
                })
                .build();
        doUploadImg(info);
    }

    /**
     * 批量上传：一次性批量上传请使用UploadFileActivity中的doUploadBatch方法
     */
    private void uploadImgMulti() {
        HttpInfo info = HttpInfo.Builder()
                .setUrl(url)
                .addUploadFile("file", filePathOne, new ProgressCallback() {
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgressOne.setProgress(percent);
                        LogUtil.d(TAG, "上传进度1：" + percent);
                    }
                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        LogUtil.d(TAG, "上传结果1：\n" + filePath+"\n"+info.getRetDetail());
                    }
                })
                .addUploadFile("file", filePathTwo,new ProgressCallback() {
                    @Override
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        uploadProgressTwo.setProgress(percent);
                        LogUtil.d(TAG, "上传进度2：" + percent);
                    }
                    @Override
                    public void onResponseMain(String filePath, HttpInfo info) {
                        LogUtil.d(TAG, "上传结果2：\n" + filePath+"\n"+info.getRetDetail());
                    }

                })
                .build();
        doUploadImg(info);
    }

    /**
     * 上传图片
     */
    private void doUploadImg(final HttpInfo info){
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
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
