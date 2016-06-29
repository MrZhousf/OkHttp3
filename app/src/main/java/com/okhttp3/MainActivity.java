package com.okhttp3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import http.HttpInfo;
import http.OkHttpUtil;
import util.NetWorkUtil;

public class MainActivity extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
                HttpInfo info = HttpInfo.Builder().setUrl(url).build(this);
                OkHttpUtil.Builder().build().doGetSync(info);
                if (info.isSuccessful()) {
                    String result = info.getRetDetail();
                    runOnUiThread(() -> {
                        resultTV.setText("同步请求："+result);
                    });
                }
            }
        }).start();
    }

    /**
     * 异步请求：回调方法可以直接操作UI
     */
    private void doHttpAsync() {
        OkHttpUtil.Builder().build().doGetAsync(
                HttpInfo.Builder().setUrl(url).build(this),
                info -> {
                    if (info.isSuccessful()) {
                        String result = info.getRetDetail();
                        resultTV.setText("异步请求："+result);
                    }
                });

    }

    /**
     * 缓存请求：请连续点击缓存请求，会发现在缓存有效期内，从第一次请求后的每一次请求花费为0秒，说明该次请求为缓存响应
     */
    private void doHttpCache() {
        OkHttpUtil.Builder()
                .setCacheLevel(OkHttpUtil.CacheLevel.SECOND_LEVEL)
                .build()
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(this),
                        info -> {
                            if (info.isSuccessful()) {
                                String result = info.getRetDetail();
                                resultTV.setText("缓存请求："+result);
                            }
                        });
    }

    /**
     * 断网请求：请先点击其他请求再测试断网请求
     */
    private void doHttpOffline(){
        if(!NetWorkUtil.isNetworkAvailable(this)){
            OkHttpUtil.Builder()
                    .setCacheType(OkHttpUtil.CacheType.CACHE_THEN_NETWORK)//缓存类型可以不设置
                    .build()
                    .doGetAsync(
                            HttpInfo.Builder().setUrl(url).build(this),
                            info -> {
                                if (info.isSuccessful()) {
                                    String result = info.getRetDetail();
                                    resultTV.setText("断网请求："+result);
                                }
                            });
        }else{
            resultTV.setText("请先断网！");
        }
    }


}
