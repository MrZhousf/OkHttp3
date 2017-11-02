package base.http;

import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Author : zhousf
 * Description : 网络请求信息体，可根据项目需求进行扩展
 * Date : 2017/10/31.
 */
public class HttpEntity {

    private String url;//请求地址
    private Map<String,String> params;//请求参数：键值对
    private byte[] paramBytes;//请求参数（字节数组）
    private File paramFile;//请求参数（文件）
    private String paramJson;//请求参数:application/json
    private String paramForm;//请求参数
    private String httpsCertificate;//Https证书
    private InputStream httpsCertificateStream;//Https证书

    //**响应返回参数定义**/
    private int retCode;//返回码
    private String retDetail;//返回结果
    private int netCode;//网络返回码


    public static HttpEntity create(){
        return new HttpEntity();
    }

    private HttpEntity() {
    }

    public HttpEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 添加接口参数
     * @param params 参数集合
     */
    public HttpEntity addParams(Map<String, String> params) {
        if(null == params)
            return this;
        if(null == this.params){
            this.params = params;
        }else{
            this.params.putAll(params);
        }
        return this;
    }

    /**
     * 添加接口参数：键值对
     * @param key 参数名
     * @param value 参数值
     */
    public HttpEntity addParam(String key, String value){
        if(null == this.params)
            this.params = new HashMap<>();
        if(!TextUtils.isEmpty(key)){
            value = value == null ? "" : value;
            this.params.put(key,value);
        }
        return this;
    }


    /**
     * 添加接口参数：表单提交
     * 请采用POST请求方式
     * MediaType.parse("application/x-www-form-urlencoded")
     * @param paramForm 参数值
     */
    public HttpEntity addParamForm(String paramForm){
        if(TextUtils.isEmpty(paramForm)){
            throw new IllegalArgumentException("param must not be null");
        }
        this.paramForm = paramForm;
        return this;
    }

    /**
     * 添加接口参数（字节数组/二进制流）
     * 请采用POST请求方式
     * MediaType.parse("application/octet-stream")
     * @param paramBytes 参数值
     */
    public HttpEntity addParamBytes(byte[] paramBytes){
        this.paramBytes = paramBytes;
        return this;
    }

    /**
     * 添加接口参数（字节数组/二进制流）
     * 请采用POST请求方式
     * MediaType.parse("application/octet-stream")
     * @param paramBytes 参数值
     */
    public HttpEntity addParamBytes(String paramBytes){
        if(TextUtils.isEmpty(paramBytes)){
            throw new IllegalArgumentException("paramBytes must not be null");
        }
        this.paramBytes = paramBytes.getBytes();
        return this;
    }

    /**
     * 添加接口参数（文件）
     * 请采用POST请求方式
     * 该方法可上传文件，建议上传文件采用标准方法：addUploadFile
     * MediaType.parse("text/x-markdown; charset=utf-8")
     * @param file 上传文件
     */
    public HttpEntity addParamFile(File file){
        if(file == null || !file.exists()){
            throw new IllegalArgumentException("file must not be null");
        }
        this.paramFile = file;
        return this;
    }

    /**
     * 添加接口参数（json）
     * 请采用POST请求方式
     * MediaType.parse("application/json; charset=utf-8")
     * @param json json格式参数值
     */
    public HttpEntity addParamJson(String json){
        if(TextUtils.isEmpty(json)){
            throw new IllegalArgumentException("json param must not be null");
        }
        this.paramJson = json;
        return this;
    }

    //设置Https证书：证书必须放在assets文件夹下
    public HttpEntity setHttpsCertificate(String httpsCertificate){
        this.httpsCertificate = httpsCertificate;
        return this;
    }

    //设置Https证书
    public HttpEntity setHttpsCertificate(InputStream httpsCertificate){
        this.httpsCertificateStream = httpsCertificate;
        return this;
    }


    @Override
    public String toString() {
        return "HttpEntity{" +
                "url='" + url + '\'' +
                ", params=" + params +
                ", paramBytes=" + Arrays.toString(paramBytes) +
                ", paramFile=" + paramFile +
                ", paramJson='" + paramJson + '\'' +
                ", paramForm='" + paramForm + '\'' +
                ", httpsCertificate='" + httpsCertificate + '\'' +
                ", httpsCertificateStream=" + httpsCertificateStream +
                ", retCode=" + retCode +
                ", retDetail='" + retDetail + '\'' +
                ", netCode=" + netCode +
                '}';
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public byte[] getParamBytes() {
        return paramBytes;
    }

    public File getParamFile() {
        return paramFile;
    }

    public String getParamJson() {
        return paramJson;
    }

    public String getParamForm() {
        return paramForm;
    }

    public String getHttpsCertificate() {
        return httpsCertificate;
    }

    public InputStream getHttpsCertificateStream() {
        return httpsCertificateStream;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getRetDetail() {
        return retDetail;
    }

    public void setRetDetail(String retDetail) {
        this.retDetail = retDetail;
    }

    public int getNetCode() {
        return netCode;
    }

    public void setNetCode(int netCode) {
        this.netCode = netCode;
    }

    public boolean isSuccessful(){
        return this.netCode == 200;
    }
}
