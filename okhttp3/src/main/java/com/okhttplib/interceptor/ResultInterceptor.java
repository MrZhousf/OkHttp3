package com.okhttplib.interceptor;

import com.okhttplib.HttpInfo;

/**
 * @author: zhousf
 * 请求结果拦截器
 */
public interface ResultInterceptor {

    HttpInfo intercept(HttpInfo info) throws Exception;

}
