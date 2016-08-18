package com.okhttplib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;

import com.okhttplib.annotation.CacheLevel;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.DownloadStatus;
import com.okhttplib.bean.CallbackMessage;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.DownloadMessage;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.CallbackOk;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;
import com.okhttplib.progress.ProgressRequestBody;
import com.okhttplib.progress.ProgressResponseBody;
import com.okhttplib.util.EncryptUtil;
import com.okhttplib.util.MediaTypeUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
 * 9、引入版本com.squareup.okhttp3:okhttp:3.4.1
 * @author zhousf
 */
public class OkHttpUtil {

    private final String TAG = getClass().getSimpleName();
    private static Application application;
    private OkHttpClient httpClient;
    private static Builder builderGlobal;
    private ExecutorService executorService;
    /**
     * 请求时间戳：区别每次请求标识
     */
    private long timeStamp;
    /********  构建属性-定义开始  ***********/
    int maxCacheSize;//缓存大小
    File cachedDir;//缓存目录
    int connectTimeout;//连接超时
    int readTimeout;//读超时
    int writeTimeout;//写超时
    boolean retryOnConnectionFailure;//失败重新连接
    List<Interceptor> networkInterceptors;//网络拦截器
    List<Interceptor> interceptors;//应用拦截器
    int cacheSurvivalTime;//缓存存活时间（秒）
    int cacheType;//缓存类型
    int cacheLevel;//缓存级别
    boolean isGlobalConfig;//是否全局配置
    boolean showHttpLog;//是否显示Http请求日志
    boolean showLifecycleLog;//是否显示ActivityLifecycle日志
    String downloadFileDir;//下载文件保存目录
    Class<?> tag;//请求标识
    /********  构建属性-定义结束  ***********/

