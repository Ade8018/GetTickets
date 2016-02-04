package me.lkt.getticket;

import java.util.ArrayList;
import java.util.List;

import me.lkt.getticket.page.TicketListPage;
import me.lkt.getticket.page.TicketListPage.OnTicketEventListener;
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

public class MainActivity extends Activity implements OnClickListener,
		OnTicketEventListener {
	private WebView wv;
	private TextView tvSelectTrain;
	private TextView tvStartRefresh;
	private TextView tvStopRefresh;
	private TextView tvGetRandcodePosition;
	private TextView tvCaptureRandcode;
	private DrawerLayout dl;
	private PowerManager pm;
	private WakeLock wl;
	private Vibrator vibrator;
	private TicketListPage ticketListPage;

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pm = (PowerManager) getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wake lock");
		wl.acquire();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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
		wv.setWebViewClient(new SslWebViewClient());
		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl("https://kyfw.12306.cn/otn/leftTicket/init");

		ticketListPage = new TicketListPage(wv, this);
	}

	@Override
	public void onClick(View v) {
		if (v == tvSelectTrain) {
			ticketListPage.requestToSelectTrains();
		} else if (v == tvStartRefresh) {
			ticketListPage.startRefresh();
		} else if (v == tvStopRefresh) {
			ticketListPage.stopRefresh();
		}
		if (dl.isShown()) {
			dl.closeDrawers();
		}
	}

	@Override
	public void onTicketFound() {
		Toast.makeText(this, "ticket found !", Toast.LENGTH_LONG).show();
		vibrator.vibrate(new long[] { 50, 100, 50, 100 }, 0);
	}

	@Override
	protected void onPause() {
		wl.release();
		super.onPause();
	}
}
