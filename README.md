## OkHttp3
基于OkHttp3封装的网络请求库
## 功能点
* 支持Http/Https协议
* 支持同步/异步请求
* 支持异步延迟执行
* 支持Post/Get/Put/Delete请求
* 支持Cookie持久化，支持Gzip压缩
* 支持协议头参数Head设置
* 支持二进制参数、JSON、表单提交
* 支持Unicode自动转码、请求参数编码以及服务器响应编码设置
* 支持四种缓存类型请求：仅网络、仅缓存、先网络再缓存、先缓存再网络
* 支持自定义缓存存活时间与缓存清理功能
* 当Activity/Fragment销毁时自动取消相应的所有网络请求，支持取消指定请求
* 异步请求响应自动切换到UI线程，摒弃runOnUiThread
* Application中自定义全局配置/增加系统默认配置
* 支持文件和图片上传/批量上传，支持同步/异步上传，支持进度提示
* 支持文件下载/批量下载，支持同步/异步下载，支持进度提示
* 支持文件断点下载，独立下载的模块摒弃了数据库记录断点的过时方法
* 完整的日志跟踪与异常处理
* 支持请求结果拦截以及异常处理拦截
* 支持单例客户端，提高网络请求速率
* 后续优化中...


## 相关截图
### 网络请求演示
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/1.gif?raw=true)
### 先网络再缓存演示
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/2.gif?raw=true)
### 先缓存再网络演示
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/3.gif?raw=true)
### 上传图片界面
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/3.jpg?raw=true)
### 断点下载文件界面
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/4.jpg?raw=true)
### 日志
![](https://github.com/MrZhousf/OkHttp3/blob/master/pic/2.jpg?raw=true)
* GET-URL/POST-URL：请求地址
* CostTime：请求耗时（单位：秒）
* Response：响应串

## 项目演示DEMO
项目中已包含所有支持业务的demo，详情请下载项目参考源码。

## 引用方式
### Maven
```
<dependency>
  <groupId>com.zhousf.lib</groupId>
  <artifactId>okhttp3</artifactId>
  <version>2.8.9</version>
  <type>pom</type>
</dependency>
```
### Gradle
```
compile 'com.zhousf.lib:okhttp3:2.8.9'
```
若出现support-annotations版本冲突请采用下面方式进行依赖：
```
compile ('com.zhousf.lib:okhttp3:2.8.9'){
    exclude(module: 'support-annotations')
}
```
### ProGuard
如果你使用了ProGuard混淆，请添加如下配置:
```
-dontwarn okio.**
```

## 提交记录
* 2016-6-29 项目提交
* 2016-7-4 
    *  项目框架调整
    *  增加Application中全局配置
    *  增加系统默认配置
    *  修复内存释放bug
* 2016-7-19
    *  代码优化、降低耦合
    *  修复已知bug
* 2016-8-9
    *  增加文件上传功能，支持批量上传
* 2016-8-10
    *  增加文件下载功能，支持批量下载、断点下载
* 2016-10-10
    *  增加请求结果拦截以及异常处理拦截
* 2016-10-25
    *  支持Cookie持久化、协议头参数Head设置
* 2016-12-12
    *  增加单例客户端，提高网络请求速率、取消指定请求功能
* 2017-3-3
    *  修复上传文件入参bug（感谢*Sanqi5401*指正）
* 2017-3-6
    *  在集成过程中出现了okio丢失的情况请添加 compile 'com.android.support:multidex:1.0.1'
（感谢*kevin*提供相关解决方案）https://stackoverflow.com/questions/36649121/java-lang-noclassdeffounderror-okhttp3-okhttpclientbuilder
* 2017-3-31
    *  增加单次批量上传文件功能：一次请求上传多个文件
* 2017-4-21
    *  增加二进制流请求功能，DEMO中已添加动态权限申请功能
* 2017-6-1
    *  支持PUT、DELETE请求
* 2017-6-30
    *  支持异步延迟执行

## 权限
```
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

## 自定义全局配置
在Application中配置如下：
```java
String downloadFileDir = Environment.getExternalStorageDirectory().getPath()+"/okHttp_download/";
OkHttpUtil.init(context)
        .setConnectTimeout(15)//连接超时时间
        .setWriteTimeout(15)//写超时时间
        .setReadTimeout(15)//读超时时间
        .setMaxCacheSize(10 * 1024 * 1024)//缓存空间大小
        .setCacheType(CacheType.FORCE_NETWORK)//缓存类型
        .setHttpLogTAG("HttpLog")//设置请求日志标识
        .setIsGzip(false)//Gzip压缩，需要服务端支持
        .setShowHttpLog(true)//显示请求日志
        .setShowLifecycleLog(false)//显示Activity销毁日志
        .setRetryOnConnectionFailure(false)//失败后不自动重连
        .setDownloadFileDir(downloadFileDir)//文件下载保存目录
        .setResponseEncoding(Encoding.UTF_8)//设置全局的服务器响应编码
        .setRequestEncoding(Encoding.UTF_8)//设置全局的请求参数编码
        .addResultInterceptor(HttpInterceptor.ResultInterceptor)//请求结果拦截器
        .addExceptionInterceptor(HttpInterceptor.ExceptionInterceptor)//请求链路异常拦截器
        .setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))//持久化cookie
        .build();
            
