package com.okhttplib.helper;

import android.annotation.SuppressLint;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.ContentType;
import com.okhttplib.bean.DownloadMessage;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.bean.UploadMessage;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;
import com.okhttplib.util.MediaTypeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
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

    private int cacheSurvivalTime;//缓存存活时间（秒）
    private @CacheType int cacheType;//缓存类型
    OkHttpClient httpClient;
    OkHttpClient.Builder clientBuilder;
    private String TAG;
    String timeStamp;
    private boolean showHttpLog;
    public String requestTag;//请求标识
    HelperInfo helperInfo;
    HttpInfo httpInfo;
    private List<ResultInterceptor> resultInterceptors;//请求结果拦截器
    private List<ExceptionInterceptor> exceptionInterceptors;//请求链路异常拦截器

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
        if(helperInfo.isDefault()){
            OkHttpClient defaultClient = helperInfo.getOkHttpUtil().getDefaultClient();
            if(null == defaultClient){
               httpClient = initHttpClient(helperInfo);
               helperInfo.getOkHttpUtil().setDefaultClient(httpClient);
            }else{
               httpClient = defaultClient;
            }
        }else{
            httpClient = initHttpClient(helperInfo);
        }
        resultInterceptors = helperInfo.getResultInterceptors();
        exceptionInterceptors = helperInfo.getExceptionInterceptors();
    }

    private OkHttpClient initHttpClient(HelperInfo helperInfo){
        if(clientBuilder != null){
            return clientBuilder.build();
        }
        clientBuilder = helperInfo.getClientBuilder();
        clientBuilder.protocols(Arrays.asList(Protocol.SPDY_3, Protocol.HTTP_1_1));
        clientBuilder.addInterceptor(NO_NETWORK_INTERCEPTOR);
        clientBuilder.addNetworkInterceptor(NETWORK_INTERCEPTOR);
        if(helperInfo.isGzip()){
            clientBuilder.addInterceptor(new GzipRequestInterceptor());
        }
        if(httpInfo == null || httpInfo.getUrl() == null)
            return clientBuilder.build();
        try {
            URI uri = new URL(httpInfo.getUrl()).toURI();
            if("https".equals(uri.getScheme())){
                if(helperInfo.getHttpsCertificateStream() == null){
                    setDefaultSslSocketFactory(clientBuilder);
                }else{
                    setSslSocketFactory(clientBuilder);
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
            if(httpInfo != null){
                httpInfo.setFromCache(false);
            }
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
            Response originalResponse = null;
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
//                        originalRequest = originalRequest.newBuilder()
//                                .cacheControl(CacheControl.FORCE_NETWORK)
//                                .build();
//                        //网络请求失败后自动读取缓存并响应
//                        originalResponse = aopChain(chain,originalRequest,CacheControl.FORCE_CACHE,true);
                        originalResponse = chain.proceed(originalRequest);
                        originalResponse.newBuilder()
                                .removeHeader("Pragma")
                                .header("Cache-Control", "public, only-if-cached, max-stale=" + 60*60*24*365)
                                .build();
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
     * 设置HTTPS认证：默认信任所有证书
     */
    private void setDefaultSslSocketFactory(OkHttpClient.Builder clientBuilder){
        clientBuilder.hostnameVerifier(DO_NOT_VERIFY);
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null,new TrustManager[]{TRUST_MANAGER}, new SecureRandom());
            clientBuilder.sslSocketFactory(sc.getSocketFactory(),TRUST_MANAGER);
        } catch (Exception e) {
            showLog("Https认证异常: "+e.getMessage());
        }
    }

    /**
     * 设置HTTPS认证
     */
    private void setSslSocketFactory(OkHttpClient.Builder clientBuilder){
        SSLContext sslContext = getSSLContext();
        if(sslContext != null){
            clientBuilder.sslSocketFactory(sslContext.getSocketFactory(),TRUST_MANAGER);
        }
    }

    @SuppressLint("TrulyRandom")
    private SSLContext getSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            InputStream inputStream = helperInfo.getHttpsCertificateStream();
            CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
            Certificate cer = cerFactory.generateCertificate(inputStream);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust", cer);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, null);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    /**
     *主机名验证
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static final X509TrustManager TRUST_MANAGER = new X509TrustManager() {
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

    /**
     * 添加请求头参数
     */
    void addHeadsToRequest(HttpInfo info, Request.Builder requestBuilder){
        if(null != info.getHeads() && !info.getHeads().isEmpty()){
            StringBuilder log = new StringBuilder("Heads: ");
            for (String key : info.getHeads().keySet()) {
                requestBuilder.addHeader(key,info.getHeads().get(key));
                log.append(key).append("=").append(info.getHeads().get(key)).append(" | ");
            }
            int point = log.lastIndexOf("|");
            if(point != -1){
                log.deleteCharAt(point);
            }
            showLog(log.toString());
        }
    }

    HttpInfo retInfo(HttpInfo info, int code){
        return retInfo(info,code,code,null);
    }

    HttpInfo retInfo(HttpInfo info, int netCode, int code){
        return retInfo(info,netCode,code,null);
    }

    HttpInfo retInfo(HttpInfo info, int code, String resDetail){
        return retInfo(info,code,code,resDetail);
    }

    /**
     * 封装请求结果
     */
    HttpInfo retInfo(HttpInfo info, int netCode, int code, String resDetail){
        info.packInfo(netCode,code,unicodeToString(resDetail));
        //拦截请求结果
        dealInterceptor(info);
        showLog("Response: "+info.getRetDetail());
        return info;
    }


    /**
     * unicode中文转码
     */
    private String unicodeToString(String str) {
        if(TextUtils.isEmpty(str))
            return "";
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    /**
     * 处理拦截器
     */
    private void dealInterceptor(HttpInfo info){
        try {
            if(BaseActivityLifecycleCallbacks.isActivityDestroyed(requestTag))
                return ;
            if(info.isSuccessful() && null != resultInterceptors){ //请求结果拦截器
                for(ResultInterceptor interceptor : resultInterceptors){
                    interceptor.intercept(info);
                }
            }else{ //请求链路异常拦截器
                if(null != exceptionInterceptors){
                    for(ExceptionInterceptor interceptor : exceptionInterceptors){
                        interceptor.intercept(info);
                    }
                }
            }
        }catch (Exception e){
            showLog("拦截器处理异常："+e.getMessage());
        }
    }

    /**
     * 请求结果回调
     */
    void responseCallback(HttpInfo info, ProgressCallback progressCallback, int code, String requestTag){
        //同步回调
        if(null != progressCallback)
            progressCallback.onResponseSync(info.getUrl(),info);
        //异步主线程回调
        if(OkMainHandler.RESPONSE_DOWNLOAD_CALLBACK == code){
            Message msg = new DownloadMessage(
                    code,
                    info.getUrl(),
                    info,
                    progressCallback,requestTag)
                    .build();
            OkMainHandler.getInstance().sendMessage(msg);
        } else if(OkMainHandler.RESPONSE_UPLOAD_CALLBACK == code){
            Message msg = new UploadMessage(
                    code,
                    info.getUrl(),
                    info,
                    progressCallback,requestTag)
                    .build();
            OkMainHandler.getInstance().sendMessage(msg);
        }
    }

    /**
     * 打印日志
     * @param msg 日志信息
     */
    void showLog(String msg){
        if(showHttpLog)
            Log.d(TAG+"["+timeStamp+"]", msg);
    }


    protected RequestBody matchContentType(HttpInfo info,UploadFileInfo fileInfo){
        RequestBody requestBody;
        MediaType mediaType;
        //设置请求参数编码格式
        String requestEncoding = info.getRequestEncoding();
        if(TextUtils.isEmpty(requestEncoding)){
            requestEncoding = ";charset=" + helperInfo.getRequestEncoding().toLowerCase();
        }else{
            requestEncoding = ";charset=" + requestEncoding.toLowerCase();
        }
        //上传文件
        if(fileInfo != null){
            String contentType = fileInfo.getContentType();
            contentType = TextUtils.isEmpty(contentType)?info.getContentType():contentType;
            mediaType = MediaType.parse(contentType+requestEncoding);
            String filePath = fileInfo.getFilePathWithName();
            if(fileInfo.getFile() != null){
                if(TextUtils.isEmpty(filePath)){
                    requestBody = RequestBody.create(mediaType,fileInfo.getFile());
                }else{
                    requestBody = RequestBody.create(
                            MediaTypeUtil.fetchFileMediaType(filePath,requestEncoding),
                            fileInfo.getFile());
                }
            }else if(fileInfo.getFileByte() != null){
                requestBody = RequestBody.create(mediaType,fileInfo.getFileByte());
            }else{
                requestBody = RequestBody.create(mediaType,fileInfo.getFile());
            }
            return requestBody;
        }
        //兼容以前版本(新版本扩展了ContentType)
        if(!TextUtils.isEmpty(info.getContentType())){
            mediaType = MediaType.parse(info.getContentType()+requestEncoding);
            if(info.getParamBytes() != null){
                requestBody = RequestBody.create(mediaType,info.getParamBytes());
            } else if(info.getParamFile() != null){
                requestBody = RequestBody.create(mediaType,info.getParamFile());
            } else if(info.getParamJson() != null){
                showLog("Params: "+info.getParamJson());
                requestBody = RequestBody.create(mediaType,info.getParamJson());
            } else if(info.getParamForm() != null){
                showLog("Params: "+info.getParamForm());
                requestBody = RequestBody.create(mediaType,info.getParamForm());
            } else {
                requestBody = packageRequestBody(info,mediaType);
            }
        }else {
            if(info.getParamBytes() != null){
                requestBody = RequestBody.create(MediaType.parse(ContentType.STREAM+requestEncoding),info.getParamBytes());
            } else if(info.getParamFile() != null){
                requestBody = RequestBody.create(MediaType.parse(ContentType.MARKDOWN+requestEncoding),info.getParamFile());
            } else if(info.getParamJson() != null){
                showLog("Params: "+info.getParamJson());
                requestBody = RequestBody.create(MediaType.parse(ContentType.JSON+requestEncoding),info.getParamJson());
            } else if(info.getParamForm() != null){
                showLog("Params: "+info.getParamForm());
                requestBody = RequestBody.create(MediaType.parse(ContentType.FORM+requestEncoding),info.getParamForm());
            } else{
                requestBody = packageRequestBody(info,MediaType.parse(ContentType.FORM+requestEncoding));
            }
        }
        return requestBody;
    }

    private RequestBody packageRequestBody(HttpInfo info, MediaType contentType){
        String value;
        StringBuilder param = new StringBuilder();
        StringBuilder log = new StringBuilder();
        boolean isFirst = true;
        if(info.getParams() != null){
            for (String key : info.getParams().keySet()) {
                value = info.getParams().get(key);
                value = value == null ? "" : value;
                if(isFirst){
                    isFirst = false;
                    param.append(key).append("=").append(value);
                    log.append(key).append("=").append(value);
                }else{
                    param.append("&").append(key).append("=").append(value);
                    log.append(" | ").append(key).append("=").append(value);
                }
            }
        }
        showLog("Params: "+log.toString());
        return RequestBody.create(contentType,param.toString());
    }


}
