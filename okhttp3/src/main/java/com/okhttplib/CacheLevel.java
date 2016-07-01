package com.okhttplib;

import android.support.annotation.IntDef;

/**
 * 缓存等级
 */
@IntDef({CacheLevel.FIRST_LEVEL, CacheLevel.SECOND_LEVEL, CacheLevel.THIRD_LEVEL, CacheLevel.FOURTH_LEVEL})
public @interface CacheLevel {
    int FIRST_LEVEL = 1; //默认无缓存
    int SECOND_LEVEL = 2; //缓存存活时间为15秒
    int THIRD_LEVEL = 3; //30秒
    int FOURTH_LEVEL = 4; //60秒
}