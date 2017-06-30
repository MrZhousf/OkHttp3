package base.networkstate;

/**
 * Author : zhousf
 * Description :
 * Date : 2017/6/30.
 */
public class NetInfo {


    /** 网络是否可用 **/
    public boolean isNetworkAvailable;
    /** 网络类型 **/
    public String networkType;

    public static final String WIFI = "WIFI";//热点网络
    public static final String CM_NET = "CMNET";//移动网络
    public static final String CM_WAP = "CMWAP";//移动网络
    public static final String NONE_NET = "NONENET";//无网络

    public NetInfo(boolean isNetworkAvailable, String networkType) {
        this.isNetworkAvailable = isNetworkAvailable;
        this.networkType = networkType;
    }

    public boolean equal(NetInfo netInfo){
        if(netInfo == null){
            return false;
        }
        if(netInfo.isNetworkAvailable == this.isNetworkAvailable
                && netInfo.networkType.equals(this.networkType)){
            return true;
        }
        return false;
    }

    public boolean equal(boolean isNetworkAvailable,String networkType){
        if(isNetworkAvailable == this.isNetworkAvailable
                && networkType.equals(this.networkType)){
            return true;
        }else{
            return false;
        }
    }


}
