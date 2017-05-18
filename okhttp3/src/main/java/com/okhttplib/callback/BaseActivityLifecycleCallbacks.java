package com.okhttplib.callback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;

/**
 * Activity声明周期回调
 * @author zhousf
 */
public class BaseActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ActivityLifecycle";

    /**
     * 是否显示ActivityLifecycle日志
     */
    private static boolean showLifecycleLog;

    /**
     * 请求集合: key=Activity value=Call集合
     */
    private static Map<String,SparseArray<Call>> callsMap = new ConcurrentHashMap<>();

    /**
     * 保存请求集合
     * @param tag 请求标识
     * @param call 请求
     */
    public static void putCall(String tag, Call call){
        if(null != tag){
            SparseArray<Call> callList = callsMap.get(tag);
            if(null == callList){
                callList = new SparseArray<>();
            }
            callList.put(call.hashCode(),call);
            callsMap.put(tag,callList);
            showLog(false,tag);
        }

    }

    /**
     * 取消请求
     * @param tag 请求标识
     */
    private static void cancelCallByActivityDestroy(String tag){
        if(null == tag)
            return ;
        SparseArray<Call> callList = callsMap.get(tag);
        if(null != callList){
            final int len = callList.size();
            for(int i=0;i<len;i++){
                Call call = callList.valueAt(i);
                if(null != call &&!call.isCanceled())
                    call.cancel();
            }
            callList.clear();
            callsMap.remove(tag);
            showLog(true,tag);
        }
    }

    /**
     * 判断当前Activity是否已经销毁
     * @param activity 请求标识
     * @return true 已经销毁  false 未销毁
     */
    public static boolean isActivityDestroyed(Activity activity){
        String tag = activity.getClass().getName();
        return callsMap.get(tag) == null;
    }

    /**
     * 判断当前tat是否已经销毁
     * @param tag 请求标识
     * @return true 已经销毁  false 未销毁
     */
    public static boolean isActivityDestroyed(String tag){
        return !TextUtils.isEmpty(tag) && callsMap.get(tag) == null;
    }

    /**
     * 取消请求
     * @param tag 请求标识
     */
    public static void cancel(String tag){
        cancel(tag,null);
    }

    /**
     * 取消请求
     * @param tag 请求标识
     * @param originalCall call
     */
    public static void cancel(String tag, Call originalCall){
        if(TextUtils.isEmpty(tag)){
            return ;
        }
        if(null != originalCall){
            SparseArray<Call> callList = callsMap.get(tag);
            if(null != callList){
                Call c = callList.get(originalCall.hashCode());
                if(null != c && !c.isCanceled())
                    c.cancel();
                callList.delete(originalCall.hashCode());
//                if(callList.size() == 0)
//                    callsMap.remove(tag);
                showLog(true,tag);
            }
        }else{
            SparseArray<Call> callList = callsMap.get(tag);
            if(null != callList){
                for(int i=0 ;i<callList.size();i++){
                    Call call = callList.valueAt(i);
                    if(null != call && !call.isCanceled()){
                        call.cancel();
                        callList.delete(call.hashCode());
                    }
//                    if(callList.size() == 0)
//                        callsMap.remove(tag);
                    showLog(true,tag);
                }
            }
        }
    }

    private static void showLog(boolean isCancel, String tag){
        if(!showLifecycleLog){
            return;
        }
        String callDetail = isCancel ? "取消请求": "增加请求";
        Log.d(TAG,callDetail+": "+tag);
    }


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
        cancelCallByActivityDestroy(activity.getClass().getName());
    }

    public static void setShowLifecycleLog(boolean showLifecycle) {
        showLifecycleLog = showLifecycle;
    }
}
