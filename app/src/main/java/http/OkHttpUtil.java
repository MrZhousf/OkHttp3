package http;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import base.BaseApplication;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.LogUtil;
import util.NetWorkUtil;


public class OkHttpUtil {

    boolean doLog = true;//日志打印
    private final String TAG = getClass().getSimpleName();
    private static OkHttpClient httpClient;
    private Builder builder;
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
     * 请求集合: key=Activity value=Call集合
     */
    private static Map<Class<?>,List<Call>> callsMap = new ConcurrentHashMap<>();
    /**
     * 回调请求标识
     */
    private final static int WHAT_CALLBACK = 1;

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
        try {
            String url = info.getUrl();
            if(TextUtils.isEmpty(url)){
                return retInfo(info,info.CheckURL);
            }
            Call call = httpClient.newCall(fetchRequest(info,method));
            Response res = call.execute();
            putCall(info,call);
            return dealResponse(info, res);
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
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                //主线程回调
                handler.sendMessage(new CallbackMessage(WHAT_CALLBACK,callback,dealResponse(info,res)).build());
            }
        });
        putCall(info,call);
    }

    /**
     * 异步请求回调接口
     */
    public interface CallbackOk {
        /**
         * 该回调方法已切换到UI线程
         */
        void onResponse(HttpInfo info) throws IOException;
    }

    /**
     * 回调信息实体类
     */
    public class CallbackMessage{
        public int what;
        public CallbackOk callback;
        public HttpInfo info;
        public CallbackMessage(int what, CallbackOk callback, HttpInfo info) {
            this.what = what;
            this.callback = callback;
            this.info = info;
        }
        public Message build(){
            Message msg = new Message();
            msg.what = this.what;
            msg.obj = this;
            return msg;
        }
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

    private HttpInfo dealResponse(HttpInfo info, Response res){
        try {
            if(null != res && null != res.body()){
                if(res.isSuccessful()){
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
    public Interceptor CACHE_CONTROL_NETWORK_INTERCEPTOR = chain -> {
        Response.Builder resBuilder = chain.proceed(chain.request()).newBuilder();
        switch (builder.cacheType) {
            case CacheType.CACHE_THEN_NETWORK:
                resBuilder.removeHeader("Pragma")
                        .header("Cache-Control", String.format("max-age=%d", builder.cacheSurvivalTime));
        }
        return resBuilder.build();
    };

    /**
     * 缓存应用拦截器
     */
    public Interceptor CACHE_CONTROL_INTERCEPTOR = chain ->  {
        Request request = chain.request();
        switch (builder.cacheType){
            case CacheType.FORCE_CACHE:
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                break;
            case CacheType.FORCE_NETWORK:
                request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                break;
            case CacheType.NETWORK_THEN_CACHE:
            case CacheType.CACHE_THEN_NETWORK:
                if(!NetWorkUtil.isNetworkAvailable(BaseApplication.getApplication())){
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                }
                break;
        }
        return chain.proceed(request);
    };

    /**
     * 日志拦截器
     */
    public Interceptor LOG_INTERCEPTOR = chain -> {
        long startTime = System.currentTimeMillis();
        showLog(String.format("%s-URL: %s %n",chain.request().method(),
                chain.request().url()));
        Response res = chain.proceed(chain.request());
        long endTime = System.currentTimeMillis();
        showLog(String.format("CostTime: %.1fs", (endTime-startTime) / 1000.0));
        return res;
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
                .addNetworkInterceptor(CACHE_CONTROL_NETWORK_INTERCEPTOR)
                ;
        if(null != builder.networkInterceptors && !builder.networkInterceptors.isEmpty())
            clientBuilder.networkInterceptors().addAll(builder.networkInterceptors);
        if(null != builder.interceptors && !builder.interceptors.isEmpty())
            clientBuilder.interceptors().addAll(builder.interceptors);
        httpClient = clientBuilder.build();
        timeStamp = System.currentTimeMillis();
        final int deviation = 5;
        this.builder = builder;
        if(this.builder.cacheSurvivalTime == 0){
            switch (this.builder.cacheLevel){
                case CacheLevel.FIRST_LEVEL:
                    this.builder.cacheSurvivalTime = 0;
                    break;
                case CacheLevel.SECOND_LEVEL:
                    this.builder.cacheSurvivalTime = 15+deviation;
                    break;
                case CacheLevel.THIRD_LEVEL:
                    this.builder.cacheSurvivalTime = 30+deviation;
                    break;
                case CacheLevel.FOURTH_LEVEL:
                    this.builder.cacheSurvivalTime = 60+deviation;
                    break;
            }
        }
        if(this.builder.cacheSurvivalTime > 0)
            this.builder.cacheType = CacheType.CACHE_THEN_NETWORK;

    }

    public static Builder Builder() {
        return new Builder();
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

        public Builder() {
            //默认配置
            setMaxCacheSize(10 * 1024 * 1024);
            setCachedDir(BaseApplication.getApplication().getExternalCacheDir());
            setConnectTimeout(30);
            setReadTimeout(30);
            setWriteTimeout(30);
            setRetryOnConnectionFailure(true);
            setCacheSurvivalTime(0);
            setCacheType(CacheType.NETWORK_THEN_CACHE);
            setCacheLevel(CacheLevel.FIRST_LEVEL);
            setNetworkInterceptors(null);
            setInterceptors(null);
        }

        public OkHttpUtil build() {
            return new OkHttpUtil(this);
        }

        public Builder setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder setCachedDir(File cachedDir) {
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
    }

    @IntDef({CacheType.FORCE_NETWORK, CacheType.FORCE_CACHE, CacheType.NETWORK_THEN_CACHE, CacheType.CACHE_THEN_NETWORK})
    public @interface CacheType {
        int FORCE_NETWORK = 1;
        int FORCE_CACHE = 2;
        int NETWORK_THEN_CACHE = 3;
        int CACHE_THEN_NETWORK = 4;
    }

    @IntDef({CacheLevel.FIRST_LEVEL, CacheLevel.SECOND_LEVEL, CacheLevel.THIRD_LEVEL, CacheLevel.FOURTH_LEVEL})
    public @interface CacheLevel {
        int FIRST_LEVEL = 1; //默认无缓存
        int SECOND_LEVEL = 2; //缓存存活时间为15秒
        int THIRD_LEVEL = 3; //30秒
        int FOURTH_LEVEL = 4; //60秒
    }

    private void showLog(String msg){
        if(this.doLog)
            Log.d(TAG+"["+timeStamp+"]", msg);
    }

    /**
     * 保存请求集合
     * @param info 请求信息体
     * @param call 请求
     */
    private void putCall(HttpInfo info, Call call){
        if(null != info.getTag()){
            List<Call> callList = callsMap.get(info.getTag());
            if(null == callList){
                callList = new LinkedList<>();
                callList.add(call);
                callsMap.put(info.getTag(),callList);
            }else{
                callList.add(call);
            }
        }
    }

    /**
     * 取消请求
     * @param clazz 上下文
     */
    public static void cancelCall(Class<?> clazz){
        List<Call> callList = callsMap.get(clazz);
        if(null != callList){
            for(Call call : callList){
                if(!call.isCanceled())
                    call.cancel();
            }
            callsMap.remove(clazz);
        }
    }

    public static void print(){
        for(Map.Entry<Class<?>,List<Call>> entry : callsMap.entrySet()){
            LogUtil.d(OkHttpUtil.class,entry.getKey().getName() + ",size = " + entry.getValue().size());
        }
    }

    /**
     * Activity声明周期回调
     */
    public static class BaseActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            cancelCall(activity.getClass());
        }
    }



}
