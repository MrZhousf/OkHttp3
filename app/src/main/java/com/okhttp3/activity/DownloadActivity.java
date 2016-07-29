package com.okhttp3.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.okhttp3.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadActivity extends ActionBarActivity {

    @Bind(R.id.downloadProgress)
    ProgressBar downloadProgress;
    @Bind(R.id.tvResult)
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.downloadBtn)
    public void onClick() {

    }



}
