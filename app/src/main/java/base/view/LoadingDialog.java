package base.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.okhttp3.R;


/**
 * Author : zhousf
 * Description : 加载框
 * Date : 2017/9/20.
 */
public class LoadingDialog extends Dialog {

    private ImageView iv_load_result;// 加载的结果图标显示
    private TextView tv_load;// 加载的文字展示
    private ProgressBar pb_loading;// 加载中的图片

    public LoadingDialog(Context context) {
        super(context, R.style.loading_dialog_style);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commom_loading_layout);
        iv_load_result = (ImageView) findViewById(R.id.iv_load_result);
        tv_load = (TextView) findViewById(R.id.tv_load);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        iv_load_result.setVisibility(View.GONE);

    }

    public LoadingDialog text(String text){
        if(tv_load != null && !TextUtils.isEmpty(text)){
            tv_load.setText(text);
        }
        return this;
    }

    //显示对话框
    public void showDialog(){
        text("加载中");
        if(null != pb_loading && pb_loading.getVisibility() != View.VISIBLE){
            pb_loading.setVisibility(View.VISIBLE);
        }
        if(null != iv_load_result){
            iv_load_result.setVisibility(View.GONE);
        }
        show();
    }

    // 加载成功
    public void succeed() {
        if(pb_loading != null)
            pb_loading.setVisibility(View.GONE);
        if(iv_load_result != null){
            iv_load_result.setVisibility(View.VISIBLE);
            iv_load_result.setImageResource(R.mipmap.load_suc_icon);
        }
        tv_load.setText("加载成功");
    }

    // 加载失败
    public void failed() {
        if(pb_loading != null)
            pb_loading.setVisibility(View.GONE);
        if(iv_load_result != null){
            iv_load_result.setVisibility(View.VISIBLE);
            iv_load_result.setImageResource(R.mipmap.load_fail_icon);
        }
        tv_load.setText("加载失败");
    }



}
