package com.okhttplib.helper;

import android.os.Build;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.BusinessType;
import com.okhttplib.annotation.RequestType;
import com.okhttplib.bean.CallbackMessage;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.BaseCallback;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;
import com.okhttplib.progress.ProgressRequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.okhttplib.HttpInfo.ConnectionTimeOut;
import static com.okhttplib.HttpInfo.WriteAndReadTimeOut;

/**
 * Http网络请求业务类
 * @author zhousf
 */
class HttpHelper extends BaseHelper{

    private long startTime;

    HttpHelper(HelperInfo helperInfo) {
        super(helperInfo);
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
            return dealResponse(helper, res);
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
            //普通网络请求结束时自动取消请求，文件下载或上传需要在异步返回时取消，避免因提前取消请求导致无法回调的问题
            if(helper.getBusinessType() == BusinessType.HttpOrHttps){
                BaseActivityLifecycleCallbacks.cancel(requestTag,call);
            }
        }
    }

    /**
     * 异步请求
     */
    void doRequestAsync(final OkHttpHelper helper){
        if(httpInfo == null)
            return ;
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
                    code = HttpInfo.ConnectionTimeOut;
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
                        dealResponse(helper,res),
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
            RequestBody requestBody = matchContentType(info,null);
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


    private void showUrlLog(Request request){
        startTime = System.nanoTime();
        showLog(String.format("%s-URL: %s %n",request.method(),request.url()));
    }

    /**
     * 处理HTTP响应
     */
    private HttpInfo dealResponse(OkHttpHelper helper,Response res){
        showLog(String.format(Locale.getDefault(),"CostTime: %.3fs",(System.nanoTime()-startTime)/1e9d));
        BufferedReader bufferedReader = null ;
        StringBuilder result = new StringBuilder("");
        try {
            if(null != res){
                final int netCode = res.code();
                if(httpInfo.isNeedResponse()){
                    httpInfo.setResponse(res);
                    result.append("返回结果为Response,请调用getResponse获取相应结果");
                    return retInfo(httpInfo,netCode,HttpInfo.SUCCESS,result.toString());
                }
                if(res.isSuccessful()){
                    if(helper.getBusinessType() == BusinessType.HttpOrHttps
                            || helper.getBusinessType() == BusinessType.UploadFile){
                        //服务器响应编码格式
                        String encoding = httpInfo.getResponseEncoding();
                        if(TextUtils.isEmpty(encoding)){
                            encoding = helper.getResponseEncoding();
                        }
                        ResponseBody body = res.body();
                        if(body != null){
                            bufferedReader = new BufferedReader(new InputStreamReader(body.byteStream(), encoding));
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                result.append(line);
                            }
                        }
                        //200即业务成功
                        return retInfo(httpInfo,netCode,HttpInfo.SUCCESS,result.toString());
                    }else if(helper.getBusinessType() == BusinessType.DownloadFile){ //下载文件
                        return helper.getDownUpLoadHelper().downloadingFile(helper,res);
                    }
                }else{
                    showLog("HttpStatus: "+netCode);
                    if(netCode == 400){
                        return retInfo(httpInfo,netCode,HttpInfo.RequestParamError);
                    }else if(netCode == 404){//请求页面路径错误
                        return retInfo(httpInfo,netCode,HttpInfo.ServerNotFound);
                    }else if(netCode == 416) {//请求数据流范围错误
                        return retInfo(httpInfo, netCode, HttpInfo.Message, "请求Http数据流范围错误\n" + result.toString());
                    }else if(netCode == 500) {//服务器内部错误
                        return retInfo(httpInfo, netCode, HttpInfo.NoResult);
                    }else if(netCode == 502) {//错误的网关
                        return retInfo(httpInfo, netCode, HttpInfo.GatewayBad);
                    }else if(netCode == 504) {//网关超时
                        return retInfo(httpInfo,netCode,HttpInfo.GatewayTimeOut);
                    }else {
                        return retInfo(httpInfo,netCode,HttpInfo.CheckNet);
                    }
                }
            }
            return retInfo(httpInfo,HttpInfo.CheckURL);
        } catch (Exception e) {
            return retInfo(httpInfo,HttpInfo.NoResult,"["+e.getMessage()+"]");
        } finally {
            if(!httpInfo.isNeedResponse()){
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
    }





}
