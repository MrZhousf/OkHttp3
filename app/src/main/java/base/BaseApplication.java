package base;

import android.app.Application;

import com.okhttplib.CacheLevel;
import com.okhttplib.CacheType;
import com.okhttplib.OkHttpUtil;

public class BaseApplication extends Application {

    public static BaseApplication baseApplication;

    public static BaseApplication getApplication() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        OkHttpUtil.init(this)
                .setConnectTimeout(30)//超时时间设置
                .setMaxCacheSize(10 * 1024 * 1024)//设置缓存空间大小
                .setCacheLevel(CacheLevel.FIRST_LEVEL)//缓存等级
                .setCacheType(CacheType.NETWORK_THEN_CACHE)//缓存类型
                .setShowHttpLog(true)//显示请求日志
                .setShowLifecycleLog(false)
                .setRetryOnConnectionFailure(true)
                .build();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }



}