    private static Map<String,String> downloadTaskMap;

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
        return doRequestSync(info, POST);
    }

    /**
     * 异步Post请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    public void doPostAsync(HttpInfo info, CallbackOk callback){
        doRequestAsync(info, POST, callback, null);
    }

    /**
     * 同步Get请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    public HttpInfo doGetSync(HttpInfo info){
        return doRequestSync(info, GET);
    }

    /**
     * 异步Get请求
     * @param info 请求信息体
     * @param callback 回调接口
     */
    public void doGetAsync(HttpInfo info, CallbackOk callback){
        doRequestAsync(info, GET, callback, null);
    }

    /**
     * 异步上传文件
     * @param info 请求信息体
     */
    public void doUploadFileAsync(final HttpInfo info){
        List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        for(final UploadFileInfo fileInfo : uploadFiles){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    uploadFile(fileInfo, info);
                }
            });
        }
    }

    /**
     * 同步上传文件
     * @param info 请求信息体
     */
    public void doUploadFileSync(final HttpInfo info){
        List<UploadFileInfo> uploadFiles = info.getUploadFiles();
        for(final UploadFileInfo fileInfo : uploadFiles){
            uploadFile(fileInfo, info);
        }
    }

    /**
     * 异步下载文件
     * @param info 请求信息体
     */
    public void doDownloadFileAsync(final HttpInfo info){
        List<DownloadFileInfo> downloadFiles = info.getDownloadFiles();
        for(final DownloadFileInfo fileInfo : downloadFiles){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        downloadFile(fileInfo,info);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 同步下载文件
     * @param info 请求信息体
     */
    public void doDownloadFileSync(final HttpInfo info){
        List<DownloadFileInfo> downloadFiles = info.getDownloadFiles();
        for(final DownloadFileInfo fileInfo : downloadFiles){
            try {
                downloadFile(fileInfo,info);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void uploadFile(UploadFileInfo fileInfo, HttpInfo info){
        try {
            String filePath = fileInfo.getFilePathWithName();
            String interfaceParamName = fileInfo.getInterfaceParamName();
            String url = fileInfo.getUrl();
            url = TextUtils.isEmpty(url) ? info.getUrl() : url;
            if(TextUtils.isEmpty(url)){
                showLog("文件上传接口地址不能为空["+filePath+"]");
                return ;
            }
            ProgressCallback progressCallback = fileInfo.getProgressCallback();
            File file = new File(filePath);
            MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            StringBuilder log = new StringBuilder("PostParams: ");
            log.append(interfaceParamName+"="+filePath);
            String logInfo;
            if(null != info.getParams() && !info.getParams().isEmpty()){
                for (String key : info.getParams().keySet()) {
                    mBuilder.addFormDataPart(key, info.getParams().get(key));
                    logInfo = key+" ="+info.getParams().get(key)+", ";
                    log.append(logInfo);
                }
            }
            showLog(log.toString());
            mBuilder.addFormDataPart(interfaceParamName,
                    file.getName(),
                    RequestBody.create(MediaTypeUtil.fetchFileMediaType(filePath), file));
            RequestBody requestBody = mBuilder.build();
            final Request request = new Request
                    .Builder()
                    .url(url)
                    .post(new ProgressRequestBody(requestBody,progressCallback))
                    .build();
            doRequestSync(null,info,POST,request,null);
            responseCallback(info,progressCallback,OkMainHandler.RESPONSE_UPLOAD_CALLBACK);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void downloadFile(final DownloadFileInfo fileInfo, HttpInfo info){
        String url = fileInfo.getUrl();
        if(TextUtils.isEmpty(url)){
            showLog("下载文件失败：文件下载地址不能为空！");
            return ;
        }
        info.setUrl(url);
        ProgressCallback progressCallback = fileInfo.getProgressCallback();
        //获取文件断点
        long completedSize = fetchCompletedSize(fileInfo);
        fileInfo.setCompletedSize(completedSize);
        //添加下载任务
        if(null == downloadTaskMap)
            downloadTaskMap = new ConcurrentHashMap<>();
        if(downloadTaskMap.containsKey(fileInfo.getSaveFileNameEncrypt())){
            info = retInfo(info,info.Message,fileInfo.getSaveFileName()+" 已在下载任务中");
            responseCallback(info,progressCallback,OkMainHandler.RESPONSE_DOWNLOAD_CALLBACK);
            return ;
        }
        downloadTaskMap.put(fileInfo.getSaveFileNameEncrypt(),fileInfo.getSaveFileNameEncrypt());
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), fileInfo))
                        .build();
            }
        };
        OkHttpClient httpClient = newBuilderFromCopy().addInterceptor(interceptor).build();
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + completedSize + "-")
                .build();
        doRequestSync(httpClient,info,GET,request,fileInfo);
        //删除下载任务
        if(null != downloadTaskMap)
            downloadTaskMap.remove(fileInfo.getSaveFileNameEncrypt());
        responseCallback(info,progressCallback,OkMainHandler.RESPONSE_DOWNLOAD_CALLBACK);

    }

    private void responseCallback(HttpInfo info,ProgressCallback progressCallback,int code){
        //同步结果回调
        if(null != progressCallback)
            progressCallback.onResponseSync(info.getUrl(),info);
        //异步主线程结果回调
        Message msg = new DownloadMessage(
                code,
                info.getUrl(),
                info,
                progressCallback)
                .build();
        OkMainHandler.getInstance().sendMessage(msg);
    }

    private long fetchCompletedSize(DownloadFileInfo fileInfo){
        String saveFileDir = fileInfo.getSaveFileDir();
        String saveFileName = fileInfo.getSaveFileName();
        String url = fileInfo.getUrl();
        String extension = url.substring(url.lastIndexOf(".") + 1);//扩展名
        String saveFileNameCopy = saveFileName+"["+timeStamp+"]"+"."+extension;
        saveFileName += "."+extension;
        saveFileDir = TextUtils.isEmpty(saveFileDir) ? downloadFileDir : saveFileDir;
        mkDirNotExists(saveFileDir);
        fileInfo.setSaveFileDir(saveFileDir);
        fileInfo.setSaveFileNameCopy(saveFileNameCopy);
        fileInfo.setSaveFileNameWithExtension(saveFileName);
        String saveFileNameEncrypt = url;
        try {
            saveFileNameEncrypt = EncryptUtil.MD5StringTo32Bit(url,true);
            fileInfo.setSaveFileNameEncrypt(saveFileNameEncrypt);
        } catch (Exception e){
            e.printStackTrace();
        }
        File file = new File(saveFileDir,saveFileNameEncrypt);
        if(file.exists() && file.isFile()){
            long size = file.length();
            showLog("断点文件下载，节点["+size+"]");
            return size;
        }
        return 0L;
    }

    private HttpInfo doRequestSync(HttpInfo info,@Method int method){
        return doRequestSync(null,info,method,null,null);
    }

    /**
     * 同步请求
     * @param info 请求信息体
     * @param method 请求方法
     * @param request 请求
     * @param downloadFile 下载文件
     */
    private HttpInfo doRequestSync(OkHttpClient httpClient,HttpInfo info,@Method int method,Request request,DownloadFileInfo downloadFile){
        Call call = null;
        try {
            String url = info.getUrl();
            if(TextUtils.isEmpty(url)){
                return retInfo(info,info.CheckURL);
            }
            if(null == httpClient){
                call = this.httpClient.newCall(request == null ? fetchRequest(info,method) : request);
            }else{
                call = httpClient.newCall(request == null ? fetchRequest(info,method) : request);
            }
            BaseActivityLifecycleCallbacks.putCall(tag,info,call);
            Response res = call.execute();
            return dealResponse(info, res, call, downloadFile);
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
        } catch(NetworkOnMainThreadException e){
            return retInfo(info,info.NetworkOnMainThreadException);
        } catch(Exception e) {
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
    private void doRequestAsync(final HttpInfo info, @Method int method, final CallbackOk callback, Request request){
        if(null == callback)
            throw new NullPointerException("CallbackOk is null that not allowed");
        Call call = httpClient.newCall(request == null ? fetchRequest(info,method) : request);
        BaseActivityLifecycleCallbacks.putCall(tag,info,call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                //主线程回调
                Message msg =  new CallbackMessage(OkMainHandler.RESPONSE_CALLBACK,
                        callback,
                        dealResponse(info,res,call,null))
                        .build();
                OkMainHandler.getInstance().sendMessage(msg);
                BaseActivityLifecycleCallbacks.cancelCall(tag,info,call);
            }
        });
    }

    private HttpInfo dealResponse(HttpInfo info,Response res,Call call,DownloadFileInfo downloadFile){
        try {
            if(null != res){
                if(res.isSuccessful() && null != res.body()){
                    if(null == downloadFile){
                        return retInfo(info,info.SUCCESS,res.body().string());
                    }else{ //下载文件
                        return dealDownloadFile(info,downloadFile,res,call);
                    }
                }else{
                    showLog("HttpStatus: "+res.code());
                    if(res.code() == 404)//请求页面路径错误
                        return retInfo(info,info.CheckURL);
                    if(res.code() == 416)//请求数据流范围错误
                        return retInfo(info,info.Message,"请求Http数据流范围错误\n"+res.body().string());
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

    private HttpInfo dealDownloadFile(HttpInfo info,DownloadFileInfo fileInfo,Response res,Call call){
        RandomAccessFile accessFile = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        String filePath = fileInfo.getSaveFileDir()+fileInfo.getSaveFileNameWithExtension();
        try {
            ResponseBody responseBody = res.body();
            int length;
            long completedSize = fileInfo.getCompletedSize();
            accessFile = new RandomAccessFile(fileInfo.getSaveFileDir()+fileInfo.getSaveFileNameEncrypt(),"rwd");
            //服务器不支持断点下载时重新下载
            if(TextUtils.isEmpty(res.header("Content-Range"))){
                completedSize = 0L;
                fileInfo.setCompletedSize(completedSize);
            }
            accessFile.seek(completedSize);
            inputStream = responseBody.byteStream();
            byte[] buffer = new byte[2048];
            bis = new BufferedInputStream(inputStream);
            fileInfo.setDownloadStatus(DownloadStatus.DOWNLOADING);
            while ( (length = bis.read(buffer)) > 0 &&
                    (DownloadStatus.DOWNLOADING.equals(fileInfo.getDownloadStatus()))) {
                accessFile.write(buffer, 0, length);
                completedSize += length;
            }
            if(DownloadStatus.PAUSE.equals(fileInfo.getDownloadStatus())){
                return retInfo(info,info.Message,"暂停下载");
            }
            //下载完成
            if(DownloadStatus.DOWNLOADING.equals(fileInfo.getDownloadStatus())){
                fileInfo.setDownloadStatus(DownloadStatus.COMPLETED);
                File newFile = new File(fileInfo.getSaveFileDir(),fileInfo.getSaveFileNameWithExtension());
                //处理文件已存在逻辑
                if(newFile.exists() && newFile.isFile()){
                    filePath = fileInfo.getSaveFileDir()+fileInfo.getSaveFileNameCopy();
                    newFile = new File(fileInfo.getSaveFileDir(),fileInfo.getSaveFileNameCopy());
                }
                File oldFile = new File(fileInfo.getSaveFileDir(),fileInfo.getSaveFileNameEncrypt());
                if(oldFile.exists() && oldFile.isFile()){
                    oldFile.renameTo(newFile);
                }
                return retInfo(info,info.SUCCESS,filePath);
            }
        }catch(SocketTimeoutException e){
            return retInfo(info,info.WriteAndReadTimeOut);
        }catch (Exception e){
            return retInfo(info,info.ConnectionInterruption);
        }finally {
            try {
                if(null != bis)
                    bis.close();
                if(null != inputStream)
                    inputStream.close();
                if(null != accessFile)
                    accessFile.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            BaseActivityLifecycleCallbacks.cancelCall(tag,info,call);
            //删除下载任务
            if(null != downloadTaskMap)
                downloadTaskMap.remove(fileInfo.getSaveFileNameEncrypt());
        }
        return retInfo(info,info.SUCCESS,filePath);
    }

    private boolean mkDirNotExists(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    private Request fetchRequest(HttpInfo info, @Method int method){
        Request request;
        if(method == POST){
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
        final NetworkInfo net = cm.getActiveNetworkInfo();
        if (net != null && net.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        return false;
    }

    /**
     * 日志拦截器
     */
    private Interceptor LOG_INTERCEPTOR = new Interceptor() {
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
        return newBuilder;
    }

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
        httpClient = clientBuilder.build();
        timeStamp = System.currentTimeMillis();
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
        BaseActivityLifecycleCallbacks.setShowLifecycleLog(builder.showLifecycleLog);
        executorService = Executors.newCachedThreadPool();
        //初始化参数
        maxCacheSize = builder.maxCacheSize;
        cachedDir = builder.cachedDir;
        connectTimeout = builder.connectTimeout;
        readTimeout = builder.readTimeout;
        writeTimeout = builder.writeTimeout;
        retryOnConnectionFailure = builder.retryOnConnectionFailure;
        networkInterceptors = builder.networkInterceptors;
        interceptors = builder.interceptors;
        cacheSurvivalTime = builder.cacheSurvivalTime;
        cacheType = builder.cacheType;
        cacheLevel = builder.cacheLevel;
        isGlobalConfig = builder.isGlobalConfig;
        showHttpLog = builder.showHttpLog;
        showLifecycleLog = builder.showLifecycleLog;
        downloadFileDir = builder.downloadFileDir;
        tag = builder.tag;
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
            e.printStackTrace();
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
        private int cacheSurvivalTime;//缓存存活时间（秒）
        private int cacheType;//缓存类型
        private int cacheLevel;//缓存级别
        private boolean isGlobalConfig;//是否全局配置
        private boolean showHttpLog;//是否显示Http请求日志
        private boolean showLifecycleLog;//是否显示ActivityLifecycle日志
        private String downloadFileDir;//下载文件保存目录
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
            setCacheType(NETWORK_THEN_CACHE);
            setCacheLevel(FIRST_LEVEL);
            setNetworkInterceptors(null);
            setInterceptors(null);
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
            setShowHttpLog(builder.showHttpLog);
            setShowLifecycleLog(builder.showLifecycleLog);
            if(!TextUtils.isEmpty(builder.downloadFileDir)){
                setDownloadFileDir(builder.downloadFileDir);
            }
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

        public Builder setDownloadFileDir(String downloadFileDir) {
            this.downloadFileDir = downloadFileDir;
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

    /**
     * 请求方法
     */
    @IntDef({POST,GET})
    @Retention(RetentionPolicy.SOURCE)
    private  @interface Method{}
    private static final int POST = 1;
    private static final int GET = 2;


}
