package com.okhttplib.interceptor;

import com.okhttplib.HttpInfo;

/**
 * 请求链路异常（非业务逻辑）拦截器
 * @author zhousf
 */
public interface ExceptionInterceptor {

    HttpInfo intercept(HttpInfo info) throws Exception;

}
