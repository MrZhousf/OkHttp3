package com.okhttplib.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 缓存类型
 */
@IntDef({CacheType.FORCE_NETWORK, CacheType.FORCE_CACHE, CacheType.NETWORK_THEN_CACHE, CacheType.CACHE_THEN_NETWORK})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheType {
    int FORCE_NETWORK = 1;
    int FORCE_CACHE = 2;
    int NETWORK_THEN_CACHE = 3;
    int CACHE_THEN_NETWORK = 4;
}