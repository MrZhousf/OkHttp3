package com.okhttplib;

import android.app.Activity;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Http请求实体类
 */
public class HttpInfo {

    //**请求参数定义**/
    private String url;//请求地址
    private Map<String,String> params;//请求参数

    //**响应返回参数定义**/
    private int retCode;//返回码
    private String retDetail;//返回结果

    private Class<?> tag;

    public HttpInfo(Builder builder) {
        this.url = builder.url;
        this.params = builder.params;
        this.tag = builder.tag;
    }

    public static Builder Builder() {
        return new Builder();
    }


    public static final class Builder {

        private String url;
        private Map<String,String> params;
        private Class<?> tag;


        public Builder() {
        }

        public HttpInfo build(Object object){
            setTag(object);
            return new HttpInfo(this);
        }

        public HttpInfo build(){
            return new HttpInfo(this);
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder addParams(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder addParam(String key, String value){
            if(null == this.params)
                this.params = new HashMap<String,String>();
            if(!TextUtils.isEmpty(key))
                this.params.put(key,value);
            return this;
        }

        public Builder setTag(Object object) {
            if(object instanceof Activity){
                Activity activity = (Activity) object;
                this.tag = activity.getClass();
            }
            if(object instanceof android.support.v4.app.Fragment){
                android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) object;
                this.tag = fragment.getActivity().getClass();
            }
            if(object instanceof android.app.Fragment){
                android.app.Fragment fragment = (android.app.Fragment) object;
                this.tag = fragment.getActivity().getClass();
            }
            return this;
        }

    }


    //**请求返回常量定义**/
    final int NonNetwork = 1;
    final String NonNetwork_Detail = "网络中断";
    final int SUCCESS = 2;
    final String SUCCESS_Detail = "发送请求成功";
    final int ProtocolException = 3;
    final String ProtocolException_Detail = "请检查协议类型是否正确";
    final int NoResult = 4;
    final String NoResult_Detail = "无法获取返回信息(服务器内部错误)";
    final int CheckURL = 5;
    final String CheckURL_Detail = "请检查请求地址是否正确";
    final int CheckNet = 6;
    final String CheckNet_Detail = "请检查网络连接是否正常";
    final int ConnectionTimeOut = 7;
    final String ConnectionTimeOut_Detail = "连接超时";
    final int WriteAndReadTimeOut = 8;
    final String WriteAndReadTimeOut_Detail = "读写超时";

    public HttpInfo packInfo(int retCode, String retDetail){
        this.retCode = retCode;
        switch (retCode){
            case NonNetwork:
                this.retDetail = NonNetwork_Detail;
                break;
            case SUCCESS:
                this.retDetail = SUCCESS_Detail;
                break;
            case ProtocolException:
                this.retDetail = ProtocolException_Detail;
                break;
            case NoResult:
                this.retDetail = NoResult_Detail;
                break;
            case CheckURL:
                this.retDetail = CheckURL_Detail;
                break;
            case CheckNet:
                this.retDetail = CheckNet_Detail;
                break;
            case ConnectionTimeOut:
                this.retDetail = ConnectionTimeOut_Detail;
                break;
            case WriteAndReadTimeOut:
                this.retDetail = WriteAndReadTimeOut_Detail;
                break;
        }
        if(!TextUtils.isEmpty(retDetail)){
            this.retDetail = retDetail;
        }
        return this;
    }

    public boolean isSuccessful(){
        return this.retCode == SUCCESS;
    }

    public String getUrl() {
        return url;
    }

    public String getRetDetail() {
        return retDetail;
    }

    public <T> T getRetDetail(Class<T> clazz){
        return new Gson().fromJson(retDetail, clazz);
    }

    public void setRetDetail(String retDetail) {
        this.retDetail = retDetail;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Class<?> getTag() {
        return tag;
    }
}
