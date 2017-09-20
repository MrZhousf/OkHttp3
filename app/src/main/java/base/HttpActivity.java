package base;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.Callback;

import java.io.IOException;

import base.view.LoadingDialog;

/**
 * Activity基类：支持网络请求、加载提示框
 * @author zhousf
 */
public class HttpActivity extends AppCompatActivity implements BaseHandler.CallBack {

    private BaseHandler mainHandler;

    private LoadingDialog loadingDialog;//加载提示框

    private final int SHOW_DIALOG = 0x001;
    private final int DISMISS_DIALOG = 0x002;
    private final int LOAD_SUCCEED = 0x003;
    private final int LOAD_FAILED = 0x004;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 同步请求
     * @param info 请求信息体
     * @return HttpInfo
     */
    protected HttpInfo doHttpSync(HttpInfo info) {
        //显示加载框
        getMainHandler().sendEmptyMessage(SHOW_DIALOG);
        info = OkHttpUtil.getDefault(this).doSync(info);
        if(info.isSuccessful()){
            //显示加载成功
            getMainHandler().sendEmptyMessage(LOAD_SUCCEED);
        }else {
            //显示加载失败
            getMainHandler().sendEmptyMessage(LOAD_FAILED);
        }
        //隐藏加载框
        getMainHandler().sendEmptyMessageDelayed(DISMISS_DIALOG,1000);
        return info;
    }

    /**
     * 异步请求
     * @param info 请求信息体
     * @param callback 结果回调接口
     */
    protected void doHttpAsync(HttpInfo info, final Callback callback){
        getMainHandler().sendEmptyMessage(SHOW_DIALOG);
        OkHttpUtil.getDefault(this).doAsync(info, new Callback() {
            @Override
            public void onSuccess(HttpInfo info) throws IOException {
                getLoadingDialog().dismiss();
                callback.onSuccess(info);
            }

            @Override
            public void onFailure(HttpInfo info) throws IOException {
                getLoadingDialog().dismiss();
                callback.onFailure(info);
            }
        });
    }

    protected LoadingDialog getLoadingDialog(){
        if(null == loadingDialog){
            loadingDialog = new LoadingDialog(this);
            //点击空白处Dialog不消失
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        return loadingDialog;
    }

    /**
     * 获取通用句柄，自动释放
     */
    protected BaseHandler getMainHandler(){
        if(null == mainHandler){
            mainHandler = new BaseHandler(this, Looper.getMainLooper());
        }
        return mainHandler;
    }


    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SHOW_DIALOG:
                getLoadingDialog().showDialog();
                break;
            case LOAD_SUCCEED:
                getLoadingDialog().succeed();
                break;
            case LOAD_FAILED:
                getLoadingDialog().failed();
                break;
            case DISMISS_DIALOG:
                getLoadingDialog().dismiss();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        if(null != mainHandler)
            mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
