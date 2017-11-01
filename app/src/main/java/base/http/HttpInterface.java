package base.http;

/**
 * Author : zhousf
 * Description : 网络请求接口
 * Date : 2017/10/31.
 */
public interface HttpInterface {

    HttpEntity doGetSync(HttpEntity httpEntity);

    void doGetAsync(HttpEntity httpEntity,HttpCallback callback);

    HttpEntity doPostSync(HttpEntity httpEntity);

    void doPostAsync(HttpEntity httpEntity,HttpCallback callback);


}
