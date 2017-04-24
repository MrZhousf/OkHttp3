package com.okhttplib.util;

import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具类
 * 支持MD5加密、文件的MD5校验码获取
 * @author zhousf
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

    /**
     * 获取文件的MD5
     * @param filePath 文件路径
     */
    public static String getFileMd5(String filePath){
        if(TextUtils.isEmpty(filePath)){
            return "";
        }
        return getFileMd5(new File(filePath));
    }

    /**
     * 获取文件的MD5
     * @param file 文件
     */
    public static String getFileMd5(File file) {
        MessageDigest messageDigest;
        RandomAccessFile randomAccessFile = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            if (file == null) {
                return "";
            }
            if (!file.exists()) {
                return "";
            }
            randomAccessFile=new RandomAccessFile(file,"r");
            byte[] bytes=new byte[1024*1024*10];
            int len = 0;
            while ((len=randomAccessFile.read(bytes))!=-1){
                messageDigest.update(bytes,0, len);
            }
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }



}
