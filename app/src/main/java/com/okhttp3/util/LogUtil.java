package com.okhttp3.util;

import android.util.Log;

/**
 * 日志打印
 * @author zhousf
 */
public class LogUtil {

    /** 是否开启debug模式 */
    public static boolean isDebug = true;

    public LogUtil() {
    }

    /**
     * 错误
     */
    public static void e(Class<?> clazz, String msg){
        if(isDebug){
            Log.e(clazz.getSimpleName(), msg);
        }
    }

    public static void e(String clazzName, String msg){
        if(isDebug){
            Log.e(clazzName, msg);
        }
    }

    /**
     *  信息
     */
    public static void i(Class<?> clazz, String msg){
        if(isDebug){
            Log.i(clazz.getSimpleName(), msg);
        }
    }

    public static void i(String clazzName, String msg){
        if(isDebug){
            Log.i(clazzName, msg);
        }
    }

    /**
     * 警告
     */
    public static void w(Class<?> clazz, String msg){
        if(isDebug){
            Log.w(clazz.getSimpleName(), msg);
        }
    }

    public static void w(String clazzName, String msg){
        if(isDebug){
            Log.w(clazzName, msg);
        }
    }

    /**
     * 测试
     */
    public static void d(Class<?> clazz, String msg){
        if(isDebug){
            Log.d(clazz.getSimpleName(), msg);
        }
    }

    public static void d(String clazzName, String msg){
        if(isDebug){
            Log.d(clazzName, msg);
        }
    }
}
