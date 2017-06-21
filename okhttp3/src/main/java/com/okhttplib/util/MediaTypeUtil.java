package com.okhttplib.util;

import android.text.TextUtils;

import com.okhttplib.annotation.ContentType;

import okhttp3.MediaType;

/**
 *  媒体类型工具类
 *  @author zhousf
 */
public class MediaTypeUtil {

    /**
     * 根据请求url返回媒体类型
     * @param url 请求地址
     * @param requestEncoding 编码格式
     */
    public static MediaType fetchFileMediaType(String url,String requestEncoding){
        if(!TextUtils.isEmpty(url) && url.contains(".")){
            String extension = url.substring(url.lastIndexOf(".") + 1);
            if("png".equals(extension)){
                extension = "image/png";
            }else if("jpg".equals(extension)){
                extension = "image/jpg";
            }else if("jpeg".equals(extension)){
                extension = "image/jpeg";
            }else if("gif".equals(extension)){
                extension = "image/gif";
            }else if("bmp".equals(extension)){
                extension = "image/bmp";
            }else if("tiff".equals(extension)){
                extension = "image/tiff";
            }else if("ico".equals(extension)){
                extension = "image/ico";
            }else{
                extension = ContentType.FORM_DATA;
            }
            return MediaType.parse(extension+requestEncoding);
        }
        return null;
    }

}
