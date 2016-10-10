package com.okhttplib.interceptor;

import com.okhttplib.HttpInfo;

/**
 * @author: zhousf
 * 请求链路异常（非业务逻辑）拦截器
 */
public interface ExceptionInterceptor {

    HttpInfo intercept(HttpInfo info) throws Exception;

}
