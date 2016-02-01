package me.lkt.getticket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	private WebView wv;
	private Button btn;
	private boolean startAutoRefresh;
	private TextView tvSelectTrain;
	private TextView tvStartRefresh;
	private Builder builder;
	private DrawerLayout dl;
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
			Log.e("lkt", "onPageFinished");
			getTicketsInfo(paramWebView);
		}
	};

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		builder = new Builder(this);
		builder.setTitle("Select Train");
		builder.setCancelable(true);

		dl = (DrawerLayout) findViewById(R.id.dl);
		tvSelectTrain = (TextView) findViewById(R.id.tv_select_train);
		tvStartRefresh = (TextView) findViewById(R.id.tv_start_refresh);
		tvStartRefresh.setOnClickListener(this);

		wv = (WebView) findViewById(R.id.wv);
		wv.setWebViewClient(mWbClient);
		wv.setWebChromeClient(new WebChromeClient() {
		});
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new GetInfo(), "getinfo");
		wv.loadUrl("https://kyfw.12306.cn/otn/leftTicket/init");

		// btn = (Button) findViewById(R.id.btn);
		// btn.setOnClickListener(this);
	}

	protected void getTicketsInfo(WebView paramWebView) {
		wv.loadUrl("javascript:window.getinfo.onGetInfo(document.getElementById('queryLeftTable'));");
	}

	@Override
	public void onClick(View v) {
		// wv.loadUrl("javascript:document.getElementById('query_ticket').click();");
		wv.loadUrl("javascript:window.getinfo.onGetInfo(document.getElementById('queryLeftTable').innerHTML);");
		if (dl.isShown()) {
			dl.closeDrawers();
		}
	}

	public class GetInfo {
		@JavascriptInterface
		public void onGetInfo(String info) {
			Log.e("lkt", info);
		}
		
		
	}
}
