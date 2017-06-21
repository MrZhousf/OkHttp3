package com.okhttplib.helper;

import android.os.Build;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.BusinessType;
import com.okhttplib.annotation.ContentType;
import com.okhttplib.annotation.RequestType;
import com.okhttplib.bean.CallbackMessage;
import com.okhttplib.bean.DownloadMessage;
import com.okhttplib.bean.UploadMessage;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.BaseCallback;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;
import com.okhttplib.progress.ProgressRequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.okhttplib.HttpInfo.ConnectionTimeOut;
import static com.okhttplib.HttpInfo.WriteAndReadTimeOut;

/**
 * Http网络请求业务类
 * @author zhousf
 */
class HttpHelper extends BaseHelper{

    private List<ResultInterceptor> resultInterceptors;//请求结果拦截器
    private List<ExceptionInterceptor> exceptionInterceptors;//请求链路异常拦截器
    private long startTime;

    HttpHelper(HelperInfo helperInfo) {
        super(helperInfo);
        resultInterceptors = helperInfo.getResultInterceptors();
        exceptionInterceptors = helperInfo.getExceptionInterceptors();
    }

    /**
     * 同步请求
     */
    HttpInfo doRequestSync(OkHttpHelper helper){
        Call call = null;
        final HttpInfo info = httpInfo;
        Request request = helper.getRequest();
        String url = info.getUrl();
        if(!checkUrl(url)){
            return retInfo(info,HttpInfo.CheckURL);
        }
        request = request == null ? buildRequest(info,helper.getRequestType(),helper.getProgressCallback()) : request;
        showUrlLog(request);
        helper.setRequest(request);
        OkHttpClient httpClient = helper.getHttpClient();
        try {
            httpClient = httpClient == null ? super.httpClient : httpClient;
            call = httpClient.newCall(request);
            BaseActivityLifecycleCallbacks.putCall(requestTag,call);
            Response res = call.execute();
            return dealResponse(helper, res, call);
        } catch (IllegalArgumentException e){
            return retInfo(info,HttpInfo.ProtocolException);
        } catch (SocketTimeoutException e){
            if(null != e.getMessage()){
                if(e.getMessage().contains("failed to connect to"))
                    return retInfo(info, ConnectionTimeOut);
                if(e.getMessage().equals("timeout"))
                    return retInfo(info, WriteAndReadTimeOut);
            }
            return retInfo(info, WriteAndReadTimeOut);
        } catch (UnknownHostException e) {
            if(!helperInfo.getOkHttpUtil().isNetworkAvailable()){
                return retInfo(info,HttpInfo.CheckNet,"["+e.getMessage()+"]");
            }else{
                return retInfo(info,HttpInfo.CheckURL,"["+e.getMessage()+"]");
            }
        } catch(NetworkOnMainThreadException e){
            return retInfo(info,HttpInfo.NetworkOnMainThreadException);
        } catch(Exception e) {
            return retInfo(info,HttpInfo.NoResult,"["+e.getMessage()+"]");
        }finally {
            BaseActivityLifecycleCallbacks.cancel(requestTag,call);
        }
    }

