package com.okhttplib.helper;

import android.util.Log;

import com.okhttplib.bean.HelperInfo;

/**
 * 日志打印辅助类
 * @author: zhousf
 */
public class LogHelper {

    private String TAG;
    private long timeStamp;
    private boolean showHttpLog;//是否显示Http请求日志

    private static LogHelper helper;

    public static LogHelper get(){
        if(null == helper){
            synchronized (LogHelper.class){
                if(null == helper)
                    helper = new LogHelper();
            }
        }
        return helper;
    }

    private LogHelper() {
    }

    public void init(HelperInfo helperInfo){
        TAG = helperInfo.getLogTAG();
        timeStamp = helperInfo.getTimeStamp();
        showHttpLog = helperInfo.isShowHttpLog();
    }

    /**
     * 打印日志
     * @param msg 日志信息
     */
    public void showLog(String msg){
        if(showHttpLog)
            Log.d(TAG+"["+timeStamp+"]", msg);
    }


}
