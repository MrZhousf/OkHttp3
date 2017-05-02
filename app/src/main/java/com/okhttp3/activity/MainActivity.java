package com.okhttp3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.okhttp3.R;
import com.okhttp3.bean.TimeAndDate;
import com.okhttp3.util.LogUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.Encoding;
import com.okhttplib.callback.Callback;

import java.io.IOException;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * 网络请求：支持同步/异步、GET/POST、缓存请求
 *
 * @author zhousf
 */
public class MainActivity extends BaseActivity {

    @Bind(R.id.fromCacheTV)
    TextView fromCacheTV;
    @Bind(R.id.resultTV)
    TextView resultTV;
    /**
     * 注意：测试时请更换该地址
     */
    private String url = "http://192.168.120.206:8080/office/api/time?key=zhousf_key";

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

    @OnClick({
            R.id.sync_btn,
            R.id.async_btn,
            R.id.force_network_btn,
            R.id.force_cache_btn,
            R.id.network_then_cache_btn,
            R.id.cache_then_network_btn,
            R.id.ten_second_cache_btn
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sync_btn:
                sync();
                break;
            case R.id.async_btn:
                async();

                break;
            case R.id.force_network_btn:
                forceNetwork();
                break;
            case R.id.force_cache_btn:
                forceCache();
                break;
            case R.id.network_then_cache_btn:
                networkThenCache();
                break;
            case R.id.cache_then_network_btn:
                cacheThenNetwork();
                break;
            case R.id.ten_second_cache_btn:
                tenSecondCache();
                break;
        }
    }

    /**
     * 同步请求：由于不能在UI线程中进行网络请求操作，所以采用子线程方式
     */
    private void sync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpInfo info = HttpInfo.Builder()
                        .setUrl(url)
                        .setResponseEncoding(Encoding.UTF_8)//设置服务器响应编码
                        .build();
                OkHttpUtil.getDefault(this)
                        .doGetSync(info);
                final String result = info.getRetDetail();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultTV.setText("同步请求：" + result);

                    }
                });
            }
        }).start();
        isNeedDeleteCache(true);
    }

    /**
     * 异步请求：回调方法可以直接操作UI
     */
    private void async() {
        OkHttpUtil.getDefault(this).doGetAsync(
                HttpInfo.Builder()
                        .setUrl(url)
                        .build(),
                new Callback() {
                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        String result = info.getRetDetail();
                        resultTV.setText("异步请求失败：" + result);
                    }

                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String result = info.getRetDetail();
                        resultTV.setText("异步请求成功：" + result);
                        //GSon解析
                        TimeAndDate time = new Gson().fromJson(result, TimeAndDate.class);
                        LogUtil.d("MainActivity", time.getResult().toString());
                    }
                });
        isNeedDeleteCache(true);
    }

    /**
     * 仅网络请求
     */
    private void forceNetwork(){
        OkHttpUtil.Builder().setCacheType(CacheType.FORCE_NETWORK)
                .build(this)
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(),
                        new Callback() {
                            @Override
                            public void onSuccess(HttpInfo info) throws IOException {
                                String result = info.getRetDetail();
                                resultTV.setText("FORCE_NETWORK：" + result);
                                setFromCacheTV(info);
                            }

                            @Override
                            public void onFailure(HttpInfo info) throws IOException {
                                resultTV.setText("FORCE_NETWORK：" + info.getRetDetail());
                            }
                        }
                );
        isNeedDeleteCache(true);
    }

    /**
     * 仅缓存请求
     */
    private void forceCache(){
        OkHttpUtil.Builder().setCacheType(CacheType.FORCE_CACHE)
                .build(this)
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(),
                        new Callback() {
                            @Override
                            public void onSuccess(HttpInfo info) throws IOException {
                                String result = info.getRetDetail();
                                resultTV.setText("FORCE_CACHE：" + result);
                                setFromCacheTV(info);
                            }

                            @Override
                            public void onFailure(HttpInfo info) throws IOException {
                                resultTV.setText("FORCE_CACHE：" + info.getRetDetail());
                            }
                        }
                );
        isNeedDeleteCache(true);
    }

    /**
     * 先请求网络，失败则请求缓存
     */
    private void networkThenCache() {
        OkHttpUtil.Builder().setCacheType(CacheType.NETWORK_THEN_CACHE)
                .build(this)
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(),
                        new Callback() {
                            @Override
                            public void onSuccess(HttpInfo info) throws IOException {
                                String result = info.getRetDetail();
                                resultTV.setText("NETWORK_THEN_CACHE：" + result);
                                setFromCacheTV(info);
                            }

                            @Override
                            public void onFailure(HttpInfo info) throws IOException {
                                resultTV.setText("NETWORK_THEN_CACHE：" + info.getRetDetail());
                            }
                        }
                );
        isNeedDeleteCache(true);
    }

    /**
     * 先请求缓存，失败则请求网络
     */
    private void cacheThenNetwork() {
        OkHttpUtil.Builder().setCacheType(CacheType.CACHE_THEN_NETWORK)
                .build(this)
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(),
                        new Callback() {
                            @Override
                            public void onSuccess(HttpInfo info) throws IOException {
                                String result = info.getRetDetail();
                                resultTV.setText("CACHE_THEN_NETWORK：" + result);
                                setFromCacheTV(info);
                            }

                            @Override
                            public void onFailure(HttpInfo info) throws IOException {
                                resultTV.setText("CACHE_THEN_NETWORK：" + info.getRetDetail());
                            }
                        }
                );
        isNeedDeleteCache(true);
    }

    private void isNeedDeleteCache(boolean delete){
        isNeedDeleteCache = delete;
    }

    private boolean isNeedDeleteCache = true;

    /**
     * 连续点击进行测试10秒内再次请求为缓存响应，10秒后再请求则缓存失效并进行网络请求
     */
    private void tenSecondCache(){
        //由于采用同一个url测试，需要先清理缓存
        if(isNeedDeleteCache){
            OkHttpUtil.getDefault().deleteCache();
            isNeedDeleteCache = false;
        }
        OkHttpUtil.Builder().setCacheType(CacheType.CACHE_THEN_NETWORK).setCacheSurvivalTime(10)
                .build(this)
                .doGetAsync(
                        HttpInfo.Builder().setUrl(url).build(),
                        new Callback() {
                            @Override
                            public void onSuccess(HttpInfo info) throws IOException {
                                String result = info.getRetDetail();
                                resultTV.setText("缓存10秒失效：" + result);
                                setFromCacheTV(info);
                            }

                            @Override
                            public void onFailure(HttpInfo info) throws IOException {
                                resultTV.setText("缓存10秒失效：" + info.getRetDetail());
                            }
                        }
                );
    }

    private void setFromCacheTV(HttpInfo info){
        fromCacheTV.setText(info.isFromCache()?"缓存请求":"网络请求");
    }



}
