package com.okhttplib.util;

import java.security.MessageDigest;

/**
 * 加密工具类
 * 支持MD5加密
 */
public class EncryptUtil {

    /**
     * MD5加密：生成16位密文
     * @param originString 加密字符串
     * @param isUpperCase 是否生成大写密文
     */
    public static String MD5StringTo16Bit(String originString,boolean isUpperCase) throws Exception{
        String result = MD5StringTo32Bit(originString,isUpperCase);
        if(result.length() == 32){
            return result.substring(8,24);
        }
        return "";
    }

    /**
     * MD5加密：生成32位密文
     * @param originString 加密字符串
     * @param isUpperCase 是否生成大写密文
     */
    public static String MD5StringTo32Bit(String originString,boolean isUpperCase) throws Exception{
        String result = "";
        if (originString != null) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte bytes[] = md.digest(originString.getBytes());
            for (int i = 0; i < bytes.length; i++) {
                String str = Integer.toHexString(bytes[i] & 0xFF);
                if (str.length() == 1) {
                    str += "F";
                }
                result += str;
            }
        }
        if(isUpperCase){
            return result.toUpperCase();
        }else{
            return result.toLowerCase();
        }
    }


}
