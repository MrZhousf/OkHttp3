package base;

import android.app.Application;
import android.os.Environment;

import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheLevel;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.cookie.PersistentCookieJar;
import com.okhttplib.cookie.cache.SetCookieCache;
import com.okhttplib.cookie.persistence.SharedPrefsCookiePersistor;
import com.okhttplib.interceptor.ExceptionInterceptor;
import com.okhttplib.interceptor.ResultInterceptor;

/**
 * Application
 * 1、初始化全局OkHttpUtil
 * @author zhousf
 */
public class BaseApplication extends Application {

    public static BaseApplication baseApplication;

    public static BaseApplication getApplication() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        String downloadFileDir = Environment.getExternalStorageDirectory().getPath()+"/okHttp_download/";
        OkHttpUtil.init(this)
                .setConnectTimeout(30)//连接超时时间
                .setWriteTimeout(30)//写超时时间
                .setReadTimeout(30)//读超时时间
                .setMaxCacheSize(10 * 1024 * 1024)//缓存空间大小
                .setCacheLevel(CacheLevel.FIRST_LEVEL)//缓存等级
                .setCacheType(CacheType.NETWORK_THEN_CACHE)//缓存类型
                .setShowHttpLog(true)//显示请求日志
                .setShowLifecycleLog(false)//显示Activity销毁日志
                .setRetryOnConnectionFailure(false)//失败后不自动重连
                .setDownloadFileDir(downloadFileDir)//文件下载保存目录
//                .addResultInterceptor(resultInterceptor)//请求结果拦截器
                .addExceptionInterceptor(exceptionInterceptor)//请求链路异常拦截器
                .setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this)))//持久化cookie
                .build();
    }

    private ResultInterceptor resultInterceptor = new ResultInterceptor() {
        @Override
        public HttpInfo intercept(HttpInfo info) throws Exception {
            info.setRetDetail("请求结果拦截器测试");
            return info;
        }
    };

    private ExceptionInterceptor exceptionInterceptor = new ExceptionInterceptor() {
        @Override
        public HttpInfo intercept(HttpInfo info) throws Exception {
            switch (info.getRetCode()){
                case HttpInfo.CheckURL:
                    info.setRetDetail("网络地址错误拦截器测试");
                    break;
                case HttpInfo.ProtocolException:
                    info.setRetDetail("协议类型错误拦截器测试");
                    break;
                case HttpInfo.CheckNet:
                    info.setRetDetail("网络连接是否正常拦截器测试");
                    break;
            }
            return info;
        }
    };


    @Override
    public void onTerminate() {
        super.onTerminate();
    }



}
