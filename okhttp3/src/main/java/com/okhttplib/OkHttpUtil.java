package com.okhttplib;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

import com.okhttplib.annotation.CacheLevel;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.RequestMethod;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.helper.HelperInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.CallbackOk;
import com.okhttplib.helper.OkHttpHelper;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.okhttplib.annotation.CacheLevel.FIRST_LEVEL;
import static com.okhttplib.annotation.CacheLevel.FOURTH_LEVEL;
import static com.okhttplib.annotation.CacheLevel.SECOND_LEVEL;
import static com.okhttplib.annotation.CacheLevel.THIRD_LEVEL;
import static com.okhttplib.annotation.CacheType.CACHE_THEN_NETWORK;
import static com.okhttplib.annotation.CacheType.FORCE_CACHE;
import static com.okhttplib.annotation.CacheType.FORCE_NETWORK;
import static com.okhttplib.annotation.CacheType.NETWORK_THEN_CACHE;


/**
 * 网络请求工具类
 * 1、同步/异步，GET/POST网络请求，缓存响应
 * 2、http/https
 * 3、当Activity/Fragment销毁时自动取消相应的所有网络请求
 * 4、自动切换UI线程，摒弃runOnUiThread
 * 5、Application中自定义全局配置/增加系统默认配置
 * 6、文件和图片上传/批量上传，支持同步/异步上传，支持进度提示
 * 7、文件断点下载，独立下载的模块摒弃了数据库记录断点
 * 8、日志跟踪与异常处理
 * 9、支持请求结果拦截以及异常处理拦截
 * 10、支持Cookie持久化
 * 11、支持协议头参数Head设置
 *
 * 引入版本com.squareup.okhttp3:okhttp:3.4.1
 * @author zhousf
 */
public class OkHttpUtil implements OkHttpUtilInterface{

    private final String TAG = getClass().getSimpleName();
    private static Application application;
    private static Builder builderGlobal;
    private static OkHttpClient httpClient;
    private static ExecutorService executorService;
    private Builder builder;
    private int cacheSurvivalTime;//缓存存活时间（秒）
    private int cacheType;//缓存类型

