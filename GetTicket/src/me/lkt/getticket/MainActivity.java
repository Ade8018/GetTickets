package me.lkt.getticket;

import java.util.ArrayList;
import java.util.List;

import me.lkt.utils.bitmap.BitmapUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class MainActivity extends Activity implements OnClickListener {
	private WebView wv;
	private TextView tvSelectTrain;
	private TextView tvStartRefresh;
	private TextView tvStopRefresh;
	private TextView tvGetRandcodePosition;
	private TextView tvCaptureRandcode;
	private Builder builder;
	private DrawerLayout dl;
	private List<String> mSelectedTrainNumbers;
	private boolean isRefreshing;
	private PowerManager pm;
	private WakeLock wl;
	private Vibrator vibrator;
	private int randcodex;
	private int randcodey;

	private WebViewClient mWbClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed();
		}

		@Override
		public void onPageFinished(WebView paramWebView, String paramString) {
			Log.e("lkt", "onScaleChanged");
		}
	};

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pm = (PowerManager) getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wake lock");
		wl.acquire();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		builder = new Builder(this);
		builder.setTitle("Select Train");
		builder.setCancelable(true);

		dl = (DrawerLayout) findViewById(R.id.dl);
		tvSelectTrain = (TextView) findViewById(R.id.tv_select_train);
		tvStartRefresh = (TextView) findViewById(R.id.tv_start_refresh);
		tvStopRefresh = (TextView) findViewById(R.id.tv_stop_refresh);
		tvGetRandcodePosition = (TextView) findViewById(R.id.tv_get_randcode_position);
		tvCaptureRandcode = (TextView) findViewById(R.id.tv_capture_randcode);
		tvCaptureRandcode.setOnClickListener(this);
		tvGetRandcodePosition.setOnClickListener(this);
		tvStartRefresh.setOnClickListener(this);
		tvSelectTrain.setOnClickListener(this);
		tvStopRefresh.setOnClickListener(this);

		wv = (WebView) findViewById(R.id.wv);
		wv.setWebViewClient(mWbClient);
		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		// wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setBlockNetworkImage(false);
		wv.getSettings().setAppCacheEnabled(true);
		wv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		wv.addJavascriptInterface(new GetInfo(), "getinfo");
		wv.loadUrl("https://kyfw.12306.cn/otn/leftTicket/init");
	}

	@Override
	public void onClick(View v) {
		if (v == tvSelectTrain) {
			wv.loadUrl("javascript:window.getinfo.onSelectTrains(document.getElementById('queryLeftTable').innerHTML);");
		} else if (v == tvStartRefresh) {
			isRefreshing = true;
			startNextRefresh();
		} else if (v == tvStopRefresh) {
			isRefreshing = false;
			vibrator.cancel();
		} else if (v == tvGetRandcodePosition) {
			onGetRandcodePosition();
		} else if (v == tvCaptureRandcode) {
			onCapture();
		}
		if (dl.isShown()) {
			dl.closeDrawers();
		}
	}

	private void onCapture() {
		Picture pic = wv.capturePicture();
		Bitmap bmp = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		pic.draw(canvas);
		Bitmap code = Bitmap.createBitmap(293, 190, Config.ARGB_8888);
		Canvas canCode = new Canvas(code);
		canCode.drawBitmap(bmp, new Rect(randcodex * 2, randcodey * 2,
				randcodex * 2 + 293 * 2, randcodey * 2 + 190 * 2), new Rect(0,
				0, 293, 190), null);
		BitmapUtils.savePicture(code, Environment.getExternalStorageDirectory()
				.getAbsolutePath(), "capture.bmp");
	}

	private void onGetRandcodePosition() {
		wv.loadUrl("javascript:"
				+ "var img = document.getElementsByClassName('touclick-image')[1];"
				+ "window.getinfo.getRandcodePosition(img.x,img.y);");
	}

	public class GetInfo {
		@JavascriptInterface
		public void onSelectTrains(String info) {
			final List<Train> trains = Utils.getTrainInfos(info);
			int count = trains.size();
			String[] items = new String[count];
			boolean checkedItems[] = new boolean[count];
			for (int i = 0; i < items.length; i++) {
				items[i] = trains.get(i).toString();
				checkedItems[i] = false;
			}
			final List<String> selectedTrainNumbers = new ArrayList<String>();
			builder.setMultiChoiceItems(items, checkedItems,
					new OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							if (isChecked) {
								selectedTrainNumbers.add(trains.get(which).name);
							} else {
								selectedTrainNumbers.remove(trains.get(which).name);
							}
						}
					});
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSelectedTrainNumbers = selectedTrainNumbers;
							Log.e("lkt", "selected train:"
									+ mSelectedTrainNumbers.toString());
						}
					});
			builder.create().show();
		}

		@JavascriptInterface
		public void onCheckInfo(String info) {
			final List<Train> trains = Utils.getTrainInfos(info);
			boolean foundTicket = false;
			for (int i = 0; i < trains.size(); i++) {
				Train t = trains.get(i);
				if (mSelectedTrainNumbers.contains(t.name) && t.hasSeat) {
					foundTicket = true;
					break;
				}
			}
			if (foundTicket) {
				onTicketFound();
			} else {
				startNextRefresh();
			}
		}

		@JavascriptInterface
		public void getRandcodePosition(int x, int y) {
			randcodex = x;
			randcodey = y;
			Toast.makeText(MainActivity.this, "x:" + x + ",y:" + y,
					Toast.LENGTH_LONG).show();
		}
	}

	public void startNextRefresh() {
		if (!isRefreshing) {
			return;
		}
		tvSelectTrain.postDelayed(new Runnable() {
			@Override
			public void run() {
				clickQuery();
			}
		}, 888);
		tvSelectTrain.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkInfo();
			}
		}, 1666);
	}

	public void clickQuery() {
		wv.loadUrl("javascript:document.getElementById('query_ticket').click();");
	}

	public void checkInfo() {
		wv.loadUrl("javascript:window.getinfo.onCheckInfo(document.getElementById('queryLeftTable').innerHTML);");
	}

	public void onTicketFound() {
		Log.e("lkt", "stopRefresh");
		Toast.makeText(this, "ticket found !", Toast.LENGTH_LONG).show();
		vibrator.vibrate(new long[] { 50, 100, 50, 100 }, 0);
	}

	@Override
	protected void onDestroy() {
		wl.release();
		super.onDestroy();
	}
}
