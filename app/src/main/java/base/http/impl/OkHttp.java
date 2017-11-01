package base.http.impl;

import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.Callback;

import java.io.IOException;

import base.http.HttpCallback;
import base.http.HttpEntity;
import base.http.HttpInterface;

/**
 * Author : zhousf
 * Description : OkHttp网络请求底层库
 * Date : 2017/10/31.
 */
public class OkHttp implements HttpInterface{

    @Override
    public HttpEntity doGetSync(HttpEntity httpEntity) {
        HttpInfo info = OkHttpUtil.getDefault().doGetSync(switchHttpInfo(httpEntity));
        return switchHttpEntity(info,httpEntity);
    }

    @Override
    public void doGetAsync(final HttpEntity httpEntity, final HttpCallback callback) {
        OkHttpUtil.getDefault().doGetAsync(switchHttpInfo(httpEntity), new Callback() {
            @Override
            public void onSuccess(HttpInfo info) throws IOException {
                callback.onSuccess(switchHttpEntity(info,httpEntity));
            }

            @Override
            public void onFailure(HttpInfo info) throws IOException {
                callback.onFailure(switchHttpEntity(info,httpEntity));
            }
        });
    }

    @Override
    public HttpEntity doPostSync(HttpEntity httpEntity) {
        HttpInfo info = OkHttpUtil.getDefault().doPostSync(switchHttpInfo(httpEntity));
        return switchHttpEntity(info,httpEntity);
    }

    @Override
    public void doPostAsync(final HttpEntity httpEntity,final HttpCallback callback) {
        OkHttpUtil.getDefault().doPostAsync(switchHttpInfo(httpEntity), new Callback() {
            @Override
            public void onSuccess(HttpInfo info) throws IOException {
                callback.onSuccess(switchHttpEntity(info,httpEntity));
            }

            @Override
            public void onFailure(HttpInfo info) throws IOException {
                callback.onFailure(switchHttpEntity(info,httpEntity));
            }
        });
    }



    private HttpInfo switchHttpInfo(HttpEntity info){
        return HttpInfo.Builder()
                .setUrl(info.getUrl())
                .addParams(info.getParams())
                .addParamBytes(info.getParamBytes())
                .addParamForm(info.getParamForm())
                .addParamFile(info.getParamFile())
                .setHttpsCertificate(info.getHttpsCertificate())
                .setHttpsCertificate(info.getHttpsCertificateStream())
                .build();
    }

    private HttpEntity switchHttpEntity(HttpInfo info,HttpEntity httpEntity){
        httpEntity.setRetCode(info.getRetCode());
        httpEntity.setRetDetail(info.getRetDetail());
        httpEntity.setNetCode(info.getNetCode());
        return httpEntity;
    }

}
