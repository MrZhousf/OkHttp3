package com.okhttplib;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.Method;

/**
 * Author : zhousf
 * Description : 通过反射获取Application上下文
 * Date : 2017/11/29.
 */
public class OkApplication{

    @SuppressLint("StaticFieldLeak")
    private static Application application;

    public static Application get(){
        if(application == null){
            synchronized (OkApplication.class){
                if(application == null){
                    new OkApplication();
                }
            }
        }
        return application;
    }

    @SuppressWarnings("all")
    private OkApplication(){
        Object activityThread;
        try {
            Class acThreadClass = Class.forName("android.app.ActivityThread");
            if(acThreadClass == null)
                return;
            Method acThreadMethod = acThreadClass.getMethod("currentActivityThread");
            if(acThreadMethod == null){
                return;
            }
            acThreadMethod.setAccessible(true);
            activityThread = acThreadMethod.invoke(null);
            Method applicationMethod = activityThread.getClass().getMethod("getApplication");
            if(applicationMethod == null){
                return;
            }
            Object app = applicationMethod.invoke(activityThread);
            application = (Application) app;
        } catch (Throwable e){
            e.printStackTrace();
        }
    }




}
