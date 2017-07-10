package com.okhttplib;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.Encoding;
import com.okhttplib.annotation.RequestType;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.BaseCallback;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.helper.HelperInfo;
import com.okhttplib.helper.OkHttpHelper;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import static com.okhttplib.annotation.CacheType.FORCE_NETWORK;


/**
 * 网络请求工具类:
 * 支持Http/Https等协议
 * 支持Cookie持久化
 * 支持Gzip压缩
 * 支持协议头参数Head设置、二进制参数请求
 * 支持Unicode自动转码、服务器响应编码设置
 * 支持同步/异步请求
 * 支持异步延迟执行
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
 *
 * 引入版本com.squareup.okhttp3:okhttp:3.7.0
 * @author zhousf
 */
public class OkHttpUtil implements OkHttpUtilInterface{

    private final String TAG = getClass().getSimpleName();
    private static Context context;
    private static Builder builderGlobal;
    private static OkHttpClient httpClient;
    private static ScheduledExecutorService executorService;
    private Builder builder;
    private int cacheSurvivalTime = 0;//缓存存活时间（秒）
    private @CacheType int cacheType = FORCE_NETWORK;//缓存类型

    /**
     * 初始化：请在Application中调用
     * @param context 上下文
     */
    public static Builder init(Context context){
        OkHttpUtil.context = context;
        ((Application)OkHttpUtil.context).registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallbacks());
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
     * 同步请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doSync(HttpInfo info) {
        return OkHttpHelper.Builder()
                .httpInfo(info)
                .requestType(info.getRequestType())
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestSync();
    }

