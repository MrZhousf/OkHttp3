package com.okhttp3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.okhttp3.R;
import com.okhttp3.util.NetWorkUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.callback.CallbackOk;

import java.io.IOException;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

import static com.okhttplib.annotation.CacheType.CACHE_THEN_NETWORK;

/**
 * 网络请求：支持同步/异步、GET/POST、缓存请求
 * @author zhousf
 */
public class MainActivity extends BaseActivity {

    @Bind(R.id.syncBtn)
    Button syncBtn;
    @Bind(R.id.asyncBtn)
    Button asyncBtn;
    @Bind(R.id.cacheBtn)
    Button cacheBtn;
    @Bind(R.id.resultTV)
    TextView resultTV;
    @Bind(R.id.offlineBtn)
    Button offlineBtn;

    /**
     * 注意：测试时请更换该地址
     */
    private String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @OnClick({R.id.syncBtn, R.id.asyncBtn, R.id.cacheBtn, R.id.offlineBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.syncBtn:
                doHttpSync();
                break;
            case R.id.asyncBtn:
                doHttpAsync();
                break;
            case R.id.cacheBtn:
                doHttpCache();
                break;
            case R.id.offlineBtn:
                doHttpOffline();
                break;
        }
    }

    /**
     * 同步请求：由于不能在UI线程中进行网络请求操作，所以采用子线程方式
     */
    private void doHttpSync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpInfo info = HttpInfo.Builder()
                        .setUrl(url)
                        .addHead("head","test")
                        .build();
                OkHttpUtil.getDefault(this)
                        .doGetSync(info);
                if (info.isSuccessful()) {
                    final String result = info.getRetDetail();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTV.setText("同步请求：" + result);

                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 异步请求：回调方法可以直接操作UI
     */
    private void doHttpAsync() {
        OkHttpUtil.getDefault(this).doPostAsync(
                HttpInfo.Builder()
                        .setUrl(url)
                        .build(),
                new CallbackOk() {
                    @Override
                    public void onResponse(HttpInfo info) throws IOException {
                        if (info.isSuccessful()) {
                            String result = info.getRetDetail();
                            resultTV.setText("异步请求："+result);
                        }
                    }
                });

    }

    /**
     * 缓存请求：请连续点击缓存请求，会发现在缓存有效期内，从第一次请求后的每一次请求花费为0秒，说明该次请求为缓存响应
     */
    private void doHttpCache() {
        OkHttpUtil.Builder().setCacheType(CacheType.FORCE_CACHE)
                .build(this)
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(),
                        new CallbackOk() {
                            @Override
                            public void onResponse(HttpInfo info) throws IOException {
                                if (info.isSuccessful()) {
                                    String result = info.getRetDetail();
                                    resultTV.setText("缓存请求：" + result);
                                }
                            }
                        }
                );
    }

    /**
     * 断网请求：请先点击其他请求再测试断网请求
     */
    private void doHttpOffline(){
        if(!NetWorkUtil.isNetworkAvailable(this)){
            OkHttpUtil.Builder()
                    .setCacheType(CACHE_THEN_NETWORK)//缓存类型可以不设置
                    .build(this)
                    .doGetAsync(
                            HttpInfo.Builder().setUrl(url).build(),
                            new CallbackOk() {
                                @Override
                                public void onResponse(HttpInfo info) throws IOException {
                                    if (info.isSuccessful()) {
                                        String result = info.getRetDetail();
                                        resultTV.setText("断网请求：" + result);
                                    }
                                }
                            }
                    );
        }else{
            resultTV.setText("请先断网！");
        }
    }


}