```

## 获取网络请求客户端单例示例
```java
//获取单例客户端（默认）
 方法一、OkHttpUtil.getDefault(this)//绑定生命周期
            .doGetSync(HttpInfo.Builder().setUrl(url).build());
 方法二、OkHttpUtil.getDefault()//不绑定生命周期
            .doGetSync(HttpInfo.Builder().setUrl(url).build());
            
```

## 取消指定请求
建议在视图中采用OkHttpUtil.getDefault(this)的方式进行请求绑定，该方式会在Activity/Fragment销毁时自动取消当前视图下的所有请求；
请求标识类型支持Object、String、Integer、Float、Double；
**<font color=red>请求标识务必保证唯一</font>**。
```java
//*******请求时先绑定请求标识，根据该标识进行取消*******/
//方法一：
OkHttpUtil.Builder()
                .setReadTimeout(120)
                .build("请求标识")//绑定请求标识
                .doDownloadFileAsync(info);
//方法二：
OkHttpUtil.getDefault("请求标识")//绑定请求标识
            .doGetSync(info);
            
//*******取消指定请求*******/ 
OkHttpUtil.getDefault().cancelRequest("请求标识");
 
```

## HttpInfo参数解析：
* 键值对提交采用addParam/addParams方法
* Json提交采用addParamJson方法
* 表单提交采用addParamForm方法
* 二进制字节流提交采用addParamBytes方法
* 文件上传采用addDownloadFile方法
* 文件下载采用addUploadFile方法
```java
HttpInfo.Builder()
        .setUrl(url)
        .setRequestType(RequestType.GET)//请求方式
        .addHead("head","test")//添加头参数
        .addParam("param","test")//添加接口键值对参数
        .addParams(new HashMap<String, String>())//添加接口键值对参数集合
        .addParamBytes("byte")//添加二进制流
        .addParamJson("json")//添加Json参数
        .addParamFile(new File(""))//添加文档参数
        .addParamForm("form")//添加表单参数
        .addDownloadFile(new DownloadFileInfo("fileURL", "myMP4",null))//添加下载文件
        .addUploadFile("interfaceParamName","filePathWithName",null)//添加上传文件
        .setResponseEncoding(Encoding.UTF_8)//设置服务器响应编码
        .setRequestEncoding(Encoding.UTF_8)//设置全局的请求参数编码
        .setDelayExec(2, TimeUnit.SECONDS)//延迟2秒执行
        .build()
