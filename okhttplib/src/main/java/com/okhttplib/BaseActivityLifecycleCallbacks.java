package com.okhttplib;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;

/**
 * Activity声明周期回调
 */
public class BaseActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

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
     * @param info 请求信息体
     * @param call 请求
     */
    public static void putCall(HttpInfo info, Call call){
        if(null != info.getTag()){
            SparseArray<Call> callList = callsMap.get(info.getTag());
            if(null == callList){
                callList = new SparseArray<>();
                callList.put(call.hashCode(),call);
                callsMap.put(info.getTag(),callList);
            }else{
                callList.put(call.hashCode(),call);
            }
        }
        showLog(false);
    }

    /**
     * 取消请求
     * @param clazz 上下文
     */
    public static void cancelCall(Class<?> clazz){
        SparseArray<Call> callList = callsMap.get(clazz);
        if(null != callList){
            for(int i=0;i<callList.size();i++){
                Call call = callList.valueAt(i);
                if(!call.isCanceled())
                    call.cancel();
            }
            callList.clear();
            callsMap.remove(clazz);
        }
        showLog(true);
    }

    public static void cancelCall(HttpInfo info, Call call){
        if(null != call){
            SparseArray<Call> callList = callsMap.get(info.getTag());
            if(null != callList){
                Call c = callList.get(call.hashCode());
                if(!c.isCanceled())
                    c.cancel();
                callList.delete(call.hashCode());
                if(callList.size() == 0)
                    callsMap.remove(info.getTag());
            }
        }
        showLog(true);
    }

    public static void showLog(boolean isCancel){
        String callDetail = "增加请求";
        if(isCancel)
            callDetail = "取消请求";
        if(showLifecycleLog){
            if(callsMap.size() > 0){
                for(Map.Entry<Class<?>,SparseArray<Call>> entry : callsMap.entrySet()){
                    Log.d(BaseActivityLifecycleCallbacks.class.getSimpleName(),"###"+callDetail+": size = "+entry.getValue().size()+" ["+entry.getKey().getName()+"]");
                }
            }else{
                Log.d(BaseActivityLifecycleCallbacks.class.getSimpleName(),"###"+callDetail+": size = 0 ");
            }
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
        cancelCall(activity.getClass());
    }

    public static void setShowLifecycleLog(boolean showLifecycle) {
        showLifecycleLog = showLifecycle;
    }
}
