package com.okhttplib.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * MIME媒体类型
 * @author zhousf
 */
@StringDef({
        MineType.FORM,
        MineType.JSON,
        MineType.STREAM,
        MineType.MARKDOWN,
        MineType.XML,
        MineType.FORM_DATA
})
@Retention(RetentionPolicy.SOURCE)
public @interface MineType {

    /**
     * 表单
     */
    String FORM = "application/x-www-form-urlencoded";

    /**
     * JSON
     */
    String JSON = "application/json; charset=utf-8";

    /**
     * 二进制流
     */
    String STREAM = "application/octet-stream";

    /**
     * 文档
     */
    String MARKDOWN = "text/x-markdown; charset=utf-8";

    /**
     * XML
     */
    String XML = "text/xml; charset=utf-8";


    /**
     * 文件表单
     */
    String FORM_DATA = "multipart/form-data";

}