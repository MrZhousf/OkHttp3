package com.okhttplib.helper;

import android.os.Build;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.RequestMethod;
import com.okhttplib.bean.CallbackMessage;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.DownloadMessage;
import com.okhttplib.bean.HelperInfo;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.CallbackOk;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Http请求辅助类
 * @author: zhousf
 */
public class HttpHelper {

    private static OkHttpClient httpClient;
    private static Class<?> requestTag;//请求标识
    private static List<ResultInterceptor> resultInterceptors;//请求结果拦截器
    private static List<ExceptionInterceptor> exceptionInterceptors;//请求链路异常拦截器

    private HttpHelper() {
    }

    public static void init(HelperInfo helperInfo){
        httpClient = helperInfo.getHttpClient();
        requestTag = helperInfo.getRequestTag();
        resultInterceptors = helperInfo.getResultInterceptors();
        exceptionInterceptors = helperInfo.getExceptionInterceptors();
    }

    /**
     * 构建Request
     */
    private static Request buildRequest(HttpInfo info, @RequestMethod int method){
        Request request;
        Request.Builder requestBuilder = new Request.Builder();
        final String url = info.getUrl();
        if(method == RequestMethod.POST){
            FormBody.Builder builder = new FormBody.Builder();
            if(null != info.getParams() && !info.getParams().isEmpty()){
                StringBuilder log = new StringBuilder("PostParams: ");
                String logInfo;
                String value;
                for (String key : info.getParams().keySet()) {
                    value = info.getParams().get(key);
                    value = value == null ? "" : value;
                    builder.add(key, value);
                    logInfo = key+"="+value+", ";
                    log.append(logInfo);
                }
                LogHelper.get().showLog(log.toString());
            }
            requestBuilder.url(url).post(builder.build());
        } else if(method == RequestMethod.GET){
            StringBuilder params = new StringBuilder();
            params.append(url);
            if(null != info.getParams() && !info.getParams().isEmpty()){
                if(!url.contains("?") && !url.endsWith("?"))
                    params.append("?");
                String logInfo;
                String value;
                boolean isFirst = params.toString().endsWith("?");
                for (String name : info.getParams().keySet()) {
                    value = info.getParams().get(name);
                    value = value == null ? "" : value;
                    if(isFirst){
                        logInfo = name + "=" + value;
                        isFirst = false;
                    }else{
                        logInfo = "&" + name + "=" + value;
                    }
                    params.append(logInfo);
                }
            }
            requestBuilder.url(params.toString()).get();
        } else{
            requestBuilder.url(url).get();
        }
        if (Build.VERSION.SDK_INT > 13) {
            requestBuilder.addHeader("Connection", "close");
        }
        addHeadsToRequest(info,requestBuilder);
        request = requestBuilder.build();
        return request;
    }


    /**
     * 同步请求
     * @param info 请求信息体
     * @param method 请求方法
     * @param request 请求
     * @param downloadFile 下载文件
     */
     static HttpInfo doRequestSync(OkHttpClient httpClient, HttpInfo info, @RequestMethod int method, Request request, DownloadFileInfo downloadFile){
        Call call = null;
        try {
            String url = info.getUrl();
            if(TextUtils.isEmpty(url)){
                return retInfo(info,HttpInfo.CheckURL);
            }
            httpClient = httpClient == null ? HttpHelper.httpClient : httpClient;
            call = httpClient.newCall(request == null ? buildRequest(info,method) : request);
            BaseActivityLifecycleCallbacks.putCall(requestTag,info,call);
            Response res = call.execute();
            return dealResponse(info, res, call, downloadFile);
        } catch (IllegalArgumentException e){
            return retInfo(info,HttpInfo.ProtocolException);
        } catch (SocketTimeoutException e){
            if(null != e.getMessage()){
                if(e.getMessage().contains("failed to connect to"))
                    return retInfo(info,HttpInfo.ConnectionTimeOut);
                if(e.getMessage().equals("timeout"))
                    return retInfo(info,HttpInfo.WriteAndReadTimeOut);
            }
            return retInfo(info,HttpInfo.WriteAndReadTimeOut);
        } catch (UnknownHostException e) {
            return retInfo(info,HttpInfo.CheckNet);
        } catch(NetworkOnMainThreadException e){
            return retInfo(info,HttpInfo.NetworkOnMainThreadException);
        } catch(Exception e) {
            return retInfo(info,HttpInfo.NoResult);
        }finally {
            BaseActivityLifecycleCallbacks.cancelCall(requestTag,info,call);
        }
    }

