package base;

import android.app.Application;
import android.util.Log;

/**
 * Application
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
        Log.d("BaseApplication","BaseApplication已初始化");
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }



}
