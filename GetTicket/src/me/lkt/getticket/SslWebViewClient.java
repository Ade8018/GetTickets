package me.lkt.getticket;

import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SslWebViewClient extends WebViewClient {
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
}
