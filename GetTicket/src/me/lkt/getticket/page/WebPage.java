package me.lkt.getticket.page;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

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
