package com.okhttplib.helper;

import android.text.TextUtils;

import com.okhttplib.HttpInfo;
import com.okhttplib.annotation.DownloadStatus;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.bean.UploadFileInfo;
import com.okhttplib.callback.BaseActivityLifecycleCallbacks;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.handler.OkMainHandler;
import com.okhttplib.progress.ProgressRequestBody;
import com.okhttplib.progress.ProgressResponseBody;
import com.okhttplib.util.EncryptUtil;
import com.okhttplib.util.MediaTypeUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 上传/下载业务类
 * @author zhousf
 */
class DownUpLoadHelper extends BaseHelper{

    private long timeStamp;
    private String downloadFileDir;//下载文件保存目录
    private String requestTag;//请求标识

    private static Map<String,String> downloadTaskMap;

    DownUpLoadHelper(HelperInfo helperInfo) {
        super();
        TAG = helperInfo.getLogTAG();
        timeStamp = helperInfo.getTimeStamp();
        showHttpLog = helperInfo.isShowHttpLog();
        timeStamp = helperInfo.getTimeStamp();
        downloadFileDir = helperInfo.getDownloadFileDir();
        requestTag = helperInfo.getRequestTag();
    }

    /**
     * 文件上传
     */
    void uploadFile(OkHttpHelper helper){
        try {
            final HttpInfo info = helper.getHttpInfo();
            final UploadFileInfo fileInfo = helper.getUploadFileInfo();
            String filePath = fileInfo.getFilePathWithName();
            String interfaceParamName = fileInfo.getInterfaceParamName();
            String url = fileInfo.getUrl();
            url = TextUtils.isEmpty(url) ? info.getUrl() : url;
            if(TextUtils.isEmpty(url)){
                showLog("文件上传接口地址不能为空["+filePath+"]");
                return ;
            }
            ProgressCallback progressCallback = fileInfo.getProgressCallback();
            File file = new File(filePath);
            MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            StringBuilder log = new StringBuilder("PostParams: ");
            log.append(interfaceParamName);
            log.append("=");
            log.append(filePath);
            String logInfo;
            if(null != info.getParams() && !info.getParams().isEmpty()){
                for (String key : info.getParams().keySet()) {
                    mBuilder.addFormDataPart(key, info.getParams().get(key));
                    logInfo = key+" ="+info.getParams().get(key)+", ";
                    log.append(logInfo);
                }
            }
            showLog(log.toString());
            mBuilder.addFormDataPart(interfaceParamName,
                    file.getName(),
                    RequestBody.create(MediaTypeUtil.fetchFileMediaType(filePath), file));
            RequestBody requestBody = mBuilder.build();
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url).post(new ProgressRequestBody(requestBody,progressCallback));
            helper.getHttpHelper().addHeadsToRequest(info,requestBuilder);
            Request request = requestBuilder.build();
            helper.setRequest(request);
            helper.doRequestSync();
            helper.getHttpHelper().responseCallback(info,progressCallback, OkMainHandler.RESPONSE_UPLOAD_CALLBACK);
        } catch (Exception e){
            showLog("上传文件失败："+e.getMessage());
        }
    }

    /**
     * 文件下载
     */
    void downloadFile(OkHttpHelper helper){
        try {
            final HttpInfo httpInfo = helper.getHttpInfo();
            final DownloadFileInfo fileInfo = helper.getDownloadFileInfo();
            String url = fileInfo.getUrl();
            if(TextUtils.isEmpty(url)){
                showLog("下载文件失败：文件下载地址不能为空！");
                return ;
            }
            httpInfo.setUrl(url);
            ProgressCallback progressCallback = fileInfo.getProgressCallback();
            //获取文件断点
            long completedSize = fetchCompletedSize(fileInfo);
            fileInfo.setCompletedSize(completedSize);
            //添加下载任务
            if(null == downloadTaskMap)
                downloadTaskMap = new ConcurrentHashMap<>();
            if(downloadTaskMap.containsKey(fileInfo.getSaveFileNameEncrypt())){
                showLog(fileInfo.getSaveFileName()+" 已在下载任务中");
                return ;
            }
            downloadTaskMap.put(fileInfo.getSaveFileNameEncrypt(),fileInfo.getSaveFileNameEncrypt());
            Interceptor interceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), fileInfo))
                            .build();
                }
            };
            //采用新的OkHttpClient处理多线程干扰回调进度问题
            OkHttpClient httpClient = helper.getClientBuilder().addInterceptor(interceptor).build();
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(url)
                    .header("RANGE", "bytes=" + completedSize + "-");
            helper.getHttpHelper().addHeadsToRequest(httpInfo, requestBuilder);
            Request request = requestBuilder.build();
            helper.setRequest(request);
            helper.setHttpClient(httpClient);
            //发送请求
            helper.doRequestSync();
            //删除下载任务
            if(null != downloadTaskMap)
                downloadTaskMap.remove(fileInfo.getSaveFileNameEncrypt());
            helper.getHttpHelper().responseCallback(httpInfo,progressCallback,OkMainHandler.RESPONSE_DOWNLOAD_CALLBACK);
        } catch (Exception e){
            showLog("下载文件失败："+e.getMessage());
        }

    }

    /**
     * 开始文件下载
     */
    HttpInfo downloadingFile(OkHttpHelper okHttpInfo ,Response res, Call call){
        final HttpInfo info = okHttpInfo.getHttpInfo();
        final DownloadFileInfo fileInfo = okHttpInfo.getDownloadFileInfo();
        RandomAccessFile accessFile = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        String filePath = fileInfo.getSaveFileDir()+fileInfo.getSaveFileNameWithExtension();
        try {
            ResponseBody responseBody = res.body();
            int length;
            long completedSize = fileInfo.getCompletedSize();
            accessFile = new RandomAccessFile(fileInfo.getSaveFileDir()+fileInfo.getSaveFileNameEncrypt(),"rwd");
            //服务器不支持断点下载时重新下载
            if(TextUtils.isEmpty(res.header("Content-Range"))){
                completedSize = 0L;
                fileInfo.setCompletedSize(completedSize);
            }
            accessFile.seek(completedSize);
            inputStream = responseBody.byteStream();
            byte[] buffer = new byte[2048];
            bis = new BufferedInputStream(inputStream);
            fileInfo.setDownloadStatus(DownloadStatus.DOWNLOADING);
            while ( (length = bis.read(buffer)) > 0 &&
                    (DownloadStatus.DOWNLOADING.equals(fileInfo.getDownloadStatus()))) {
                accessFile.write(buffer, 0, length);
                completedSize += length;
            }
            if(DownloadStatus.PAUSE.equals(fileInfo.getDownloadStatus())){
                return okHttpInfo.getHttpHelper().retInfo(info,HttpInfo.Message,"暂停下载");
            }
            //下载完成
            if(DownloadStatus.DOWNLOADING.equals(fileInfo.getDownloadStatus())){
                fileInfo.setDownloadStatus(DownloadStatus.COMPLETED);
                File newFile = new File(fileInfo.getSaveFileDir(),fileInfo.getSaveFileNameWithExtension());
                //处理文件已存在逻辑
                if(newFile.exists() && newFile.isFile()){
                    filePath = fileInfo.getSaveFileDir()+fileInfo.getSaveFileNameCopy();
                    newFile = new File(fileInfo.getSaveFileDir(),fileInfo.getSaveFileNameCopy());
                }
                File oldFile = new File(fileInfo.getSaveFileDir(),fileInfo.getSaveFileNameEncrypt());
                if(oldFile.exists() && oldFile.isFile()){
                    boolean rename = oldFile.renameTo(newFile);
                    showLog("重命名["+rename+"]:"+newFile.getAbsolutePath());
                }
                return okHttpInfo.getHttpHelper().retInfo(info,HttpInfo.SUCCESS,filePath);
            }
        }catch(SocketTimeoutException e){
            return okHttpInfo.getHttpHelper().retInfo(info,HttpInfo.WriteAndReadTimeOut);
        }catch (Exception e){
            return okHttpInfo.getHttpHelper().retInfo(info,HttpInfo.ConnectionInterruption);
        }finally {
            try {
                if(null != bis)
                    bis.close();
                if(null != inputStream)
                    inputStream.close();
                if(null != accessFile)
                    accessFile.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            BaseActivityLifecycleCallbacks.cancel(requestTag);
            //删除下载任务
            if(null != downloadTaskMap)
                downloadTaskMap.remove(fileInfo.getSaveFileNameEncrypt());
        }
        return okHttpInfo.getHttpHelper().retInfo(info,HttpInfo.SUCCESS,filePath);
    }

    /**
     * 获取断点文件已完成的节点
     */
    private long fetchCompletedSize(DownloadFileInfo fileInfo){
        String saveFileDir = fileInfo.getSaveFileDir();
        String saveFileName = fileInfo.getSaveFileName();
        String url = fileInfo.getUrl();
        String extension = url.substring(url.lastIndexOf(".") + 1);//扩展名
        String saveFileNameCopy = saveFileName+"["+timeStamp+"]"+"."+extension;
        saveFileName += "."+extension;
        saveFileDir = TextUtils.isEmpty(saveFileDir) ? downloadFileDir : saveFileDir;
        mkDirNotExists(saveFileDir);
        fileInfo.setSaveFileDir(saveFileDir);
        fileInfo.setSaveFileNameCopy(saveFileNameCopy);
        fileInfo.setSaveFileNameWithExtension(saveFileName);
        String saveFileNameEncrypt = url;
        try {
            saveFileNameEncrypt = EncryptUtil.MD5StringTo32Bit(url,true);
            fileInfo.setSaveFileNameEncrypt(saveFileNameEncrypt);
        } catch (Exception e){
            showLog("断点文件下载: 文件名MD5加密失败 "+e.getMessage());
        }
        File file = new File(saveFileDir,saveFileNameEncrypt);
        if(file.exists() && file.isFile()){
            long size = file.length();
            showLog("断点文件下载，节点["+size+"]");
            return size;
        }
        return 0L;
    }

    private boolean mkDirNotExists(String dir) {
        File file = new File(dir);
        return file.exists() || file.mkdirs();
    }



}
