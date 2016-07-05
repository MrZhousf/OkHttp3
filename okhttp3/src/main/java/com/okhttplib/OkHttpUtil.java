package com.okhttplib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class OkHttpUtil {

    private final String TAG = getClass().getSimpleName();
    private static Application application;
    private static OkHttpClient httpClient;
    private static Builder builderGlobal;
    private int cacheLevel;
    private int cacheType;
    private int cacheSurvivalTime;
    private Class<?> tag;
    private boolean showHttpLog;
    /**
     * 请求时间戳：区别每次请求标识
     */
    private long timeStamp;
    /**
     * 请求方法
     */
    private enum Method {
        GET,POST
    }

    /**
     * 回调请求标识
     */
    private final static int WHAT_CALLBACK = 1;

    /**
     * 初始化：请在Application中调用
     * @param context 上下文
     */
    public static Builder init(Application context){
        application = context;
        application.registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallbacks());
        return BuilderGlobal();
    }

    public static OkHttpUtil getDefault(){
        return new Builder(false).build();
    }

    /**
     * 获取默认请求配置
     * @param object 请求标识
     * @return OkHttpUtil
     */
    public static OkHttpUtil getDefault(Object object){
        return new Builder(false).build(object);
    }

    /**
     * 同步Post请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    public HttpInfo doPostSync(HttpInfo info){
        doRequestSync(info, Method.POST);
        return info;
    }

    /**
     * 同步Get请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    public HttpInfo doGetSync(HttpInfo info){
        doRequestSync(info, Method.GET);
        return info;
    }

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    public void doPostAsync(HttpInfo info, CallbackOk callback){
        doRequestAsync(info, Method.POST, callback);
    }

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    public void doGetAsync(HttpInfo info, CallbackOk callback){
        doRequestAsync(info, Method.GET, callback);
    }

    /**
     * 同步请求
     * @param info 请求信息体
     * @param method 请求方法
     * @return HttpInfo
     */
    private HttpInfo doRequestSync(HttpInfo info, Method method){
        Call call = null;
        try {
            String url = info.getUrl();
            if(TextUtils.isEmpty(url)){
                return retInfo(info,info.CheckURL);
            }
            call = httpClient.newCall(fetchRequest(info,method));
            BaseActivityLifecycleCallbacks.putCall(tag,info,call);
            Response res = call.execute();
            return dealResponse(info, res, call);
        } catch (IllegalArgumentException e){
            return retInfo(info,info.ProtocolException);
        } catch (SocketTimeoutException e){
            if(null != e.getMessage()){
                if(e.getMessage().contains("failed to connect to"))
                    return retInfo(info,info.ConnectionTimeOut);
                if(e.getMessage().equals("timeout"))
                    return retInfo(info,info.WriteAndReadTimeOut);
            }
            return retInfo(info,info.WriteAndReadTimeOut);
        } catch (UnknownHostException e) {
            return retInfo(info,info.CheckNet);
        } catch (Exception e) {
            return retInfo(info,info.NoResult);
        }finally {
            BaseActivityLifecycleCallbacks.cancelCall(tag,info,call);
        }
    }

    /**
     * 异步请求
     * @param info 请求信息体
     * @param method 请求方法
     * @param callback 回调接口
     */
    private void doRequestAsync(final HttpInfo info, Method method, final CallbackOk callback){
        if(null == callback)
            throw new NullPointerException("CallbackOk is null that not allowed");
        Call call = httpClient.newCall(fetchRequest(info,method));
        BaseActivityLifecycleCallbacks.putCall(tag,info,call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                //主线程回调
                handler.sendMessage(new CallbackMessage(WHAT_CALLBACK,callback,dealResponse(info,res,call)).build());
                BaseActivityLifecycleCallbacks.cancelCall(tag,info,call);
            }
        });
    }

    /**
     * 主线程业务调度
     */
    private static Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what){
                case WHAT_CALLBACK:
                    try {
                        CallbackMessage callMsg = (CallbackMessage) msg.obj;
                        callMsg.callback.onResponse(callMsg.info);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private HttpInfo dealResponse(HttpInfo info, Response res, Call call){
        try {
            if(null != res){
                if(res.isSuccessful() && null != res.body()){
                    return retInfo(info,info.SUCCESS,res.body().string());
                }else{
                    showLog("HttpStatus: "+res.code());
                    if(res.code() == 404)//请求页面路径错误
                        return retInfo(info,info.CheckURL);
                    if(res.code() == 500)//服务器内部错误
                        return retInfo(info,info.NoResult);
                    if(res.code() == 502)//错误网关
                        return retInfo(info,info.CheckNet);
                    if(res.code() == 504)//网关超时
                        return retInfo(info,info.CheckNet);
                }
            }
            return retInfo(info,info.CheckURL);
        } catch (Exception e) {
            e.printStackTrace();
            return retInfo(info,info.NoResult);
        } finally {
            if(null != res)
                res.close();
        }
    }

    private Request fetchRequest(HttpInfo info, Method method){
        Request request;
        if(method == Method.POST){
            FormBody.Builder builder = new FormBody.Builder();
            if(null != info.getParams() && !info.getParams().isEmpty()){
                StringBuilder log = new StringBuilder("PostParams: ");
                String logInfo;
                for (String key : info.getParams().keySet()) {
                    builder.add(key, info.getParams().get(key));
                    logInfo = key+" ="+info.getParams().get(key)+", ";
                    log.append(logInfo);
                }
                showLog(log.toString());
            }
            request = new Request.Builder()
                    .url(info.getUrl())
                    .post(builder.build())
                    .build();
        }else{
            StringBuilder params = new StringBuilder();
            params.append(info.getUrl());
            if(null != info.getParams() && !info.getParams().isEmpty()){
                String logInfo;
                for (String name : info.getParams().keySet()) {
                    logInfo = "&" + name + "=" + info.getParams().get(name);
                    params.append(logInfo);
                }
            }
            request = new Request.Builder()
                    .url(params.toString())
                    .get()
                    .build();
        }
        return request;
    }

    private HttpInfo retInfo(HttpInfo info, int code){
        retInfo(info,code,null);
        return info;
    }

    private HttpInfo retInfo(HttpInfo info, int code, String resDetail){
        info.packInfo(code,resDetail);
        showLog("Response: "+info.getRetDetail());
        return info;
    }

    /**
     * 网络请求拦截器
     */
    public Interceptor CACHE_CONTROL_NETWORK_INTERCEPTOR = new Interceptor() {
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
    public Interceptor CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            switch (cacheType){
                case CacheType.FORCE_CACHE:
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    break;
                case CacheType.FORCE_NETWORK:
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                    break;
                case CacheType.NETWORK_THEN_CACHE:
                    if(!isNetworkAvailable(application)){
                        request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    }else {
                        request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                    }
                    break;
                case CacheType.CACHE_THEN_NETWORK:
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
        final NetworkInfo net = cm.getActiveNetworkInfo();
        if (net != null && net.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        return false;
    }

    /**
     * 日志拦截器
     */
    public Interceptor LOG_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            long startTime = System.currentTimeMillis();
            showLog(String.format("%s-URL: %s %n",chain.request().method(),
                    chain.request().url()));
            Response res = chain.proceed(chain.request());
            long endTime = System.currentTimeMillis();
            showLog(String.format("CostTime: %.1fs", (endTime-startTime) / 1000.0));
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
                .addInterceptor(LOG_INTERCEPTOR)
                .addInterceptor(CACHE_CONTROL_INTERCEPTOR)
                .addNetworkInterceptor(CACHE_CONTROL_NETWORK_INTERCEPTOR);
        if(null != builder.networkInterceptors && !builder.networkInterceptors.isEmpty())
            clientBuilder.networkInterceptors().addAll(builder.networkInterceptors);
        if(null != builder.interceptors && !builder.interceptors.isEmpty())
            clientBuilder.interceptors().addAll(builder.interceptors);
        httpClient = clientBuilder.build();
        timeStamp = System.currentTimeMillis();
        final int deviation = 5;
        this.cacheLevel = builder.cacheLevel;
        this.cacheType = builder.cacheType;
        this.cacheSurvivalTime = builder.cacheSurvivalTime;
        this.tag = builder.tag;
        this.showHttpLog = builder.showHttpLog;
        if(this.cacheSurvivalTime == 0){
            switch (this.cacheLevel){
                case CacheLevel.FIRST_LEVEL:
                    this.cacheSurvivalTime = 0;
                    break;
                case CacheLevel.SECOND_LEVEL:
                    this.cacheSurvivalTime = 15 + deviation;
                    break;
                case CacheLevel.THIRD_LEVEL:
                    this.cacheSurvivalTime = 30 + deviation;
                    break;
                case CacheLevel.FOURTH_LEVEL:
                    this.cacheSurvivalTime = 60 + deviation;
                    break;
            }
        }
        if(this.cacheSurvivalTime > 0)
            cacheType = CacheType.CACHE_THEN_NETWORK;
        BaseActivityLifecycleCallbacks.setShowLifecycleLog(builder.showLifecycleLog);
    }

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
        private int cacheSurvivalTime;//缓存存活时间（秒）
        private int cacheType;//缓存类型
        private int cacheLevel;//缓存级别
        private boolean isGlobalConfig;//是否全局配置
        private boolean showHttpLog;//是否显示Http请求日志
        private boolean showLifecycleLog;//是否显示ActivityLifecycle日志
        private Class<?> tag;

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

        public OkHttpUtil build(){
            return build(null);
        }

        public OkHttpUtil build(Object object) {
            if(isGlobalConfig){
                if(null == builderGlobal){
                    builderGlobal = this;
                }
            }
            if(null != object)
                setTag(object);
            return new OkHttpUtil(this);
        }

        /**
         * 系统默认配置
         */
        private void initDefaultConfig(){
            setMaxCacheSize(10 * 1024 * 1024);
            setCachedDir(application.getExternalCacheDir());
            setConnectTimeout(30);
            setReadTimeout(30);
            setWriteTimeout(30);
            setRetryOnConnectionFailure(true);
            setCacheSurvivalTime(0);
            setCacheType(CacheType.NETWORK_THEN_CACHE);
            setCacheLevel(CacheLevel.FIRST_LEVEL);
            setNetworkInterceptors(null);
            setInterceptors(null);
            setShowHttpLog(true);
            setShowLifecycleLog(false);
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
            setShowHttpLog(builder.showHttpLog);
            setShowLifecycleLog(builder.showLifecycleLog);
        }

        public Builder setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder setCachedDir(File cachedDir) {
            if(null != cachedDir)
                this.cachedDir = cachedDir;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            if(connectTimeout <= 0)
                throw new IllegalArgumentException("connectTimeout must be > 0");
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setReadTimeout(int readTimeout) {
            if(readTimeout <= 0)
                throw new IllegalArgumentException("readTimeout must be > 0");
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder setWriteTimeout(int writeTimeout) {
            if(writeTimeout <= 0)
                throw new IllegalArgumentException("writeTimeout must be > 0");
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Builder setNetworkInterceptors(List<Interceptor> networkInterceptors) {
            if(null != networkInterceptors)
                this.networkInterceptors = networkInterceptors;
            return this;
        }

        public Builder setInterceptors(List<Interceptor> interceptors) {
            if(null != interceptors)
                this.interceptors = interceptors;
            return this;
        }

        public Builder setCacheSurvivalTime(int cacheSurvivalTime) {
            if(cacheSurvivalTime < 0)
                throw new IllegalArgumentException("cacheSurvivalTime must be >= 0");
            this.cacheSurvivalTime = cacheSurvivalTime;
            return this;
        }

        public Builder setCacheType(@CacheType int cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder setCacheLevel(@CacheLevel int cacheLevel) {
            this.cacheLevel = cacheLevel;
            return this;
        }

        public Builder setShowHttpLog(boolean showHttpLog) {
            this.showHttpLog = showHttpLog;
            return this;
        }

        public Builder setShowLifecycleLog(boolean showLifecycleLog) {
            this.showLifecycleLog = showLifecycleLog;
            return this;
        }

        public Builder setTag(Object object) {
            if(object instanceof Activity){
                Activity activity = (Activity) object;
                this.tag = activity.getClass();
            }
            if(object instanceof android.support.v4.app.Fragment){
                android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) object;
                this.tag = fragment.getActivity().getClass();
            }
            if(object instanceof android.app.Fragment){
                android.app.Fragment fragment = (android.app.Fragment) object;
                this.tag = fragment.getActivity().getClass();
            }
            return this;
        }
    }

    /**
     * 打印日志
     * @param msg 日志信息
     */
    private void showLog(String msg){
        if(this.showHttpLog)
            Log.d(TAG+"["+timeStamp+"]", msg);
    }


}
