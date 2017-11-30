package base.networkstate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.okhttp3.util.SelectorFactory;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 网络实时速度服务
 */
public class NetSpeedService extends Service {

	private LinearLayout linearLayout;
	private WindowManager windowManager;
	private TextView textView;
	private TrafficUtil trafficUtil;
	private Timer mTimer = null;
	private TelephonyManager telephonyManager;
	private int gsmSignalStrength;

	@Override
	public void onCreate() {
		super.onCreate();
		initView();
		initLoop();
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder();
	}

	@Override
	public void onTrimMemory(int level) {

	}

	private void initLoop(){
		trafficUtil = new TrafficUtil();
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = 1;
				msg.obj = trafficUtil.getNetSpeed();
				mHandler.sendMessage(msg);
			}
		}, 0, 1000L);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1 && msg.obj != null) {
				if(!isNetworkAvailable(getApplicationContext())){
					textView.setText("无网络");
					return ;
				}
				String speed = msg.obj.toString();
				if(TextUtils.isEmpty(speed)){
					speed = "0";
				}
				double sp = Double.parseDouble(speed);
				if(sp > 800){
					sp /= 1024;
					speed = String.valueOf(round(sp,2))+"M/s";
				}else{
					speed = sp+"K/s";
				}
				textView.setText(speed);
			}
			super.handleMessage(msg);
		}

	};

	private PhoneStateListener phoneStateListener = new PhoneStateListener(){
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			gsmSignalStrength = signalStrength.getGsmSignalStrength();
			int cdmaDbm = signalStrength.getCdmaDbm();
			int cdmaEcio = signalStrength.getCdmaEcio();
			int evdoDbm = signalStrength.getEvdoDbm();
			int evdoEcio = signalStrength.getEvdoEcio();
		}
	};

	private void initView() {
		final LayoutParams layoutParams = new LayoutParams();
		windowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
		layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
		layoutParams.format = PixelFormat.RGBA_8888;
		layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		layoutParams.gravity = Gravity.START | Gravity.TOP;
		layoutParams.width = dpToPx(getApplicationContext(), 75);
		layoutParams.height = LayoutParams.WRAP_CONTENT;
		layoutParams.x = (getScreenWidth(getApplicationContext()) - layoutParams.width) / 10;
		layoutParams.y = 10;
		//初始化浮动窗口布局
		linearLayout = new LinearLayout(getApplicationContext());
		linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER);
		linearLayout.setPadding(0,5,0,5);
		SelectorFactory.newShapeSelector()
				.setStrokeWidth(1)
				.setCornerRadius(10)
				.setDefaultStrokeColor(Color.GRAY)
				.setDefaultBgColor(Color.WHITE)
				.bind(linearLayout);
		linearLayout.getBackground().setAlpha(150);
		textView = new TextView(getApplicationContext());
		textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		final String text = "0K/s";
		textView.setText(text);
		textView.setTextSize(12);
		textView.setTextColor(Color.BLACK);
		linearLayout.addView(textView);
		windowManager.addView(linearLayout, layoutParams);
		linearLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		textView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				layoutParams.x = (int) event.getRawX() - textView.getMeasuredWidth() / 2;
				layoutParams.y = (int) event.getRawY() - textView.getMeasuredHeight() / 2 - 25;
				windowManager.updateViewLayout(linearLayout, layoutParams);
				return false;
			}
		});
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
	}


	@Override
	public void onDestroy() {
		if (linearLayout != null && windowManager != null) {
			windowManager.removeView(linearLayout);
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		mHandler.removeCallbacksAndMessages(null);
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		super.onDestroy();
	}

	class ServiceBinder extends Binder {
		public NetSpeedService getService() {
			return NetSpeedService.this;
		}
	}

	public int dpToPx(Context context, float dp) {
		final float scale = context.getResources()
				.getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	public int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		if(wm != null){
			wm.getDefaultDisplay().getMetrics(outMetrics);
		}
		return outMetrics.widthPixels;
	}

	public double round(Double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b = null == v ? new BigDecimal("0.0") : new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public boolean isNetworkAvailable(Context context){
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager)
					context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(cm == null)
				return false;
			final NetworkInfo network = cm.getActiveNetworkInfo();
			if(network != null && network.getState() == NetworkInfo.State.CONNECTED){
				return true;
			}
		}
		return false;
	}


}