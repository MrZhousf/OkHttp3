package com.okhttplib.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 缓存等级
 * @author zhousf
 */
@IntDef({CacheLevel.FIRST_LEVEL, CacheLevel.SECOND_LEVEL, CacheLevel.THIRD_LEVEL, CacheLevel.FOURTH_LEVEL})
@Retention(RetentionPolicy.SOURCE)
public @interface CacheLevel {

    /**
     * 无缓存
     */
    int FIRST_LEVEL = 1;

    /**
     * 15秒(缓存有效时间)
     */
    int SECOND_LEVEL = 2;

    /**
     * 30秒(缓存有效时间)
     */
    int THIRD_LEVEL = 3;

    /**
     * 60秒(缓存有效时间)
     */
    int FOURTH_LEVEL = 4;
}