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
import com.okhttplib.bean.HelperInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.CallbackOk;
import com.okhttplib.helper.DownUpLoadHelper;
import com.okhttplib.helper.HttpHelper;
import com.okhttplib.helper.LogHelper;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import static com.okhttplib.helper.DownUpLoadHelper.downloadFile;
import static com.okhttplib.helper.DownUpLoadHelper.uploadFile;


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
    private static ExecutorService executorService;

    /********  构建属性-定义开始  ***********/
    private int maxCacheSize;//缓存大小
    private File cachedDir;//缓存目录
    private int connectTimeout;//连接超时
    private int readTimeout;//读超时
    private int writeTimeout;//写超时
    private int cacheSurvivalTime;//缓存存活时间（秒）
    private int cacheType;//缓存类型
    private boolean retryOnConnectionFailure;//失败重新连接
    private List<Interceptor> networkInterceptors;//网络拦截器
    private List<Interceptor> interceptors;//应用拦截器
    private List<ResultInterceptor> resultInterceptors;//请求结果拦截器
    private List<ExceptionInterceptor> exceptionInterceptors;//请求链路异常拦截器
    private int cacheLevel;//缓存级别
    private boolean showHttpLog;//是否显示Http请求日志
    private boolean showLifecycleLog;//是否显示ActivityLifecycle日志
    private String downloadFileDir;//下载文件保存目录
    private Class<?> requestTag;//请求标识
    private CookieJar cookieJar;
    /********  构建属性-定义结束  ***********/

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
        return new Builder(false).build();
    }

    /**
     * 获取默认请求配置：该方法会绑定Activity、fragment生命周期
     * @param object 请求标识
     * @return OkHttpUtil
     */
    public static OkHttpUtilInterface getDefault(Object object){
        return new Builder(false).build(object);
    }

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doPostSync(HttpInfo info){
        return HttpHelper.doRequestSync(info, RequestMethod.POST);
    }

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    @Override
    public void doPostAsync(HttpInfo info, CallbackOk callback){
        HttpHelper.doRequestAsync(info, RequestMethod.POST, callback, null);
    }

    /**
     * 同步Get请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    @Override
    public HttpInfo doGetSync(HttpInfo info){
        return HttpHelper.doRequestSync(info, RequestMethod.GET);
    }

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    @Override
    public void doGetAsync(HttpInfo info, CallbackOk callback){
        HttpHelper.doRequestAsync(info, RequestMethod.GET, callback, null);
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
                    DownUpLoadHelper.uploadFile(info,fileInfo);
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
            uploadFile(info,fileInfo);
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
                    downloadFile(info,fileInfo,newBuilderFromCopy());
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
            DownUpLoadHelper.downloadFile(info,fileInfo,newBuilderFromCopy());
        }
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

    /**
     * 日志拦截器
     */
    private Interceptor LOG_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            long startTime = System.currentTimeMillis();
            LogHelper.get().showLog(String.format("%s-URL: %s %n",chain.request().method(),
                    chain.request().url()));
            Response res = chain.proceed(chain.request());
            long endTime = System.currentTimeMillis();
            LogHelper.get().showLog(String.format("CostTime: %.1fs", (endTime-startTime) / 1000f));
            return res;
        }
    };

    private OkHttpUtil(Builder builder) {
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
        clientBuilder.addInterceptor(LOG_INTERCEPTOR);
        setSslSocketFactory(clientBuilder);
        //初始化参数
        maxCacheSize = builder.maxCacheSize;
        cachedDir = builder.cachedDir;
        connectTimeout = builder.connectTimeout;
        readTimeout = builder.readTimeout;
        writeTimeout = builder.writeTimeout;
        retryOnConnectionFailure = builder.retryOnConnectionFailure;
        networkInterceptors = builder.networkInterceptors;
        interceptors = builder.interceptors;
        resultInterceptors = builder.resultInterceptors;
        exceptionInterceptors = builder.exceptionInterceptors;
        cacheSurvivalTime = builder.cacheSurvivalTime;
        cacheType = builder.cacheType;
        cacheLevel = builder.cacheLevel;
        showHttpLog = builder.showHttpLog;
        showLifecycleLog = builder.showLifecycleLog;
        downloadFileDir = builder.downloadFileDir;
        requestTag = builder.requestTag;
        cookieJar = builder.cookieJar;
        if(null != cookieJar)
            clientBuilder.cookieJar(cookieJar);
        if(this.cacheSurvivalTime == 0){
            final int deviation = 5;
            switch (this.cacheLevel){
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
            cacheType = CACHE_THEN_NETWORK;
        if(null == application)
            cacheType = FORCE_NETWORK;
        if(null == executorService)
            executorService = Executors.newCachedThreadPool();
        BaseActivityLifecycleCallbacks.setShowLifecycleLog(showLifecycleLog);
        HelperInfo helperInfo = new HelperInfo();
        helperInfo.setShowHttpLog(showHttpLog);
        helperInfo.setRequestTag(requestTag);
        helperInfo.setTimeStamp(System.currentTimeMillis());
        helperInfo.setExceptionInterceptors(exceptionInterceptors);
        helperInfo.setResultInterceptors(resultInterceptors);
        helperInfo.setDownloadFileDir(downloadFileDir);
        helperInfo.setHttpClient(clientBuilder.build());
        helperInfo.setLogTAG(TAG);
        initHelper(helperInfo);
    }

    /**
     * 初始化辅助类
     */
    private void initHelper(HelperInfo helperInfo){
        HttpHelper.init(helperInfo);
        DownUpLoadHelper.init(helperInfo);
        LogHelper.get().init(helperInfo);
    }

    private OkHttpClient.Builder newBuilderFromCopy(){
        OkHttpClient.Builder newBuilder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .cache(new Cache(cachedDir,maxCacheSize))
                .retryOnConnectionFailure(retryOnConnectionFailure)
                .addInterceptor(CACHE_CONTROL_INTERCEPTOR)
                .addNetworkInterceptor(CACHE_CONTROL_NETWORK_INTERCEPTOR);
        if(null != networkInterceptors && !networkInterceptors.isEmpty())
            newBuilder.networkInterceptors().addAll(networkInterceptors);
        if(null != interceptors && !interceptors.isEmpty())
            newBuilder.interceptors().addAll(interceptors);
        newBuilder.addInterceptor(LOG_INTERCEPTOR);
        setSslSocketFactory(newBuilder);
        return newBuilder;
    }

    /**
     * 设置HTTPS认证
     */
    private void setSslSocketFactory(OkHttpClient.Builder clientBuilder){
        clientBuilder.hostnameVerifier(DO_NOT_VERIFY);
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            sc.init(null,new TrustManager[]{trustManager}, new SecureRandom());
            clientBuilder.sslSocketFactory(sc.getSocketFactory(),trustManager);
        } catch (Exception e) {
            LogHelper.get().showLog("Https认证异常: "+e.getMessage());
        }
    }

    /**
     *主机名验证
     */
    private final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static Builder Builder() {
        return new Builder(false);
    }

    private static Builder BuilderGlobal() {
        return new Builder(true);
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
        private Class<?> requestTag;
        private CookieJar cookieJar;

        public Builder() {
        }

        public Builder(boolean isGlobal) {
            isGlobalConfig = isGlobal;
            //系统默认配置
            initDefaultConfig();
            if(!isGlobal){
                if(null != builderGlobal){
                    //全局自定义配置
                    initGlobalConfig(builderGlobal);
                }
            }
        }

        public OkHttpUtilInterface build(){
            return build(null);
        }

        public OkHttpUtilInterface build(Object object) {
            if(isGlobalConfig){
                if(null == builderGlobal){
                    builderGlobal = this;
                }
            }
            if(null != object)
                setRequestTag(object);
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
        public Builder setRequestTag(Object object) {
            this.requestTag = object.getClass();
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



}