```

## 在Activity中同步调用示例
```java
    /**
     * 同步请求：由于不能在UI线程中进行网络请求操作，所以采用子线程方式
     */
    private void sync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpInfo info = HttpInfo.Builder()
                        .setUrl(url)
                        .setResponseEncoding(Encoding.UTF_8)//设置该接口的服务器响应编码
                        .setRequestEncoding(Encoding.UTF_8)//设置该接口的请求参数编码
                        .build();
                OkHttpUtil.getDefault(this)
                        .doGetSync(info);
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
    }
```

## 在Activity中异步调用示例
```java
    /**
     * 异步请求：回调方法可以直接操作UI
     */
    private void async() {
        OkHttpUtil.getDefault(this).doGetAsync(
                HttpInfo.Builder().setUrl(url).build(),
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
                        setFromCacheTV(info);
                    }
                });
    }
```

## 仅网络请求
```java
    /**
     * 仅网络请求
     */
    private void forceNetwork(){
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
    }
```

## 仅缓存请求
```java
    /**
     * 仅缓存请求
     */
    private void forceCache(){
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
    }
```

## 先网络再缓存
```java
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
    }
```

## 先缓存再网络
```java
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
    }
```

## 缓存10秒失效
```java
    /**
     * 缓存10秒失效：连续点击进行测试10秒内再次请求为缓存响应，10秒后再请求则缓存失效并进行网络请求
     */
    private void tenSecondCache(){
        //由于采用同一个url测试，需要先清理缓存
        if(isNeedDeleteCache){
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
```

## 在Activity中上传图片示例
```java
 /**
     * 异步上传图片：显示上传进度
     */
    private void doUploadImg() {
        HttpInfo info = HttpInfo.Builder()
                        .setUrl(url)
                        .addUploadFile("file", filePathOne, new ProgressCallback() {
                            //onProgressMain为UI线程回调，可以直接操作UI
                            @Override
                            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                                uploadProgressOne.setProgress(percent);
                                LogUtil.d(TAG, "上传进度：" + percent);
                            }
                        })
                        .build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
    }
```

## 在Activity中单次批量上传文件示例
* 若服务器为php，接口文件参数名称后面追加"[]"表示数组，
示例：builder.addUploadFile("<font color=red>uploadFile[]</font>",path);
```java
/**
     * 单次批量上传：一次请求上传多个文件
     */
     private void doUploadBatch(){
        imgList.clear();
        imgList.add("/storage/emulated/0/okHttp_download/test.apk");
        imgList.add("/storage/emulated/0/okHttp_download/test.rar");
        HttpInfo.Builder builder = HttpInfo.Builder()
                .setUrl(url);
        //循环添加上传文件
        for (String path: imgList  ) {
            //若服务器为php，接口文件参数名称后面追加"[]"表示数组，示例：builder.addUploadFile("uploadFile[]",path);
            builder.addUploadFile("uploadFile",path);
        }
        HttpInfo info = builder.build();
        OkHttpUtil.getDefault(UploadFileActivity.this).doUploadFileAsync(info,new ProgressCallback(){
            @Override
            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                uploadProgress.setProgress(percent);
            }

            @Override
            public void onResponseMain(String filePath, HttpInfo info) {
                LogUtil.d(TAG, "上传结果：" + info.getRetDetail());
                tvResult.setText(info.getRetDetail());
            }
        });
    }
