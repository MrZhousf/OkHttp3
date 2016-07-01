package com.okhttplib;

import android.support.annotation.IntDef;

/**
 * 缓存类型
 */
@IntDef({CacheType.FORCE_NETWORK, CacheType.FORCE_CACHE, CacheType.NETWORK_THEN_CACHE, CacheType.CACHE_THEN_NETWORK})
public @interface CacheType {
    int FORCE_NETWORK = 1;
    int FORCE_CACHE = 2;
    int NETWORK_THEN_CACHE = 3;
    int CACHE_THEN_NETWORK = 4;
}