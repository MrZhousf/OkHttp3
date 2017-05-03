package com.okhttp3.util;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


/**
 * Toast辅助类，避免重复显示
 * @author zhousf
 */
public class ToastUtil {

    private static Toast mToast;

    private static Handler mHandler = new Handler();

    private static Runnable runnable = new Runnable() {
        public void run() {
            if(mToast != null) {
                mToast.cancel();
                mToast = null;
            }
        }
    };

    @IntDef({Toast.LENGTH_SHORT, Toast.LENGTH_LONG})
    public @interface Duration {}


    /**
     * Toast显示
     * @param context 上下文
     * @param text 显示内容
     */
    public static void show(Context context, String text) {
        show(context, text, null);
    }

    /**
     * Toast显示
     * @param context 上下文
     * @param resId 显示内容资源
     */
    public static void show(Context context, int resId) {
        show(context, context.getResources().getString(resId), null);
    }

    /**
     * Toast显示:可以控制显示时间
     * @param context 上下文
     * @param resId 显示内容资源
     * @param duration 显示时间
     */
    public static void show(Context context, int resId, int duration) {
        show(context, context.getResources().getString(resId), duration);
    }


    /**
     * Toast显示:可以控制显示时间
     * @param context 上下文
     * @param text 显示内容
     * @param duration 显示时间
     */
    public static void show(Context context,String text,@Duration Integer duration){
        final int myDuration = (duration==null)?Toast.LENGTH_SHORT:duration;
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, text, myDuration);
        mHandler.removeCallbacks(runnable);
        int delayMillis = (myDuration==Toast.LENGTH_SHORT)?2000:3000;
        mHandler.postDelayed(runnable, delayMillis);
        mToast.show();
    }

    /**
     * Toast显示:可以显示图片等控件
     * @param context 上下文
     * @param text 显示内容
     * @param view 图片View
     */
    public static void showWithView(Context context,String text,View view){
        showWithView(context,text,null,view);
    }

    /**
     * Toast显示:可以显示图片等控件、控制显示时间
     * @param context 上下文
     * @param text 显示内容
     * @param duration 显示时间
     * @param view 图片View
     */
    public static void showWithView(Context context,String text,@Duration Integer duration,View view){
        final int myDuration = (duration==null)?Toast.LENGTH_SHORT:duration;
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, text, myDuration);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastView = (LinearLayout) mToast.getView();
        toastView.addView(view);
        mHandler.removeCallbacks(runnable);
        int delayMillis = (myDuration==Toast.LENGTH_SHORT)?2000:3000;
        mHandler.postDelayed(runnable, delayMillis);
        mToast.show();
    }


}
