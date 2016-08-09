package com.okhttp3.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.okhttp3.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.httpBtn, R.id.uploadImgBtn, R.id.uploadFileBtn, R.id.downloadBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.httpBtn:
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                break;
            case R.id.uploadImgBtn:
                startActivity(new Intent(WelcomeActivity.this, UploadImageActivity.class));
                break;
            case R.id.uploadFileBtn:
                startActivity(new Intent(WelcomeActivity.this, UploadFileActivity.class));
                break;
            case R.id.downloadBtn:
                startActivity(new Intent(WelcomeActivity.this, DownloadActivity.class));
                break;
        }
    }
}
