package me.lkt.getticket.page;

import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 封装页面
 * 
 * @author lkt
 * 
 */
public class WebPage {
	protected WebView wv;

	public WebPage(WebView wv) {
		this.wv = wv;
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

		});
		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		// wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setBlockNetworkImage(false);
		wv.getSettings().setAppCacheEnabled(true);
		wv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		// wv.addJavascriptInterface(new GetInfo(), "getinfo");
		wv.loadUrl("https://kyfw.12306.cn/otn/leftTicket/init");
	}

	// public WebView getWebView() {
	// return wv;
	// }
}
