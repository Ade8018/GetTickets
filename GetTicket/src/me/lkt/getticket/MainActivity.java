package me.lkt.getticket;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
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
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private WebView wv;
	private Button btn;
	private boolean startRefreshing;
	private TextView tvSelectTrain;
	private TextView tvStartRefresh;
	private Builder builder;
	private DrawerLayout dl;
	private List<String> mSelectedTrainNumbers;
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
		tvSelectTrain.setOnClickListener(this);

		wv = (WebView) findViewById(R.id.wv);
		wv.setWebViewClient(mWbClient);
		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new GetInfo(), "getinfo");
		wv.loadUrl("https://kyfw.12306.cn/otn/leftTicket/init");
	}

	@Override
	public void onClick(View v) {
		if (v == tvSelectTrain) {
			wv.loadUrl("javascript:window.getinfo.onSelectTrains(document.getElementById('queryLeftTable').innerHTML);");
		} else if (v == tvStartRefresh) {
			startNextRefresh();
		}
		if (dl.isShown()) {
			dl.closeDrawers();
		}
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
			if (trains != null && trains.size() > 0) {
				startRefreshing = false;
			}
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
	}

	public void startNextRefresh() {
		tvSelectTrain.postDelayed(new Runnable() {
			@Override
			public void run() {
				clickQuery();
			}
		}, 500);
		tvSelectTrain.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkInfo();
			}
		}, 1000);
	}

	// private Handler mHandler = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	// if (msg.what == 0) {
	//
	// } else if (msg.what == 1) {
	// checkInfo();
	// }
	// };
	// };

	public void clickQuery() {
		wv.loadUrl("javascript:document.getElementById('query_ticket').click();");
	}

	public void checkInfo() {
		wv.loadUrl("javascript:window.getinfo.onCheckInfo(document.getElementById('queryLeftTable').innerHTML);");
	}

	public void onTicketFound() {
		Log.e("lkt", "stopRefresh");
		startRefreshing = false;
		Toast.makeText(this, "ticket found !", Toast.LENGTH_LONG).show();
	}

}
