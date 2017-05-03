package com.okhttplib.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 缓存类型
 * @author zhousf
 */
@IntDef({CacheType.FORCE_NETWORK, CacheType.FORCE_CACHE, CacheType.NETWORK_THEN_CACHE, CacheType.CACHE_THEN_NETWORK})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheType {

    /**
     * 仅网络
     */
    int FORCE_NETWORK = 1;

    /**
     * 仅缓存
     */
    int FORCE_CACHE = 2;

    /**
     * 先网络再缓存
     */
    int NETWORK_THEN_CACHE = 3;

    /**
     * 先缓存再网络
     */
    int CACHE_THEN_NETWORK = 4;
}