```

## 在Activity中断点下载文件示例
```java
 @OnClick({R.id.downloadBtn, R.id.pauseBtn, R.id.continueBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.downloadBtn://下载
                download();
                break;
            case R.id.pauseBtn://暂停下载
                if(null != fileInfo)
                    fileInfo.setDownloadStatus(DownloadStatus.PAUSE);
                break;
            case R.id.continueBtn://继续下载
                download();
                break;
        }
    }

    private void download(){
        if(null == fileInfo)
            fileInfo = new DownloadFileInfo(url,"fileName",new ProgressCallback(){
                @Override
                public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                    downloadProgress.setProgress(percent);
                    tvResult.setText(percent+"%");
                    LogUtil.d(TAG, "下载进度：" + percent);
                }
                @Override
                public void onResponseMain(String filePath, HttpInfo info) {
                    if(info.isSuccessful()){
                        tvResult.setText(info.getRetDetail()+"\n下载状态："+fileInfo.getDownloadStatus());
                    }else{
                        Toast.makeText(DownloadBreakpointsActivity.this,info.getRetDetail(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        HttpInfo info = HttpInfo.Builder().addDownloadFile(fileInfo).build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
    }
```

## 二进制流方式请求
```java
HttpInfo info = new HttpInfo.Builder()
        .setUrl("http://192.168.120.154:8082/StanClaimProd-app/surveySubmit/getFileLen")
        .addParamBytes(byte)//添加二进制流
        .build();
OkHttpUtil.getDefault().doPostAsync(info, new Callback() {
    @Override
    public void onSuccess(HttpInfo info) throws IOException {
        String result = info.getRetDetail();
        resultTV.setText("请求失败："+result);
    }

    @Override
    public void onFailure(HttpInfo info) throws IOException {
        resultTV.setText("请求成功："+info.getRetDetail());
    }
});
```

## 请求结果统一预处理拦截器/请求链路异常信息拦截器示例
请求结果拦截器与链路异常拦截器方便项目进行网络请求业务时对信息返回的统一管理与设置
```java
/**
 * Http拦截器
 * 1、请求结果统一预处理拦截器
 * 2、请求链路异常信息拦截器
 * @author zhousf
 */
public class HttpInterceptor {

    /**
     * 请求结果统一预处理拦截器
     * 该拦截器会对所有网络请求返回结果进行预处理并修改
     */
    public static ResultInterceptor ResultInterceptor = new ResultInterceptor() {
        @Override
        public HttpInfo intercept(HttpInfo info) throws Exception {
            //请求结果预处理：可以进行GSon过滤与解析
            return info;
        }
    };

    /**
     * 请求链路异常信息拦截器
     * 该拦截器会发送网络请求时链路异常信息进行拦截处理
     */
    public static ExceptionInterceptor ExceptionInterceptor = new ExceptionInterceptor() {
        @Override
        public HttpInfo intercept(HttpInfo info) throws Exception {
            switch (info.getRetCode()){
                case HttpInfo.NonNetwork:
                    info.setRetDetail("网络中断");
                    break;
                case HttpInfo.CheckURL:
                    info.setRetDetail("网络地址错误["+info.getNetCode()+"]");
                    break;
                case HttpInfo.ProtocolException:
                    info.setRetDetail("协议类型错误["+info.getNetCode()+"]");
                    break;
                case HttpInfo.CheckNet:
                    info.setRetDetail("请检查网络连接是否正常["+info.getNetCode()+"]");
                    break;
                case HttpInfo.ConnectionTimeOut:
                    info.setRetDetail("连接超时");
                    break;
                case HttpInfo.WriteAndReadTimeOut:
                    info.setRetDetail("读写超时");
                    break;
                case HttpInfo.ConnectionInterruption:
                    info.setRetDetail("连接中断");
                    break;
            }
            return info;
        }
    };
}

```

## Cookie持久化示例
没有在Application中进行全局Cookie持久化配置时可以采用以下方式：
```java
OkHttpUtilInterface okHttpUtil = OkHttpUtil.Builder()
            .setCacheLevel(FIRST_LEVEL)
            .setConnectTimeout(25).build(this);
//一个okHttpUtil即为一个网络连接
okHttpUtil.doGetAsync(
                HttpInfo.Builder().setUrl(url).build(),
                new CallbackOk() {
                    @Override
                    public void onResponse(HttpInfo info) throws IOException {
                        if (info.isSuccessful()) {
                            String result = info.getRetDetail();
                            resultTV.setText("异步请求："+result);
                        }
                    }
                });
```

## 有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流

* QQ: 424427633


## 感激
感谢以下的项目,排名不分先后

* [OkHttp](https://github.com/square/okhttp/) 
* [PersistentCookieJar](https://github.com/franmontiel/PersistentCookieJar)


## 相关示例

### OkHttpUtil接口
```java
/**
 * 网络请求工具接口
 * @author zhousf
 */
public interface OkHttpUtilInterface {


    /**
     * 同步Post请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doPostSync(HttpInfo info);

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @param callback 进度回调接口
     * @return HttpInfo
     */
    HttpInfo doPostSync(HttpInfo info, ProgressCallback callback);

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doPostAsync(HttpInfo info, BaseCallback callback);

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    void doPostAsync(HttpInfo info, ProgressCallback callback);

    /**
     * 同步Get请求
     * @param info 请求信息体
     */
    HttpInfo doGetSync(HttpInfo info);

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doGetAsync(HttpInfo info, BaseCallback callback);

    /**
     * 异步上传文件
     * @param info 请求信息体
     */
    void doUploadFileAsync(final HttpInfo info);

    /**
     * 批量异步上传文件
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    void doUploadFileAsync(final HttpInfo info, ProgressCallback callback);

    /**
     * 同步上传文件
     * @param info 请求信息体
     */
    void doUploadFileSync(final HttpInfo info);

    /**
     * 批量同步上传文件
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    void doUploadFileSync(final HttpInfo info, ProgressCallback callback);

    /**
     * 异步下载文件
     * @param info 请求信息体
     */
    void doDownloadFileAsync(final HttpInfo info);


    /**
     * 同步下载文件
     * @param info 请求信息体
     */
    void doDownloadFileSync(final HttpInfo info);

    /**
     * 同步Delete请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doDeleteSync(HttpInfo info);

    /**
     * 异步Delete请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doDeleteAsync(HttpInfo info, BaseCallback callback);

    /**
     * 同步Put请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    HttpInfo doPutSync(HttpInfo info);

    /**
     * 异步PUT请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    void doPutAsync(HttpInfo info, BaseCallback callback);

    /**
     * 取消请求
     * @param requestTag 请求标识
     */
    void cancelRequest(Object requestTag);


    /**
     * 获取默认的HttpClient
     */
    OkHttpClient getDefaultClient();

    /**
     * 清理缓存：只清理网络请求的缓存，不清理下载文件
     */
    boolean deleteCache();

}

```

### MainActivity
```java

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
    private String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
    
    private boolean isNeedDeleteCache = true;

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
                        .setResponseEncoding(Encoding.UTF_8)//设置服务器响应编码
                        .build();
                OkHttpUtil.getDefault(this)
                        .doGetSync(info);
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
        OkHttpUtil.getDefault(this).doGetAsync(
                HttpInfo.Builder().setUrl(url).build(),
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
                        setFromCacheTV(info);
                    }
                });
        needDeleteCache(true);
    }

    /**
     * 仅网络请求
     */
    private void forceNetwork(){
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
    private void forceCache(){
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
    private void tenSecondCache(){
        //由于采用同一个url测试，需要先清理缓存
        if(isNeedDeleteCache){
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


    private void needDeleteCache(boolean delete){
        isNeedDeleteCache = delete;
    }

    private void setFromCacheTV(HttpInfo info){
        fromCacheTV.setText(info.isFromCache()?"缓存请求":"网络请求");
    }

    /**
     * 清理缓存
     */
    private void deleteCache(){
        if(OkHttpUtil.getDefault().deleteCache()){
            ToastUtil.show(this,"清理缓存成功");
        }else{
            ToastUtil.show(this,"清理缓存失败");
        }
    }



}
```
