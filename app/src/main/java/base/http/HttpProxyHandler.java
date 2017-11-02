package base.http;

import com.okhttp3.util.LogUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Author : zhousf
 * Description : 采用动态代理实现面向切面AOP
 * Date : 2017/10/31.
 */
class HttpProxyHandler implements InvocationHandler {

    private Object obj;

    HttpProxyHandler() {
    }

    HttpInterface getProxy(Object targetObject) {
        this.obj = targetObject;
        Object proxy = Proxy.newProxyInstance(targetObject.getClass().getClassLoader(),
                targetObject.getClass().getInterfaces(), this);
        return (HttpInterface)proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //执行前操作
        doBefore(args);
        Object result = method.invoke(obj, args);
        //异步请求时result为空
        if(result != null){
            //执行后操作
            doAfter((HttpEntity) result);
        }
        return result;
    }

    //执行前操作
    private void doBefore(Object[] args){
        for (Object obj : args){
            if(obj instanceof HttpEntity){
                HttpEntity entity = (HttpEntity) obj;
                //打印执行前信息
                LogUtil.d("HttpProxy",entity.toString());
                return ;
            }
        }
    }

    //执行后操作
    private void doAfter(HttpEntity entity){
        //打印执行后信息
        LogUtil.d("HttpProxy",entity.toString());
    }


}