    /**
     * 异步请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    @Override
    public void doAsync(final HttpInfo info, final BaseCallback callback) {
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .requestType(info.getRequestType())
                        .callback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .doRequestAsync();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
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
                .requestType(RequestType.POST)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestSync();
    }

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @param callback 进度回调接口
     * @return HttpInfo
     */
    @Override
    public HttpInfo doPostSync(HttpInfo info, ProgressCallback callback){
        return OkHttpHelper.Builder()
                .httpInfo(info)
                .requestType(RequestType.POST)
                .progressCallback(callback)
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
    public void doPostAsync(final HttpInfo info,final BaseCallback callback){
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .requestType(RequestType.POST)
                        .callback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .doRequestAsync();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
    }

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 进度回调接口
     */
    @Override
    public void doPostAsync(final HttpInfo info,final ProgressCallback callback) {
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .requestType(RequestType.POST)
                        .progressCallback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .doRequestAsync();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
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
                .requestType(RequestType.GET)
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
    public void doGetAsync(final HttpInfo info,final BaseCallback callback) {
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .requestType(RequestType.GET)
                        .callback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .doRequestAsync();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
    }

    /**
     * 异步上传文件
     * @param info 请求信息体
     */
    @Override
    public void doUploadFileAsync(final HttpInfo info){
        List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        for(final UploadFileInfo fileInfo : uploadFiles){
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    OkHttpHelper.Builder()
                            .httpInfo(info)
                            .uploadFileInfo(fileInfo)
                            .requestType(RequestType.POST)
                            .helperInfo(packageHelperInfo())
                            .build()
                            .uploadFile();
                }
            },info.getDelayExecTime(),info.getDelayExecUnit());
        }
    }

    /**
     * 批量异步上传文件
     * @param info 请求信息体
     */
    @Override
    public void doUploadFileAsync(final HttpInfo info, final ProgressCallback callback){
        final List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .uploadFileInfoList(uploadFiles)
                        .requestType(RequestType.POST)
                        .progressCallback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .uploadFile();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
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
                    .requestType(RequestType.POST)
                    .helperInfo(packageHelperInfo())
                    .build()
                    .uploadFile();
        }
    }

    /**
     * 批量同步上传文件
     * @param info 请求信息体
     */
    @Override
    public void doUploadFileSync(final HttpInfo info, final ProgressCallback callback){
        final List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        OkHttpHelper.Builder()
                .httpInfo(info)
                .uploadFileInfoList(uploadFiles)
                .requestType(RequestType.POST)
                .progressCallback(callback)
                .helperInfo(packageHelperInfo())
                .build()
                .uploadFile();
    }

    /**
     * 异步下载文件
     * @param info 请求信息体
     */
    @Override
    public void doDownloadFileAsync(final HttpInfo info){
        List<DownloadFileInfo> downloadFiles = info.getDownloadFiles();
        for(final DownloadFileInfo fileInfo : downloadFiles){
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    OkHttpHelper.Builder()
                            .httpInfo(info)
                            .downloadFileInfo(fileInfo)
                            .requestType(RequestType.GET)
                            .clientBuilder(newBuilderFromCopy())
                            .helperInfo(packageHelperInfo())
                            .build()
                            .downloadFile();
                }
            },info.getDelayExecTime(),info.getDelayExecUnit());
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
                    .requestType(RequestType.GET)
                    .clientBuilder(newBuilderFromCopy())
                    .helperInfo(packageHelperInfo())
                    .build()
                    .downloadFile();
        }
    }

    /**
     * 同步Delete请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doDeleteSync(HttpInfo info){
        return OkHttpHelper.Builder()
                .httpInfo(info)
                .requestType(RequestType.DELETE)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestSync();
    }

    /**
     * 异步Delete请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    @Override
    public void doDeleteAsync(final HttpInfo info,final BaseCallback callback){
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .requestType(RequestType.DELETE)
                        .callback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .doRequestAsync();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
    }

    /**
     * 同步Put请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doPutSync(HttpInfo info){
        return OkHttpHelper.Builder()
                .httpInfo(info)
                .requestType(RequestType.PUT)
                .helperInfo(packageHelperInfo())
                .build()
                .doRequestSync();
    }

    /**
     * 异步Put请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    @Override
    public void doPutAsync(final HttpInfo info,final BaseCallback callback){
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                OkHttpHelper.Builder()
                        .httpInfo(info)
                        .requestType(RequestType.PUT)
                        .callback(callback)
                        .helperInfo(packageHelperInfo())
                        .build()
                        .doRequestAsync();
            }
        },info.getDelayExecTime(),info.getDelayExecUnit());
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

    public boolean isNetworkAvailable() {
        if(context == null)
            return true;
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
        if(null == context)
            this.cacheType = CacheType.FORCE_NETWORK;
        if(null == executorService)
            executorService = new ScheduledThreadPoolExecutor(20);
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
        int random = 1000 + (int)(Math.random()*999);
        String timeStamp = System.currentTimeMillis()+"_"+random;
        helperInfo.setTimeStamp(timeStamp);
        helperInfo.setExceptionInterceptors(builder.exceptionInterceptors);
        helperInfo.setResultInterceptors(builder.resultInterceptors);
        helperInfo.setDownloadFileDir(builder.downloadFileDir);
        helperInfo.setClientBuilder(newBuilderFromCopy());
        helperInfo.setOkHttpUtil(this);
        helperInfo.setDefault(builder.isDefault);
        helperInfo.setLogTAG(builder.httpLogTAG == null ? TAG : builder.httpLogTAG);
        helperInfo.setResponseEncoding(builder.responseEncoding);
        helperInfo.setRequestEncoding(builder.requestEncoding);
        helperInfo.setCacheSurvivalTime(cacheSurvivalTime);
        helperInfo.setCacheType(cacheType);
        helperInfo.setGzip(builder.isGzip);
        return helperInfo;
    }

    private OkHttpClient.Builder newBuilderFromCopy(){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(builder.readTimeout, TimeUnit.SECONDS)
                .writeTimeout(builder.writeTimeout, TimeUnit.SECONDS)
                .cache(new Cache(builder.cachedDir,builder.maxCacheSize))
                .retryOnConnectionFailure(builder.retryOnConnectionFailure);
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
        private @CacheType int cacheType;//缓存类型
        private boolean isGlobalConfig;//是否全局配置
        private boolean showHttpLog;//是否显示Http请求日志
        private String httpLogTAG;//显示Http请求日志标识
        private boolean showLifecycleLog;//是否显示ActivityLifecycle日志
        private String downloadFileDir;//下载文件保存目录
        private String requestTag;
        private CookieJar cookieJar;
        private boolean isDefault;//是否默认请求
        private @Encoding String responseEncoding;//服务器响应编码
        private @Encoding String requestEncoding;//请求参数应编码
        private boolean isGzip = false;//Gzip压缩

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
            if(null != context){
                setCachedDir(context.getExternalCacheDir());
            }else{
                setCachedDir(Environment.getExternalStorageDirectory());
            }
            setConnectTimeout(30);
            setReadTimeout(30);
            setWriteTimeout(30);
            setRetryOnConnectionFailure(true);
            setCacheSurvivalTime(0);
            setCacheType(FORCE_NETWORK);
            setNetworkInterceptors(null);
            setInterceptors(null);
            setResultInterceptors(null);
            setExceptionInterceptors(null);
            setShowHttpLog(true);
            setShowLifecycleLog(false);
            setDownloadFileDir(Environment.getExternalStorageDirectory().getPath()+"/okHttp_download/");
            setIsGzip(false);
            setResponseEncoding(Encoding.UTF_8);
            setRequestEncoding(Encoding.UTF_8);
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
            setNetworkInterceptors(builder.networkInterceptors);
            setInterceptors(builder.interceptors);
            setResultInterceptors(builder.resultInterceptors);
            setExceptionInterceptors(builder.exceptionInterceptors);
            setShowHttpLog(builder.showHttpLog);
            setHttpLogTAG(builder.httpLogTAG);
            setShowLifecycleLog(builder.showLifecycleLog);
            if(!TextUtils.isEmpty(builder.downloadFileDir)){
                setDownloadFileDir(builder.downloadFileDir);
            }
            setCookieJar(builder.cookieJar);
            setResponseEncoding(builder.responseEncoding);
            setRequestEncoding(builder.requestEncoding);
            setIsGzip(builder.isGzip);
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

        //设置连接超时（单位：秒）
        public Builder setConnectTimeout(int connectTimeout) {
            if(connectTimeout <= 0)
                throw new IllegalArgumentException("connectTimeout must be > 0");
            this.connectTimeout = connectTimeout;
            return this;
        }

        //设置读超时（单位：秒）
        public Builder setReadTimeout(int readTimeout) {
            if(readTimeout <= 0)
                throw new IllegalArgumentException("readTimeout must be > 0");
            this.readTimeout = readTimeout;
            return this;
        }

        //设置写超时（单位：秒）
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

        //设置显示Http请求日志
        public Builder setShowHttpLog(boolean showHttpLog) {
            this.showHttpLog = showHttpLog;
            return this;
        }

        //设置Http请求日志标识
        public Builder setHttpLogTAG(String logTAG){
            this.httpLogTAG = logTAG;
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

        //设置服务器响应编码（默认：UTF-8）
        public Builder setResponseEncoding(@Encoding String responseEncoding) {
            this.responseEncoding = responseEncoding;
            return this;
        }

        //设置请求参数编码（默认：UTF-8）
        public Builder setRequestEncoding(@Encoding String requestEncoding){
            this.requestEncoding = requestEncoding;
            return this;
        }

        //Gzip压缩，需要服务端支持
        public Builder setIsGzip(boolean isGzip){
            this.isGzip = isGzip;
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

    @Override
    public boolean deleteCache() {
        try {
            if(httpClient != null && httpClient.cache() != null)
            httpClient.cache().delete();
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
