package base;

import android.app.Application;

import http.OkHttpUtil;

public class BaseApplication extends Application {

    private OkHttpUtil.BaseActivityLifecycleCallbacks activityLifecycleCallbacks;

    public static BaseApplication baseApplication;

    public static BaseApplication getApplication() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks = new OkHttpUtil.BaseActivityLifecycleCallbacks());
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        super.onTerminate();
    }



}
