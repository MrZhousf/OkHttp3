package com.okhttplib.helper;

import android.util.Log;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.CacheType;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;


/**
 * 业务操作基类：日志拦截与打印、Https验证
 * @author zhousf
 */
abstract class BaseHelper {

    int cacheSurvivalTime;//缓存存活时间（秒）
    @CacheType int cacheType;//缓存类型
    OkHttpClient httpClient;
    protected String TAG;
    protected String timeStamp;
    protected boolean showHttpLog;
    protected String requestTag;//请求标识
    HelperInfo helperInfo;
    HttpInfo httpInfo;

    BaseHelper() {
    }

    BaseHelper(HelperInfo helperInfo) {
        this.helperInfo = helperInfo;
        this.httpInfo = helperInfo.getHttpInfo();
        if(httpInfo != null){
            httpInfo.setFromCache(true);
        }
        cacheSurvivalTime = helperInfo.getCacheSurvivalTime();
        cacheType = helperInfo.getCacheType();
        TAG = helperInfo.getLogTAG();
        timeStamp = helperInfo.getTimeStamp();
        showHttpLog = helperInfo.isShowHttpLog();
        requestTag = helperInfo.getRequestTag();
        //是否采用默认的客户端进行请求
        OkHttpClient defaultClient = helperInfo.getOkHttpUtil().getDefaultClient();
        if(helperInfo.isDefault()){
           if(null == defaultClient){
               httpClient = initHttpClient(helperInfo,null);
               helperInfo.getOkHttpUtil().setDefaultClient(httpClient);
           }else{
               httpClient = initHttpClient(helperInfo,defaultClient.cookieJar());
           }
        }else{
            httpClient = initHttpClient(helperInfo,null);
        }
    }

    private OkHttpClient initHttpClient(HelperInfo helperInfo, CookieJar cookieJar){
        OkHttpClient.Builder clientBuilder = helperInfo.getClientBuilder();
        clientBuilder.protocols(Arrays.asList(Protocol.SPDY_3, Protocol.HTTP_1_1));
        clientBuilder.addInterceptor(NO_NETWORK_INTERCEPTOR);
        clientBuilder.addNetworkInterceptor(NETWORK_INTERCEPTOR);
        if(helperInfo.isGzip()){
            clientBuilder.addInterceptor(new GzipRequestInterceptor());
        }
        if(null != cookieJar)
            clientBuilder.cookieJar(cookieJar);
        setSslSocketFactory(clientBuilder);
        return clientBuilder.build();
    }

    /**
     * 网络请求拦截器
     */
    private final Interceptor NETWORK_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Response originalResponse = chain.proceed(originalRequest);
            httpInfo.setFromCache(false);
            if(cacheSurvivalTime == 0){
                cacheSurvivalTime = 60*60*24*365;//默认缓存时间为365天
            }
            return originalResponse.newBuilder()
                    .header("Cache-Control", String.format(Locale.getDefault(),"max-age=%d", cacheSurvivalTime))
                    .build();
        }
    };

    /**
     * 缓存应用拦截器
     */
    private Interceptor NO_NETWORK_INTERCEPTOR = new Interceptor() {

        int count = 0;

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Response originalResponse;
            switch (cacheType){
                case CacheType.FORCE_CACHE:
                    originalRequest = originalRequest.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    httpInfo.setFromCache(true);
                    originalResponse = chain.proceed(originalRequest);
                    break;
                case CacheType.FORCE_NETWORK:
                    originalRequest = originalRequest.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                    originalResponse = chain.proceed(originalRequest);
                    break;
                case CacheType.NETWORK_THEN_CACHE:
                    if(!helperInfo.getOkHttpUtil().isNetworkAvailable()){
                        httpInfo.setFromCache(true);
                        originalRequest = originalRequest.newBuilder()
                                .cacheControl(CacheControl.FORCE_CACHE)
                                .build();
                        originalResponse = chain.proceed(originalRequest);
                    }else {
                        originalRequest = originalRequest.newBuilder()
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build();
                        //网络请求失败后自动读取缓存并响应
                        originalResponse = aopChain(chain,originalRequest,CacheControl.FORCE_CACHE,true);
                    }
                    break;
                case CacheType.CACHE_THEN_NETWORK:
                    if(!helperInfo.getOkHttpUtil().isNetworkAvailable()){
                        httpInfo.setFromCache(true);
                        originalRequest = originalRequest.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                        originalResponse = chain.proceed(originalRequest);
                    }else{
                        originalResponse = chain.proceed(originalRequest);
                    }
                    break;
                default:
                    originalResponse = chain.proceed(originalRequest);
                    break;
            }
            return originalResponse;
        }

        private Response aopChain(Chain chain,Request request,CacheControl cacheControl,boolean readCache){
            Response originalResponse = null;
            count ++;
            try {
                //责任链模式处理拦截器
                originalResponse = chain.proceed(request);
                if(count >= 4)
                    return originalResponse;
            }catch (Exception e){
                e.printStackTrace();
                request = request.newBuilder().cacheControl(cacheControl).build();
                originalResponse = aopChain(chain,request,cacheControl,readCache);
                if(readCache)
                    httpInfo.setFromCache(true);
            }
            return originalResponse;
        }

    };


    /**
     * GZIP拦截器
     */
    private final class GzipRequestInterceptor implements Interceptor {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }

            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
            return chain.proceed(compressedRequest);
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override public MediaType contentType() {
                    return body.contentType();
                }

                @Override public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
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
            showLog("Https认证异常: "+e.getMessage());
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


    /**
     * 打印日志
     * @param msg 日志信息
     */
    void showLog(String msg){
        if(showHttpLog)
            Log.d(TAG+"["+timeStamp+"]", msg);
    }


}
