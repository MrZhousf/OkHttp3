package com.okhttp3.util;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author : zhousf
 * @description : 文件复制工具类：可指定长度复制文件
 * @date : 2017/4/6.
 */

public class FileUtil {

    /**
     * 保存文件的路径
     */
    private final static String saveFileDir = Environment.getExternalStorageDirectory().getPath()+"/okHttp_upload/";

    /**
     * 根据长度截取文件并返回字节数组
     * @param from 复制起始点
     * @param to 复制终点
     * @param file 复制的文件
     * @return 截取后的文件
     */
    public static byte[] copyFileToByte(long from, long to, File file){
        if(to == 0){
            to = file.length();
        }
        if(from > file.length()){
            throw new IllegalArgumentException("from is over size of the file:"+file.getPath());
        }
        long length = to - from;
        byte[] result = new byte[(int)length];
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file,"rw");
            accessFile.seek(from);
            int readSize = accessFile.read(result);
            if (readSize == -1) {
                return null;
            } else if (readSize == length) {
                return result;
            } else {
                byte[] tmpByte = new byte[readSize];
                System.arraycopy(result, 0, tmpByte, 0, readSize);
                return tmpByte;
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 根据长度截取文件并返回字节数组
     * @param from 复制起始点
     * @param to 复制终点
     * @param filePath 复制的文件的地址
     * @return 截取后的文件地址
     */
    public static String copyFile(long from, long to, String filePath){
        File file = new File(filePath);
        if(file.exists()){
            return copyFile(from,to,file);
        }
        return null;
    }


    /**
     * 根据长度截取文件并返回字节数组
     * @param from 复制起始点
     * @param to 复制终点
     * @param file 复制的文件
     * @return 截取后的文件地址
     */
    public static String copyFile(long from, long to, File file){
        byte[] result = copyFileToByte(from,to,file);
        if(result != null){
            String name = file.getPath().substring(file.getPath().lastIndexOf("/") + 1,file.getPath().length());//文件名
            mkDirNotExists(saveFileDir);
            return getFile(result,saveFileDir,name).getPath();
        }
        return null;
    }

    public static boolean mkDirNotExists(String dir) {
        File file = new File(dir);
        return file.exists() || file.mkdirs();
    }

    //根据byte数组，生成文件
    public static File getFile(byte[] bfile, String filePath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath+"\\"+fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return file;
    }
}




