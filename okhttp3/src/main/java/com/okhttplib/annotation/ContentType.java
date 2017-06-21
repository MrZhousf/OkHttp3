package com.okhttplib.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 内容类型
 * @author zhousf
 */
@StringDef({
        ContentType.FORM,
        ContentType.JSON,
        ContentType.STREAM,
        ContentType.MARKDOWN,
        ContentType.XML,
        ContentType.FORM_DATA
})
@Retention(RetentionPolicy.SOURCE)
public @interface ContentType {

    /**
     * 表单
     */
    String FORM = "application/x-www-form-urlencoded";

    /**
     * JSON
     */
    String JSON = "application/json";

    /**
     * 二进制流
     */
    String STREAM = "application/octet-stream";

    /**
     * 文档
     */
    String MARKDOWN = "text/x-markdown";

    /**
     * XML
     */
    String XML = "text/xml";


    /**
     * 文件表单
     */
    String FORM_DATA = "multipart/form-data";

}