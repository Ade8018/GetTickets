package me.lkt.getticket.page;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.lkt.getticket.entity.Train;
import me.lkt.getticket.utils.Utils;
import me.lkt.utils.bitmap.BitmapUtils;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class TicketListPage extends WebPage {
	public interface OnTicketEventListener {
		public void onTicketFound();
	}

	private Builder builder;
	private List<String> mSelectedTrainNumbers;
	private boolean isRefreshing;
	private OnTicketEventListener listener;
	private List<Train> trains;
	private static final int rate = 3;

	public TicketListPage(WebView wv, OnTicketEventListener listener) {
		super(wv);
		this.listener = listener;
		wv.addJavascriptInterface(this, "ticketlist_interface");
		builder = new Builder(wv.getContext());
		builder.setTitle("Select Train");
		builder.setCancelable(true);
	}

	public void requestToSelectTrains() {
		wv.loadUrl("javascript:window.ticketlist_interface.onSelectTrains(document.getElementById('queryLeftTable').innerHTML);");
	}

	public void startRefresh() {
		isRefreshing = true;
		startNextRefresh();
	}

	public void stopRefresh() {
		isRefreshing = false;
	}

	public void preorderTicket() {
		final Train train = getFirstSelectedTrainHasSeat();
		if (train == null || train.jsOrder == null) {
			Toast.makeText(wv.getContext(), "尝试预订车票时出错，请从头开始",
					Toast.LENGTH_LONG).show();
			return;
		}
		wv.post(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl("javascript:" + train.jsOrder + ";");
			}
		});
	}

	public void loginIfReady() {
		wv.post(new Runnable() {
			@Override
			public void run() {
				setRandcodePicLoadingListener();
			}
		});
	}

	protected void requestRandCodePosition() {
		wv.postDelayed(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl("javascript:"
						+ "var img = document.getElementsByClassName('touclick-image')[1];"
						+ "window.ticketlist_interface.onGetRandcodePosition(img.x,img.y);");
			}
		}, 100);
	}

	protected void setRandcodePicLoadingListener() {
		wv.loadUrl("javascript:"
				+ "var img =  document.getElementsByClassName('touclick-image')[1];"
				+ "img.onload = function(){"
				+ "document.getElementById('username').value = '361700704@qq.com';"
				+ "document.getElementById('password').value = '1qazMKO0';"
				+ "window.ticketlist_interface.onRandcodeImgLoaded();" + "}");
	}

	public void captureRandCodePic(int randcodex, int randcodey) {
		try {
			Picture pic = wv.capturePicture();
			Bitmap bmp = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(),
					Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			pic.draw(canvas);
			Bitmap code = Bitmap.createBitmap(293, 190, Config.ARGB_8888);
			Canvas canCode = new Canvas(code);
			canCode.drawBitmap(bmp, new Rect(randcodex * rate,
					randcodey * rate, randcodex * rate + 293 * rate, randcodey
							* rate + 190 * rate), new Rect(0, 0, 293, 190),
					null);
			BitmapUtils.savePicture(code, Environment
					.getExternalStorageDirectory().getAbsolutePath(),
					"capture.bmp");
		} catch (Exception e) {

		}
	}

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
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSelectedTrainNumbers = selectedTrainNumbers;
				Log.e("lkt",
						"selected train:" + mSelectedTrainNumbers.toString());
			}
		});
		builder.create().show();
	}

	@JavascriptInterface
	public void onCheckInfo(String info) {
		trains = Utils.getTrainInfos(info);
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
	public void onRandcodeImgLoaded() {
		Log.e("lkt", "onRandcodeImgLoaded ");
		requestRandCodePosition();
	}

	@JavascriptInterface
	public void onGetRandcodePosition(final int x, final int y) {
		Log.e("lkt", "onGetRandcodePosition " + x + " " + y);
		if (x == 0 && y == 0) {
			requestRandCodePosition();
			return;
		}
		wv.postDelayed(new Runnable() {
			@Override
			public void run() {
				captureRandCodePic(x, y);
				requestRandcodeValue();
			}
		}, 500);
	}

	protected void requestRandcodeValue() {
		try {
			URL url = new URL("http://api.yundama.com/api.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("method", "upload");
			conn.addRequestProperty("username", "tomatomaster");
			conn.addRequestProperty("password", "1qazMKO0");
			conn.addRequestProperty("codetype", "6701");
			conn.addRequestProperty("appid", "");
			conn.addRequestProperty("appkey", "");
			conn.addRequestProperty("timeout", "20");
			conn.addRequestProperty("file", null);
			conn.connect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillRandcode(String positionStr) {
		wv.loadUrl("javascript:"
				+ "document.getElementsByName('randCode')[1].value = '"
				+ positionStr + "';");
	}

	private void startNextRefresh() {
		if (!isRefreshing) {
			return;
		}
		wv.postDelayed(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl("javascript:document.getElementById('query_ticket').click();");
			}
		}, 888);
		wv.postDelayed(new Runnable() {
			@Override
			public void run() {
				wv.loadUrl("javascript:window.ticketlist_interface.onCheckInfo(document.getElementById('queryLeftTable').innerHTML);");
			}
		}, 1666);
	}

	private void onTicketFound() {
		stopRefresh();
		listener.onTicketFound();
	}

	private Train getFirstSelectedTrainHasSeat() {
		Train train = null;
		for (int i = 0; i < trains.size(); i++) {
			train = trains.get(i);
			if (mSelectedTrainNumbers.contains(train.name) && train.hasSeat) {
				return train;
			}
		}
		return null;
	}
}
