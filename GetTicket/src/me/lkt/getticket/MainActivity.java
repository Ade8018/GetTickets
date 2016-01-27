package me.lkt.getticket;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class MainActivity extends Activity implements OnClickListener {
	private WebView wv;
	private Button btn;
	private boolean startAutoRefresh;
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
			if (startAutoRefresh) {
				getTicketsInfo(paramWebView);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wv = (WebView) findViewById(R.id.wv);
		wv.setWebViewClient(mWbClient);
		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl("http://www.12306.cn/mormhweb/");
		btn = (Button) findViewById(R.id.btn);
		btn.setOnClickListener(this);
	}

	protected void getTicketsInfo(WebView paramWebView) {
	}

	@Override
	public void onClick(View v) {
		wv.loadUrl("javascript:document.getElementById('query_ticket').click();");
	}
}
