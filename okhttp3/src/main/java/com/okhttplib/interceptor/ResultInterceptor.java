package com.okhttplib.interceptor;

import com.okhttplib.HttpInfo;

/**
 * 请求结果拦截器
 * @author zhousf
 */
public interface ResultInterceptor {

    HttpInfo intercept(HttpInfo info) throws Exception;

}