    /**
     * 异步请求
     */
    void doRequestAsync(final OkHttpHelper helper){
        final HttpInfo info = httpInfo;
        final BaseCallback callback = helper.getCallback();
        Request request = helper.getRequest();
        String url = info.getUrl();
        if(!checkUrl(url)){
            //主线程回调
            Message msg =  new CallbackMessage(OkMainHandler.RESPONSE_CALLBACK,
                    callback,
                    retInfo(info,HttpInfo.CheckURL),
                    requestTag,
                    null)
                    .build();
            OkMainHandler.getInstance().sendMessage(msg);
            return ;
        }
        request = request == null ? buildRequest(info,helper.getRequestType(),helper.getProgressCallback()) : request;
        showUrlLog(request);
        Call call = httpClient.newCall(request);
        BaseActivityLifecycleCallbacks.putCall(requestTag,call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //主线程回调
                int code = HttpInfo.CheckNet;
                if(e instanceof UnknownHostException){
                    if(!helperInfo.getOkHttpUtil().isNetworkAvailable()){
                        code = HttpInfo.CheckNet;
                    }else{
                        code = HttpInfo.CheckURL;
                    }
                } else if(e instanceof SocketTimeoutException){
                    if(null != e.getMessage()){
                        if(e.getMessage().contains("failed to connect to"))
                            code = HttpInfo.ConnectionTimeOut;
                        if(e.getMessage().equals("timeout"))
                            code = HttpInfo.WriteAndReadTimeOut;
                    }
                }
                Message msg =  new CallbackMessage(OkMainHandler.RESPONSE_CALLBACK,
                        callback,
                        retInfo(info,code,"["+e.getMessage()+"]"),
                        requestTag,
                        call)
                        .build();
                OkMainHandler.getInstance().sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                //主线程回调
                Message msg =  new CallbackMessage(OkMainHandler.RESPONSE_CALLBACK,
                        callback,
                        dealResponse(helper,res,call),
                        requestTag,
                        call)
                        .build();
                OkMainHandler.getInstance().sendMessage(msg);
            }
        });
    }

    /**
     * 检查请求URL
     */
    private boolean checkUrl(String url){
        HttpUrl parsed = HttpUrl.parse(url);
        return parsed != null && !TextUtils.isEmpty(url);
    }

    /**
     * 构建Request
     */
    private Request buildRequest(HttpInfo info, @RequestType int method, ProgressCallback progressCallback){
        Request request;
        Request.Builder requestBuilder = new Request.Builder();
        final String url = info.getUrl();
        if(method == RequestType.GET){
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
        }else{
            RequestBody requestBody = matchContentType(info,requestBuilder,progressCallback);
            ProgressRequestBody progress = new ProgressRequestBody(requestBody,progressCallback,timeStamp,requestTag);
            if(method == RequestType.POST){
                requestBuilder.url(url).post(progress);
            } else if(method == RequestType.PUT){
                requestBuilder.url(url).put(progress);
            } else if(method == RequestType.DELETE){
                requestBuilder.url(url).delete(progress);
            } else{
                requestBuilder.url(url).post(progress);
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            requestBuilder.addHeader("Connection", "close");
        }
        addHeadsToRequest(info,requestBuilder);
        request = requestBuilder.build();
        return request;
    }

    private RequestBody matchContentType(HttpInfo info, Request.Builder requestBuilder, ProgressCallback progressCallback){
        final String url = info.getUrl();
        RequestBody requestBody;
        //设置请求参数编码格式
        String requestEncoding = info.getRequestEncoding();
        if(TextUtils.isEmpty(requestEncoding)){
            requestEncoding = ";charset=" + helperInfo.getRequestEncoding().toLowerCase();
        }else{
            requestEncoding = ";charset=" + requestEncoding.toLowerCase();
        }
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
            requestBody = packageFormBody(info,url,requestBuilder);
        }
        return requestBody;
    }

    private RequestBody packageFormBody(HttpInfo info,String url,Request.Builder requestBuilder){
        FormBody.Builder builder = new FormBody.Builder();
        if(null != info.getParams() && !info.getParams().isEmpty()){
            StringBuilder log = new StringBuilder("Params: ");
            String value;
            for (String key : info.getParams().keySet()) {
                value = info.getParams().get(key);
                value = value == null ? "" : value;
                builder.add(key, value);
                log.append(key).append("=").append(value).append(" | ");
            }
            int point = log.lastIndexOf("|");
            if(point != -1){
                log.deleteCharAt(point);
            }
            showLog(log.toString());
        }
        return builder.build();
    }

    private void showUrlLog(Request request){
        startTime = System.nanoTime();
        showLog(String.format("%s-URL: %s %n",request.method(),request.url()));
    }

    /**
     * 处理HTTP响应
     */
    private HttpInfo dealResponse(OkHttpHelper helper,Response res,Call call){
        showLog(String.format(Locale.getDefault(),"CostTime: %.3fs",(System.nanoTime()-startTime)/1e9d));
        final HttpInfo info = httpInfo;
        BufferedReader bufferedReader = null ;
        String result = "";
        try {
            if(null != res){
                final int netCode = res.code();
                if(res.isSuccessful()){
                    if(helper.getBusinessType() == BusinessType.HttpOrHttps
                            || helper.getBusinessType() == BusinessType.UploadFile){
                        //服务器响应编码格式
                        String encoding = info.getResponseEncoding();
                        if(TextUtils.isEmpty(encoding)){
                            encoding = helper.getResponseEncoding();
                        }
                        bufferedReader = new BufferedReader(new InputStreamReader(res.body().byteStream(), encoding));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        return retInfo(info,netCode,HttpInfo.SUCCESS,result);
                    }else if(helper.getBusinessType() == BusinessType.DownloadFile){ //下载文件
                        return helper.getDownUpLoadHelper().downloadingFile(helper,res,call);
                    }
                }else{
                    showLog("HttpStatus: "+netCode);
                    if(netCode == 400){
                        return retInfo(info,netCode,HttpInfo.RequestParamError);
                    }else if(netCode == 404){//请求页面路径错误
                        return retInfo(info,netCode,HttpInfo.ServerNotFound);
                    }else if(netCode == 416) {//请求数据流范围错误
                        return retInfo(info, netCode, HttpInfo.Message, "请求Http数据流范围错误\n" + result);
                    }else if(netCode == 500) {//服务器内部错误
                        return retInfo(info, netCode, HttpInfo.NoResult);
                    }else if(netCode == 502) {//错误的网关
                        return retInfo(info, netCode, HttpInfo.GatewayBad);
                    }else if(netCode == 504) {//网关超时
                        return retInfo(info,netCode,HttpInfo.GatewayTimeOut);
                    }else {
                        return retInfo(info,netCode,HttpInfo.CheckNet);
                    }
                }
            }
            return retInfo(info,HttpInfo.CheckURL);
        } catch (Exception e) {
            return retInfo(info,HttpInfo.NoResult,"["+e.getMessage()+"]");
        } finally {
            if(null != res){
                res.close();
            }
            if(null != bufferedReader){
                try {
                    bufferedReader.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
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
    void responseCallback(HttpInfo info, ProgressCallback progressCallback, int code,String requestTag){
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
     * 添加请求头参数
     */
    Request.Builder addHeadsToRequest(HttpInfo info, Request.Builder requestBuilder){
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
        return requestBuilder;
    }




}
