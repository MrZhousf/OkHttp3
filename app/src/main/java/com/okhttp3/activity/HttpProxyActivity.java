package com.okhttp3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.okhttp3.R;
import com.okhttp3.util.SelectorFactory;

import java.io.IOException;

import base.BaseActivity;
import base.http.HttpCallback;
import base.http.HttpEntity;
import base.http.HttpProxy;
import butterknife.Bind;
import butterknife.OnClick;

import static android.graphics.Color.GRAY;

/**
 * 网络请求代理方式：
 * 建议采用该方式集成第三方http网络库，方便以后替换底层库
 * 动态代理方式可以对网络请求进行AOP操作
 *
 * @author zhousf
 */
public class HttpProxyActivity extends BaseActivity {

    @Bind(R.id.resultTV)
    TextView resultTV;
    @Bind(R.id.sync_btn)
    Button syncBtn;
    @Bind(R.id.async_btn)
    Button asyncBtn;

    private String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";

    @Override
    protected int initLayout() {
        return R.layout.activity_http_proxy;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SelectorFactory.newShapeSelector()
                .setStrokeWidth(2)
                .setCornerRadius(15)
                .setDefaultStrokeColor(GRAY)
                .setDefaultBgColor(getResources().getColor(R.color.light_gray))
                .setPressedBgColor(getResources().getColor(R.color.light_blue))
                .bind(syncBtn)
                .bind(asyncBtn);
    }

    @OnClick({
            R.id.sync_btn,
            R.id.async_btn
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sync_btn://同步请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpEntity httpEntity = HttpEntity.create().setUrl(url);
                        HttpProxy.get().doGetSync(httpEntity);
                        final String result = httpEntity.getRetDetail();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTV.setText("同步请求：" + result);

                            }
                        });
                    }
                }).start();
                break;
            case R.id.async_btn://异步请求
                HttpProxy.get().doGetAsync(HttpEntity.create().setUrl(url), new HttpCallback() {
                    @Override
                    public void onSuccess(HttpEntity info) throws IOException {
                        resultTV.setText("异步请求成功：" + info.getRetDetail());
                    }

                    @Override
                    public void onFailure(HttpEntity info) throws IOException {
                        resultTV.setText("异步请求失败：" + info.getRetDetail());
                    }
                });
                break;
        }
    }





}