    /**
     * 初始化：请在Application中调用
     * @param context 上下文
     */
    public static Builder init(Application context){
        application = context;
        application.registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallbacks());
        return BuilderGlobal();
    }

    /**
     * 获取默认请求配置
     * @return OkHttpUtil
     */
    public static OkHttpUtilInterface getDefault(){
        return new Builder(false).isDefault(true).build();
    }

    /**
     * 获取默认请求配置：该方法会绑定Activity、fragment生命周期
     * @param requestTag 请求标识
     * @return OkHttpUtil
     */
    public static OkHttpUtilInterface getDefault(Object requestTag){
        return new Builder(false).isDefault(true).build(requestTag);
    }

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doPostSync(HttpInfo info){
        return OkHttpHelper.Builder()
                .httpInfo(info)
                .requestMethod(RequestMethod.POST)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestSync();
    }

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    @Override
    public void doPostAsync(HttpInfo info, CallbackOk callback){
        OkHttpHelper.Builder()
                .httpInfo(info)
                .requestMethod(RequestMethod.POST)
                .callbackOk(callback)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestAsync();
    }

    /**
     * 同步Get请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doGetSync(HttpInfo info){
        return OkHttpHelper.Builder()
                .httpInfo(info)
                .requestMethod(RequestMethod.GET)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestSync();
    }

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    @Override
    public void doGetAsync(HttpInfo info, CallbackOk callback){
        OkHttpHelper.Builder()
                .httpInfo(info)
                .requestMethod(RequestMethod.GET)
                .callbackOk(callback)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestAsync();
    }

    /**
     * 异步上传文件
     * @param info 请求信息体
     */
    @Override
    public void doUploadFileAsync(final HttpInfo info){
        List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        for(final UploadFileInfo fileInfo : uploadFiles){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    OkHttpHelper.Builder()
                            .httpInfo(info)
                            .uploadFileInfo(fileInfo)
                            .requestMethod(RequestMethod.POST)
                            .helperInfo(packageHelperInfo())
                            .build()
                            .uploadFile();
                }
            });
        }
    }

    /**
     * 同步上传文件
     * @param info 请求信息体
     */
    @Override
    public void doUploadFileSync(final HttpInfo info){
        List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        for(final UploadFileInfo fileInfo : uploadFiles){
            OkHttpHelper.Builder()
                    .httpInfo(info)
                    .uploadFileInfo(fileInfo)
                    .requestMethod(RequestMethod.POST)
                    .helperInfo(packageHelperInfo())
                    .build()
                    .uploadFile();
        }
    }

    /**
     * 异步下载文件
     * @param info 请求信息体
     */
    @Override
    public void doDownloadFileAsync(final HttpInfo info){
        List<DownloadFileInfo> downloadFiles = info.getDownloadFiles();
        for(final DownloadFileInfo fileInfo : downloadFiles){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    OkHttpHelper.Builder()
                            .httpInfo(info)
                            .downloadFileInfo(fileInfo)
                            .requestMethod(RequestMethod.GET)
                            .clientBuilder(newBuilderFromCopy())
                            .helperInfo(packageHelperInfo())
                            .build()
                            .downloadFile();
                }
            });
        }
    }

    /**
     * 同步下载文件
     * @param info 请求信息体
     */
    @Override
    public void doDownloadFileSync(final HttpInfo info){
        List<DownloadFileInfo> downloadFiles = info.getDownloadFiles();
        for(final DownloadFileInfo fileInfo : downloadFiles){
            OkHttpHelper.Builder()
                    .httpInfo(info)
                    .downloadFileInfo(fileInfo)
                    .requestMethod(RequestMethod.GET)
                    .clientBuilder(newBuilderFromCopy())
                    .helperInfo(packageHelperInfo())
                    .build()
                    .downloadFile();
        }
    }

    /**
     * 取消请求
     * @param requestTag 请求标识
     */
    @Override
    public void cancelRequest(Object requestTag) {
        BaseActivityLifecycleCallbacks.cancel(parseRequestTag(requestTag));
    }

    @Override
    public OkHttpClient getDefaultClient() {
        return httpClient;
    }

    public void setDefaultClient(OkHttpClient client){
        httpClient = client;
    }

    /**
     * 网络请求拦截器
     */
    private Interceptor CACHE_CONTROL_NETWORK_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response.Builder resBuilder = chain.proceed(chain.request()).newBuilder();
            resBuilder.removeHeader("Pragma")
                    .header("Cache-Control", String.format("max-age=%d", cacheSurvivalTime));
            return resBuilder.build();
        }
    };

    /**
     * 缓存应用拦截器
     */
    private Interceptor CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            switch (cacheType){
                case FORCE_CACHE:
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    break;
                case FORCE_NETWORK:
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                    break;
                case NETWORK_THEN_CACHE:
                    if(!isNetworkAvailable(application)){
                        request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    }else {
                        request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                    }
                    break;
                case CACHE_THEN_NETWORK:
                    if(!isNetworkAvailable(application)){
                        request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    }
                    break;
            }
            return chain.proceed(request);
        }
    };

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.getState() == NetworkInfo.State.CONNECTED;
    }

    private OkHttpUtil(Builder builder) {
        //初始化参数
        this.builder = builder;
        this.cacheType = builder.cacheType;
        this.cacheSurvivalTime = builder.cacheSurvivalTime;
        if(this.cacheSurvivalTime == 0){
            final int deviation = 5;
            switch (builder.cacheLevel){
                case FIRST_LEVEL:
                    this.cacheSurvivalTime = 0;
                    break;
                case SECOND_LEVEL:
                    this.cacheSurvivalTime = 15 + deviation;
                    break;
                case THIRD_LEVEL:
                    this.cacheSurvivalTime = 30 + deviation;
                    break;
                case FOURTH_LEVEL:
                    this.cacheSurvivalTime = 60 + deviation;
                    break;
            }
        }
        if(this.cacheSurvivalTime > 0)
            this.cacheType = CACHE_THEN_NETWORK;
        if(null == application)
            this.cacheType = FORCE_NETWORK;
        if(null == executorService)
            executorService = Executors.newCachedThreadPool();
        BaseActivityLifecycleCallbacks.setShowLifecycleLog(builder.showLifecycleLog);
        if(builder.isGlobalConfig){
            OkHttpHelper.Builder()
                    .helperInfo(packageHelperInfo())
                    .build();
        }
    }

    /**
     * 封装业务类信息
     */
    private HelperInfo packageHelperInfo(){
        HelperInfo helperInfo = new HelperInfo();
        helperInfo.setShowHttpLog(builder.showHttpLog);
        helperInfo.setRequestTag(builder.requestTag);
        helperInfo.setTimeStamp(System.currentTimeMillis());
        helperInfo.setExceptionInterceptors(builder.exceptionInterceptors);
        helperInfo.setResultInterceptors(builder.resultInterceptors);
        helperInfo.setDownloadFileDir(builder.downloadFileDir);
        helperInfo.setClientBuilder(newBuilderFromCopy());
        helperInfo.setOkHttpUtil(this);
        helperInfo.setDefault(builder.isDefault);
        helperInfo.setLogTAG(TAG);
        return helperInfo;
    }

    private OkHttpClient.Builder newBuilderFromCopy(){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(builder.readTimeout, TimeUnit.SECONDS)
                .writeTimeout(builder.writeTimeout, TimeUnit.SECONDS)
                .cache(new Cache(builder.cachedDir,builder.maxCacheSize))
                .retryOnConnectionFailure(builder.retryOnConnectionFailure)
                .addInterceptor(CACHE_CONTROL_INTERCEPTOR)
                .addNetworkInterceptor(CACHE_CONTROL_NETWORK_INTERCEPTOR);
        if(null != builder.networkInterceptors && !builder.networkInterceptors.isEmpty())
            clientBuilder.networkInterceptors().addAll(builder.networkInterceptors);
        if(null != builder.interceptors && !builder.interceptors.isEmpty())
            clientBuilder.interceptors().addAll(builder.interceptors);
        if(null != builder.cookieJar)
            clientBuilder.cookieJar(builder.cookieJar);
        return clientBuilder;
    }

    public static Builder Builder() {
        return new Builder(false);
    }

    private static Builder BuilderGlobal() {
        return new Builder(true).isDefault(true);
    }

    public static final class Builder {

        private int maxCacheSize;//缓存大小
        private File cachedDir;//缓存目录
        private int connectTimeout;//连接超时
        private int readTimeout;//读超时
        private int writeTimeout;//写超时
        private boolean retryOnConnectionFailure;//失败重新连接
        private List<Interceptor> networkInterceptors;//网络拦截器
        private List<Interceptor> interceptors;//应用拦截器
        private List<ResultInterceptor> resultInterceptors;//请求结果拦截器
        private List<ExceptionInterceptor> exceptionInterceptors;//请求链路异常拦截器
        private int cacheSurvivalTime;//缓存存活时间（秒）
        private int cacheType;//缓存类型
        private int cacheLevel;//缓存级别
        private boolean isGlobalConfig;//是否全局配置
        private boolean showHttpLog;//是否显示Http请求日志
        private boolean showLifecycleLog;//是否显示ActivityLifecycle日志
        private String downloadFileDir;//下载文件保存目录
        private String requestTag;
        private CookieJar cookieJar;
        private boolean isDefault;//是否默认请求

        public Builder() {
        }

        public Builder(boolean isGlobal) {
            isGlobalConfig = isGlobal;
            //系统默认配置
            initDefaultConfig();
            if(!isGlobal && null != builderGlobal){
                //全局自定义配置
                initGlobalConfig(builderGlobal);
            }
        }

        public OkHttpUtilInterface build(){
            return build(null);
        }

        public OkHttpUtilInterface build(Object requestTag) {
            if(isGlobalConfig && null == builderGlobal){
                builderGlobal = this;
            }
            if(null != requestTag)
                setRequestTag(requestTag);
            return new OkHttpUtil(this);
        }

        /**
         * 系统默认配置
         */
        private void initDefaultConfig(){
            setMaxCacheSize(10 * 1024 * 1024);
            if(null != application){
                setCachedDir(application.getExternalCacheDir());
            }else{
                setCachedDir(Environment.getExternalStorageDirectory());
            }
            setConnectTimeout(30);
            setReadTimeout(30);
            setWriteTimeout(30);
            setRetryOnConnectionFailure(true);
            setCacheSurvivalTime(0);
            setCacheType(CACHE_THEN_NETWORK);
            setCacheLevel(FIRST_LEVEL);
            setNetworkInterceptors(null);
            setInterceptors(null);
            setResultInterceptors(null);
            setExceptionInterceptors(null);
            setShowHttpLog(true);
            setShowLifecycleLog(false);
            setDownloadFileDir(Environment.getExternalStorageDirectory().getPath()+"/okHttp_download/");
        }

        /**
         * 全局自定义配置
         * @param builder builder
         */
        private void initGlobalConfig(Builder builder){
            setMaxCacheSize(builder.maxCacheSize);
            setCachedDir(builder.cachedDir);
            setConnectTimeout(builder.connectTimeout);
            setReadTimeout(builder.readTimeout);
            setWriteTimeout(builder.writeTimeout);
            setRetryOnConnectionFailure(builder.retryOnConnectionFailure);
            setCacheSurvivalTime(builder.cacheSurvivalTime);
            setCacheType(builder.cacheType);
            setCacheLevel(builder.cacheLevel);
            setNetworkInterceptors(builder.networkInterceptors);
            setInterceptors(builder.interceptors);
            setResultInterceptors(builder.resultInterceptors);
            setExceptionInterceptors(builder.exceptionInterceptors);
            setShowHttpLog(builder.showHttpLog);
            setShowLifecycleLog(builder.showLifecycleLog);
            if(!TextUtils.isEmpty(builder.downloadFileDir)){
                setDownloadFileDir(builder.downloadFileDir);
            }
            setCookieJar(builder.cookieJar);
        }

        private Builder isDefault(boolean isDefault){
            this.isDefault = isDefault;
            return this;
        }

        //设置缓存大小
        public Builder setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        //设置缓存目录
        public Builder setCachedDir(File cachedDir) {
            if(null != cachedDir)
                this.cachedDir = cachedDir;
            return this;
        }

        //设置连接超时
        public Builder setConnectTimeout(int connectTimeout) {
            if(connectTimeout <= 0)
                throw new IllegalArgumentException("connectTimeout must be > 0");
            this.connectTimeout = connectTimeout;
            return this;
        }

        //设置读超时
        public Builder setReadTimeout(int readTimeout) {
            if(readTimeout <= 0)
                throw new IllegalArgumentException("readTimeout must be > 0");
            this.readTimeout = readTimeout;
            return this;
        }

        //设置写超时
        public Builder setWriteTimeout(int writeTimeout) {
            if(writeTimeout <= 0)
                throw new IllegalArgumentException("writeTimeout must be > 0");
            this.writeTimeout = writeTimeout;
            return this;
        }

        //设置失败重新连接
        public Builder setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        //设置网络拦截器：每次Http请求时都会执行该拦截器
        public Builder setNetworkInterceptors(List<Interceptor> networkInterceptors) {
            if(null != networkInterceptors)
                this.networkInterceptors = networkInterceptors;
            return this;
        }

        //设置应用拦截器：每次Http、缓存请求时都会执行该拦截器
        public Builder setInterceptors(List<Interceptor> interceptors) {
            if(null != interceptors)
                this.interceptors = interceptors;
            return this;
        }

        //设置请求结果拦截器
        public Builder setResultInterceptors(List<ResultInterceptor> resultInterceptors){
            if(null != resultInterceptors)
                this.resultInterceptors = resultInterceptors;
            return this;
        }

        public Builder addResultInterceptor(ResultInterceptor resultInterceptor){
            if(null != resultInterceptor){
                if(null == this.resultInterceptors)
                    this.resultInterceptors = new ArrayList<>();
                this.resultInterceptors.add(resultInterceptor);
            }
            return this;
        }

        //设置请求链路异常拦截器
        public Builder setExceptionInterceptors(List<ExceptionInterceptor> exceptionInterceptors){
            if(null != exceptionInterceptors){
                this.exceptionInterceptors = exceptionInterceptors;
            }
            return this;
        }

        public Builder addExceptionInterceptor(ExceptionInterceptor exceptionInterceptor){
            if(null != exceptionInterceptor){
                if(null == this.exceptionInterceptors)
                    this.exceptionInterceptors = new ArrayList<>();
                this.exceptionInterceptors.add(exceptionInterceptor);
            }
            return this;
        }

        //设置缓存存活时间（秒）
        public Builder setCacheSurvivalTime(int cacheSurvivalTime) {
            if(cacheSurvivalTime < 0)
                throw new IllegalArgumentException("cacheSurvivalTime must be >= 0");
            this.cacheSurvivalTime = cacheSurvivalTime;
            return this;
        }

        //设置缓存类型
        public Builder setCacheType(@CacheType int cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        //设置缓存级别
        public Builder setCacheLevel(@CacheLevel int cacheLevel) {
            this.cacheLevel = cacheLevel;
            return this;
        }

        //设置显示Http请求日志
        public Builder setShowHttpLog(boolean showHttpLog) {
            this.showHttpLog = showHttpLog;
            return this;
        }

        //设置显示ActivityLifecycle日志
        public Builder setShowLifecycleLog(boolean showLifecycleLog) {
            this.showLifecycleLog = showLifecycleLog;
            return this;
        }

        //设置请求标识（与Activity/Fragment生命周期绑定）
        public Builder setRequestTag(Object requestTag) {
            this.requestTag = parseRequestTag(requestTag);
            return this;
        }

        //设置下载文件目录
        public Builder setDownloadFileDir(String downloadFileDir) {
            this.downloadFileDir = downloadFileDir;
            return this;
        }

        //设置cookie持久化
        public Builder setCookieJar(CookieJar cookieJar) {
            if (null != cookieJar)
                this.cookieJar = cookieJar;
            return this;
        }
    }

    private static String parseRequestTag(Object object){
        String requestTag = null;
        if(null != object){
            requestTag = object.getClass().getName();
            if(requestTag.contains("$")){
                requestTag = requestTag.substring(0,requestTag.indexOf("$"));
            }
            if(object instanceof String
                    || object instanceof Float
                    || object instanceof Double
                    || object instanceof Integer){
                requestTag = String.valueOf(object);
            }
        }
        return requestTag;
    }


}
