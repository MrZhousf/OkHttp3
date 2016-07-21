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
import com.okhttplib.callback.CallbackOk;
import com.okhttplib.callback.ProgressCallback;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadFileActivity extends ActionBarActivity {

    @Bind(R.id.uploadProgress)
    ProgressBar uploadProgress;
    @Bind(R.id.ivImage)
    ImageView ivImage;
    @Bind(R.id.tvResult)
    TextView tvResult;

    private final String TAG = UploadFileActivity.class.getSimpleName();

    private String url = "https://admin.jrtoo.com/cifcogroup/application/web/index.php?r=file/upload";

    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.uploadImgBtn, R.id.chooseImgBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chooseImgBtn:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
                break;
            case R.id.uploadImgBtn:
                if (!TextUtils.isEmpty(filePath)) {
                    doUploadImg();
                } else {
                    Toast.makeText(this, "请先选择上传的图片！", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * 上传图片
     */
    private void doUploadImg() {
        HttpInfo info = HttpInfo.Builder()
                .setUrl(url)
                .addUploadFile(filePath, "file", new ProgressCallback() {
                    @Override
                    public void onProgress(long bytesWritten, long contentLength, boolean done) {
                        int percent = (int) ((100 * bytesWritten) / contentLength);
                        uploadProgress.setProgress(percent);
                        Log.d(TAG, "上传进度：" + percent);
                    }
                })
                .build();
        OkHttpUtil.getDefault(this).doUploadFile(info, new CallbackOk() {
            @Override
            public void onResponse(HttpInfo info) throws IOException {
                tvResult.setText("上传结果：" + info.getRetDetail());
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri contentUri = data.getData();
            filePath = FilePathUtil.getFilePathFromUri(this, contentUri);
            if (!TextUtils.isEmpty(filePath)) {
                showImage(contentUri);
            } else {
                Toast.makeText(this, "获取图片地址失败", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 显示图片
     */
    private void showImage(Uri contentUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
            if (null != bitmap) {
                ivImage.setImageBitmap(bitmap);
                ivImage.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
