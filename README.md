
##OkHttp3
基于OkHttp3封装的网络请求工具类


##功能点
* 支持Http/Https等协议
* 支持缓存响应，缓存等级
* 断网请求
* 同步/异步请求
* Json自动解析
* 请求与Activity/Fragment生命周期绑定，自动取消请求
* 异步请求切换到UI线程，摒弃runOnUiThread
* Application中自定义全局配置/增加系统默认配置
* 完善的日志跟踪
* 后续优化中...

##引用方式
###Maven
```java
<dependency>
  <groupId>com.zhousf.lib</groupId>
  <artifactId>okhttp3</artifactId>
  <version>1.2.2</version>
  <type>pom</type>
</dependency>
```
###Gradle
```java
compile 'com.zhousf.lib:okhttp3:1.2.2'
```

##提交记录
* 2016-6-29 项目提交
* 2016-7-4 
    *  项目框架调整
    *  代码优化、降低耦合
    *  增加Application中全局配置
    *  增加系统默认配置
    *  修复内存释放bug

##权限
```java
    <!-- 添加读写权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <!-- 访问互联网权限 -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

##自定义全局配置
在Application中配置如下：
```java
OkHttpUtil.init(this)
                .setConnectTimeout(30)//超时设置
                .setMaxCacheSize(10 * 1024 * 1024)//设置缓存空间
                .setCacheLevel(CacheLevel.FIRST_LEVEL)//缓存等级
                .setShowHttpLog(true)//显示请求日志
                .setShowLifecycleLog(true)
                .build();
```

##在Activity中同步调用示例
```java
    /**
     * 同步请求：由于不能在UI线程中进行网络请求操作，所以采用子线程方式
     */
    private void doHttpSync() {
        new Thread(()-> {
                HttpInfo info = HttpInfo.Builder().setUrl(url).build();
                OkHttpUtil.getDefault(MainActivity.this).doGetSync(info);
                if (info.isSuccessful()) {
                    final String result = info.getRetDetail();
                    runOnUiThread(() -> {
                            resultTV.setText("同步请求：" + result);
                        }
                    );
                }
            }
        ).start();
    }
```

##在Activity中异步调用示例
```java
  /**
     * 异步请求：回调方法可以直接操作UI
     */
    private void doHttpAsync() {
        OkHttpUtil.Builder()
                .setCacheLevel(CacheLevel.FIRST_LEVEL)
                .setConnectTimeout(25).build(this)
                .doGetAsync(
                HttpInfo.Builder().setUrl(url).build(),
                info -> {
                    if (info.isSuccessful()) {
                        String result = info.getRetDetail();
                        resultTV.setText("异步请求："+result);
                    }
                });

    }
```

##相关截图
### 界面
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/1.jpg?raw=true)
### 日志
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/2.jpg?raw=true)
* GET-URL：请求地址
* CostTime：请求耗时（单位：秒）
* Response：响应串


##有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流

* QQ: 424427633


##感激
感谢以下的项目,排名不分先后

* [OkHttp](https://github.com/square/okhttp/) 


##相关示例

```java
   package com.okhttp3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.okhttplib.annotation.CacheLevel;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
                HttpInfo info = HttpInfo.Builder().setUrl(url).build(MainActivity.this);
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
        OkHttpUtil.Builder().setCacheLevel(CacheLevel.FIRST_LEVEL).setConnectTimeout(25).build().doGetAsync(
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
                .setCacheLevel(CacheLevel.SECOND_LEVEL)
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
                    .setCacheType(CacheType.CACHE_THEN_NETWORK)//缓存类型可以不设置
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


```
