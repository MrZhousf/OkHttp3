package base.networkstate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 检测当前环境的网络状态
 * Author : zhousf
 * 静态注册：
 * <receiver android:name="core.networkstate.NetworkStateReceiver" >
 *  <intent-filter>
 *     <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
 *     <action android:name="android.gzcpc.conn.CONNECTIVITY_CHANGE" />
 *  </intent-filter>
 * </receiver>
 * 动态注册：
 * registerNetworkStateReceive/unRegisterNetworkStateReceiver
 * 权限：
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    private static BroadcastReceiver broadcastReceiver;
    private static ArrayList<NetworkStateListener> networkStateListenerList = new ArrayList<NetworkStateListener>();
    private final static String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static LinkedList<NetInfo> list = new LinkedList<>();


    public static BroadcastReceiver getBroadcastReceiver(){
        return broadcastReceiver == null ? broadcastReceiver = new NetworkStateReceiver() : broadcastReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(broadcastReceiver == null)
            broadcastReceiver = NetworkStateReceiver.this;
        if (intent.getAction().equalsIgnoreCase(ANDROID_NET_CHANGE_ACTION)) {
            boolean isNetworkAvailable = NetworkUtil.isNetworkAvailable(context);
            String networkType = NetworkUtil.getNetworkType(context);
            NetInfo netInfo  = new NetInfo(isNetworkAvailable,networkType);
            if(!list.isEmpty()){
                if(!list.getLast().equal(isNetworkAvailable,networkType)){
                    list.add(netInfo);
                    notifyListener(isNetworkAvailable,netInfo);
                }
            }else {
                list.add(netInfo);
                notifyListener(isNetworkAvailable,netInfo);
            }
        }
    }


    /**
     * 通知网络状态监听器
     */
    private void notifyListener(boolean isNetworkAvailable,NetInfo netInfo) {
        for (int i = 0; i < networkStateListenerList.size(); i++) {
            final NetworkStateListener listener = networkStateListenerList.get(i);
            if (null != listener) {
                listener.onNetworkState(isNetworkAvailable, netInfo);
            }
        }
    }

    /**
     * 添加网络状态监听
     */
    public static void addNetworkStateListener(NetworkStateListener observer) {
        if (null == networkStateListenerList) {
            networkStateListenerList = new ArrayList<NetworkStateListener>();
        }
        networkStateListenerList.add(observer);
    }

    /**
     * 移除网络状态监听
     */
    public static void removeNetworkStateListener(NetworkStateListener observer) {
        if (null != networkStateListenerList) {
            networkStateListenerList.remove(observer);
        }
    }


    /**
     * 注册网络状态广播
     */
    public static void registerNetworkStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ANDROID_NET_CHANGE_ACTION);
        context.getApplicationContext().registerReceiver(getBroadcastReceiver(), filter);
    }


    /**
     * 注销网络状态广播
     */
    public static void unRegisterNetworkStateReceiver(Context context) {
        if (null != broadcastReceiver) {
            try {
                context.getApplicationContext().unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
