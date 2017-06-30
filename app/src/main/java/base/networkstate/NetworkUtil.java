package base.networkstate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Author : zhousf
 */
public class NetworkUtil {

    /**
     * 网络是否可用
     */
    public static boolean isNetworkAvailable(Context context){
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo network = cm.getActiveNetworkInfo();
            if(network != null && network.getState() == NetworkInfo.State.CONNECTED){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有网络连接
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = mConnectivityManager
                    .getActiveNetworkInfo();
            if (network != null) {
                return network.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断WIFI网络是否可用
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi != null) {
                return wifi.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断MOBILE网络是否可用
     */
    public static boolean isMobileConnected(Context context){
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobile = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobile != null) {
                return mobile.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取当前网络连接的类型信息
     */
    public static int getNetworkTypeConnected(Context context){
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = mConnectivityManager
                    .getActiveNetworkInfo();
            if (network != null && network.isAvailable()) {
                return network.getType();
            }
        }
        return -1;
    }

    /**
     *
     *获取当前的网络状态
     */
    public static String getNetworkType(Context context){
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network == null) {
            return NetInfo.NONE_NET;
        }
        if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
            return network.getExtraInfo().equalsIgnoreCase("cmnet") ? NetInfo.CM_NET : NetInfo.CM_WAP;
        } else if (network.getType() == ConnectivityManager.TYPE_WIFI) {
            return NetInfo.WIFI;
        }
        return NetInfo.NONE_NET;

    }





}
