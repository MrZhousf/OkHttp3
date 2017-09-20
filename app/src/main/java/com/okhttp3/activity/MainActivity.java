package com.okhttp3.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.okhttp3.R;
import com.okhttp3.bean.TimeAndDate;
import com.okhttp3.util.LogUtil;
import com.okhttp3.util.SelectorFactory;
import com.okhttp3.util.ToastUtil;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.Encoding;
import com.okhttplib.annotation.RequestType;
import com.okhttplib.callback.Callback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import base.BaseActivity;
import butterknife.Bind;
import butterknife.OnClick;

import static android.graphics.Color.GRAY;

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
    @Bind(R.id.sync_btn)
    Button syncBtn;
    @Bind(R.id.async_btn)
    Button asyncBtn;
    @Bind(R.id.force_network_btn)
    Button forceNetworkBtn;
    @Bind(R.id.force_cache_btn)
    Button forceCacheBtn;
    @Bind(R.id.network_then_cache_btn)
    Button networkThenCacheBtn;
    @Bind(R.id.cache_then_network_btn)
    Button cacheThenNetworkBtn;
    @Bind(R.id.ten_second_cache_btn)
    Button tenSecondCacheBtn;
    @Bind(R.id.delete_cache_btn)
    Button deleteCacheBtn;
    /**
     * 注意：测试时请更换该地址
     */
    private String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
//    private String url = "http://192.168.120.206:8080/office/api/time?key=zhousf_key";

    private boolean isNeedDeleteCache = true;

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置按钮圆角样式
        SelectorFactory.newShapeSelector()
                .setStrokeWidth(2)
                .setCornerRadius(15)
                .setDefaultStrokeColor(GRAY)
                .setDefaultBgColor(getResources().getColor(R.color.light_gray))
                .setPressedBgColor(getResources().getColor(R.color.light_blue))
                .bind(syncBtn)
                .bind(asyncBtn)
                .bind(forceNetworkBtn)
                .bind(forceCacheBtn)
                .bind(networkThenCacheBtn)
                .bind(cacheThenNetworkBtn)
                .bind(tenSecondCacheBtn)
                .bind(deleteCacheBtn);
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
            R.id.ten_second_cache_btn,
            R.id.delete_cache_btn
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sync_btn://同步请求
                sync();
                break;
            case R.id.async_btn://异步请求
                async();
                break;
            case R.id.force_network_btn://仅网络
                forceNetwork();
                break;
            case R.id.force_cache_btn://仅缓存
                forceCache();
                break;
            case R.id.network_then_cache_btn://先网络再缓存
                networkThenCache();
                break;
            case R.id.cache_then_network_btn://先缓存再网络
                cacheThenNetwork();
                break;
            case R.id.ten_second_cache_btn://缓存10秒失效
                tenSecondCache();
                break;
            case R.id.delete_cache_btn://清理缓存
                deleteCache();
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
                        .setResponseEncoding(Encoding.UTF_8)//设置该接口服务器响应编码
                        .setRequestEncoding(Encoding.UTF_8)//设置该接口请求参数编码
                        .build();
                doHttpSync(info);
                final String result = info.getRetDetail();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultTV.setText("同步请求：" + result);
                        setFromCacheTV(info);

                    }
                });
            }
        }).start();
        needDeleteCache(true);
    }

    /**
     * 异步请求：回调方法可以直接操作UI
     */
    private void async() {
        doHttpAsync(HttpInfo.Builder()
                        .setUrl(url)
                        .setRequestType(RequestType.GET)//设置请求方式
                        .addHead("head", "test")//添加头参数
                        .addParam("param", "test")//添加接口参数
                        .setDelayExec(2, TimeUnit.SECONDS)//延迟2秒执行
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
                        TimeAndDate time = info.getRetDetail(TimeAndDate.class);
                        LogUtil.d("MainActivity", time.getResult().toString());
                        setFromCacheTV(info);
                    }
                });
        needDeleteCache(true);
    }

    /**
     * 仅网络请求
     */
    private void forceNetwork() {
        OkHttpUtil.Builder().setCacheType(CacheType.FORCE_NETWORK).build(this)
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
        needDeleteCache(true);
    }

    /**
     * 仅缓存请求
     */
    private void forceCache() {
        OkHttpUtil.Builder().setCacheType(CacheType.FORCE_CACHE).build(this)
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
        needDeleteCache(true);
    }

    /**
     * 先网络再缓存：先请求网络，失败则请求缓存
     */
    private void networkThenCache() {
        OkHttpUtil.Builder().setCacheType(CacheType.NETWORK_THEN_CACHE).build(this)
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
        needDeleteCache(true);
    }

    /**
     * 先缓存再网络：先请求缓存，失败则请求网络
     */
    private void cacheThenNetwork() {
        OkHttpUtil.Builder().setCacheType(CacheType.CACHE_THEN_NETWORK).build(this)
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
        needDeleteCache(true);
    }

    /**
     * 缓存10秒失效：连续点击进行测试10秒内再次请求为缓存响应，10秒后再请求则缓存失效并进行网络请求
     */
    private void tenSecondCache() {
        //由于采用同一个url测试，需要先清理缓存
        if (isNeedDeleteCache) {
            isNeedDeleteCache = false;
            OkHttpUtil.getDefault().deleteCache();
        }
        OkHttpUtil.Builder()
                .setCacheType(CacheType.CACHE_THEN_NETWORK)
                .setCacheSurvivalTime(10)//缓存存活时间为10秒
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


    private void needDeleteCache(boolean delete) {
        isNeedDeleteCache = delete;
    }

    private void setFromCacheTV(HttpInfo info) {
        fromCacheTV.setText(info.isFromCache() ? "缓存请求" : "网络请求");
    }

    /**
     * 清理缓存
     */
    private void deleteCache() {
        if (OkHttpUtil.getDefault().deleteCache()) {
            ToastUtil.show(this, "清理缓存成功");
        } else {
            ToastUtil.show(this, "清理缓存失败");
        }
    }


}
