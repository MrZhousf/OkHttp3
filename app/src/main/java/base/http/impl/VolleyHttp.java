package base.http.impl;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import base.BaseApplication;
import base.http.HttpCallback;
import base.http.HttpEntity;
import base.http.HttpInterface;

/**
 * Author : zhousf
 * Description : Volley网络请求底层库
 * 这里只是简单示例，Volley需要进行二次封装
 * Date : 2017/11/2.
 */
public class VolleyHttp implements HttpInterface {

    private RequestQueue requestQueue = Volley.newRequestQueue(BaseApplication.getApplication());

    @Override
    public HttpEntity doGetSync(HttpEntity httpEntity) {
        //同步需要在子线程执行，否则线程会出现阻塞
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,httpEntity.getUrl(),future,future);
        requestQueue.add(stringRequest);
        try {
            String request = future.get(10000, TimeUnit.SECONDS);
            httpEntity.setNetCode(200);
            httpEntity.setRetDetail(request);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return httpEntity;
    }

    @Override
    public void doGetAsync(final HttpEntity httpEntity, final HttpCallback callback) {
        StringRequest request = new StringRequest(Request.Method.GET,httpEntity.getUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                httpEntity.setNetCode(200);
                httpEntity.setRetDetail(res);
                try {
                    callback.onSuccess(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                httpEntity.setNetCode(volleyError.networkResponse.statusCode);
                httpEntity.setRetDetail(volleyError.getMessage());
                try {
                    callback.onFailure(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return httpEntity.getParams();
            }
        };
        requestQueue.add(request);
    }

    @Override
    public HttpEntity doPostSync(HttpEntity httpEntity) {
        //同步需要在子线程执行，否则线程会出现阻塞
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,httpEntity.getUrl(),future,future);
        requestQueue.add(stringRequest);
        try {
            String request = future.get(1000, TimeUnit.SECONDS);
            httpEntity.setNetCode(200);
            httpEntity.setRetDetail(request);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return httpEntity;
    }

    @Override
    public void doPostAsync(final HttpEntity httpEntity, final HttpCallback callback) {
        StringRequest request = new StringRequest(Request.Method.POST,httpEntity.getUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                httpEntity.setNetCode(200);
                httpEntity.setRetDetail(res);
                try {
                    callback.onSuccess(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                httpEntity.setNetCode(volleyError.networkResponse.statusCode);
                httpEntity.setRetDetail(volleyError.getMessage());
                try {
                    callback.onFailure(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return httpEntity.getParams();
            }
        };
        requestQueue.add(request);
    }


}
