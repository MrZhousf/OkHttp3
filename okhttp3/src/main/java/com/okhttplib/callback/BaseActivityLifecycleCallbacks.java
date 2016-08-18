package com.okhttplib.callback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.okhttplib.HttpInfo;

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
    private static Map<Class<?>,SparseArray<Call>> callsMap = new ConcurrentHashMap<>();

    /**
     * 保存请求集合
     * @param tag 请求标识
     * @param info 请求信息体
     * @param call 请求
     */
    public static void putCall(Class<?> tag, HttpInfo info, Call call){
        Class<?> t = fetchTag(tag, info);
        if(null != t){
            SparseArray<Call> callList = callsMap.get(t);
            if(null == callList){
                callList = new SparseArray<>();
            }
            callList.put(call.hashCode(),call);
            callsMap.put(t,callList);
            showLog(false);
        }

    }

    /**
     * 取消请求
     * @param tag 请求标识
     */
    private static void cancelCallByActivityDestroy(Class<?> tag){
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
            showLog(true);
        }
    }

    /**
     * 取消请求
     * @param tag 请求标识
     * @param info 请求信息体
     * @param call 请求
     */
    public static void cancelCall(Class<?> tag, HttpInfo info, Call call){
        Class<?> t = fetchTag(tag, info);
        if(null != call && null != t){
            SparseArray<Call> callList = callsMap.get(t);
            if(null != callList){
                Call c = callList.get(call.hashCode());
                if(null != c && !c.isCanceled())
                    c.cancel();
                callList.delete(call.hashCode());
                if(callList.size() == 0)
                    callsMap.remove(t);
                showLog(true);
            }
        }
    }

    private static Class<?> fetchTag(Class<?> tag, HttpInfo info){
        Class<?> t = null;
        if(null != tag)
            t = tag;
        if(null != info.getTag() && null == t)
            t = info.getTag();
        return t;
    }

    private static void showLog(boolean isCancel){
        if(!showLifecycleLog){
            return;
        }
        String callDetail = "增加请求";
        if(isCancel)
            callDetail = "取消请求";
        int originalSize = callsMap.size();
        int rest ;
        if(originalSize > 0){
            for(Map.Entry<Class<?>,SparseArray<Call>> entry : callsMap.entrySet()){
                rest = entry.getValue().size();
                Log.d(TAG,callDetail+": size = "+rest+" ["+entry.getKey().getName()+"]");
            }
        }else{
            Log.d(TAG,callDetail+": size = 0 ");
        }
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
        cancelCallByActivityDestroy(activity.getClass());
    }

    public static void setShowLifecycleLog(boolean showLifecycle) {
        showLifecycleLog = showLifecycle;
    }
}