    public static HttpInfo doRequestSync(HttpInfo info,@RequestMethod int method){
        return doRequestSync(null,info,method,null,null);
    }

    /**
     * 异步请求
     * @param info 请求信息体
     * @param method 请求方法
     * @param callback 回调接口
     * @param request request
     */
    public static void doRequestAsync(final HttpInfo info, @RequestMethod int method, final CallbackOk callback, Request request){
        if(null == callback)
            throw new NullPointerException("CallbackOk is null that not allowed");
        Call call = httpClient.newCall(request == null ? buildRequest(info,method) : request);
        BaseActivityLifecycleCallbacks.putCall(requestTag,info,call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogHelper.get().showLog(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                //主线程回调
                Message msg =  new CallbackMessage(OkMainHandler.RESPONSE_CALLBACK,
                        callback,
                        dealResponse(info,res,call,null))
                        .build();
                OkMainHandler.getInstance().sendMessage(msg);
                BaseActivityLifecycleCallbacks.cancelCall(requestTag,info,call);
            }
        });
    }

    /**
     * 处理HTTP响应
     */
    private static HttpInfo dealResponse(HttpInfo info,Response res,Call call,DownloadFileInfo downloadFile){
        try {
            if(null != res){
                if(res.isSuccessful() && null != res.body()){
                    if(null == downloadFile){
                        return retInfo(info,HttpInfo.SUCCESS,res.body().string());
                    }else{ //下载文件
                        return DownUpLoadHelper.downloadingFile(info,downloadFile,res,call);
                    }
                }else{
                    LogHelper.get().showLog("HttpStatus: "+res.code());
                    if(res.code() == 404)//请求页面路径错误
                        return retInfo(info,HttpInfo.CheckURL);
                    if(res.code() == 416)//请求数据流范围错误
                        return retInfo(info,HttpInfo.Message,"请求Http数据流范围错误\n"+res.body().string());
                    if(res.code() == 500)//服务器内部错误
                        return retInfo(info,HttpInfo.NoResult);
                    if(res.code() == 502)//错误网关
                        return retInfo(info,HttpInfo.CheckNet);
                    if(res.code() == 504)//网关超时
                        return retInfo(info,HttpInfo.CheckNet);
                }
            }
            return retInfo(info,HttpInfo.CheckURL);
        } catch (Exception e) {
            e.printStackTrace();
            return retInfo(info,HttpInfo.NoResult);
        } finally {
            if(null != res)
                res.close();
        }
    }

    static HttpInfo retInfo(HttpInfo info, int code){
        retInfo(info,code,null);
        return info;
    }

    /**
     * 封装请求结果
     */
    static HttpInfo retInfo(HttpInfo info, int code, String resDetail){
        info.packInfo(code,resDetail);
        //拦截请求结果
        dealInterceptor(info);
        LogHelper.get().showLog("Response: "+info.getRetDetail());
        return info;
    }

    /**
     * 处理拦截器
     */
    private static void dealInterceptor(HttpInfo info){
        try {
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
            LogHelper.get().showLog("拦截器处理异常："+e.getMessage());
        }
    }

    /**
     * 请求结果回调
     */
    static void responseCallback(HttpInfo info, ProgressCallback progressCallback, int code){
        //同步回调
        if(null != progressCallback)
            progressCallback.onResponseSync(info.getUrl(),info);
        //异步主线程回调
        Message msg = new DownloadMessage(
                code,
                info.getUrl(),
                info,
                progressCallback)
                .build();
        OkMainHandler.getInstance().sendMessage(msg);
    }

    /**
     * 添加请求头参数
     */
    static Request.Builder addHeadsToRequest(HttpInfo info, Request.Builder requestBuilder){
        if(null != info.getHeads() && !info.getHeads().isEmpty()){
            for (String key : info.getHeads().keySet()) {
                requestBuilder.addHeader(key,info.getHeads().get(key));
            }
        }
        return requestBuilder;
    }




}
