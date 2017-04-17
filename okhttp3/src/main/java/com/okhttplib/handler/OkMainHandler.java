package com.okhttplib.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.okhttplib.HttpInfo;
import com.okhttplib.bean.CallbackMessage;
import com.okhttplib.bean.DownloadMessage;
import com.okhttplib.bean.ProgressMessage;
import com.okhttplib.bean.UploadMessage;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.BaseCallback;
import com.okhttplib.callback.CallbackOk;

import okhttp3.Call;

/**
 * 主线程Handler
 * @author zhousf
 */
public class OkMainHandler extends Handler {

    private static OkMainHandler singleton;

    public static OkMainHandler getInstance(){
        if(null == singleton){
            synchronized (OkMainHandler.class){
                if(null == singleton)
                    singleton = new OkMainHandler();
            }
        }
        return singleton;
    }

    private OkMainHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 网络请求回调标识
     */
    public static final int RESPONSE_CALLBACK = 0x01;

    /**
     * 进度回调标识
     */
    public static final int PROGRESS_CALLBACK = 0x02;

    /**
     * 上传结果回调标识
     */
    public static final int RESPONSE_UPLOAD_CALLBACK = 0x03;

    /**
     * 下载结果回调标识
     */
    public static final int RESPONSE_DOWNLOAD_CALLBACK = 0x04;


    @Override
    public void handleMessage(Message msg) {
        final int what = msg.what;
        try {
            switch (what){
                case RESPONSE_CALLBACK://网络请求
                    CallbackMessage callMsg = (CallbackMessage) msg.obj;
                    if(null != callMsg.callback){
                        //开始回调
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(callMsg.requestTag)){
                            BaseCallback callback = callMsg.callback;
                            if(callback instanceof CallbackOk){
                                ((CallbackOk)callback).onResponse(callMsg.info);
                            } else if(callback instanceof com.okhttplib.callback.Callback){
                                HttpInfo info = callMsg.info;
                                if(info.isSuccessful()){
                                    ((com.okhttplib.callback.Callback)callback).onSuccess(info);
                                }else{
                                    ((com.okhttplib.callback.Callback)callback).onFailure(info);
                                }
                            }
                        }
                    }
                    Call call = callMsg.call;
                    if (call != null) {
                        if(!call.isCanceled()){
                            call.cancel();
                        }
                        BaseActivityLifecycleCallbacks.cancel(callMsg.requestTag,call);
                    }
                    break;
                case PROGRESS_CALLBACK://进度回调
                    ProgressMessage proMsg = (ProgressMessage) msg.obj;
                    if(null != proMsg.progressCallback){
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(proMsg.requestTag)){
                            proMsg.progressCallback.onProgressMain(proMsg.percent,proMsg.bytesWritten,proMsg.contentLength,proMsg.done);
                        }
                    }
                    break;
                case RESPONSE_UPLOAD_CALLBACK://上传结果回调
                    UploadMessage uploadMsg = (UploadMessage) msg.obj;
                    if(null != uploadMsg.progressCallback){
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(uploadMsg.requestTag)){
                            uploadMsg.progressCallback.onResponseMain(uploadMsg.filePath,uploadMsg.info);
                        }
                    }
                    break;
                case RESPONSE_DOWNLOAD_CALLBACK://下载结果回调
                    DownloadMessage downloadMsg = (DownloadMessage) msg.obj;
                    if(null != downloadMsg){
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(downloadMsg.requestTag)){
                            downloadMsg.progressCallback.onResponseMain(downloadMsg.filePath,downloadMsg.info);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
