package base;

import android.app.Application;

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
        OkHttpUtil.init(baseApplication)
                .setConnectTimeout(40)
                .setShowLifecycleLog(true)
                .setShowHttpLog(false)
                .build();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }



}
