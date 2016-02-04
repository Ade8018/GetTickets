package me.lkt.getticket.page;

import java.util.ArrayList;
import java.util.List;

import me.lkt.getticket.Utils;
import me.lkt.getticket.entity.Train;
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
import android.widget.Toast;

import com.tencent.smtt.sdk.WebView;

public class TicketListPage extends WebPage {
	public interface OnTicketEventListener {
		public void onTicketFound();
	}

	private Builder builder;
	private List<String> mSelectedTrainNumbers;
	private boolean isRefreshing;
	private OnTicketEventListener listener;

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
		// click preorder
	}

	public void loginIfReady() {
		requestRandCodePosition();
	}

	protected void requestRandCodePosition() {
		wv.loadUrl("javascript:"
				+ "var img = document.getElementsByClassName('touclick-image')[1];"
				+ "window.ticketlist_interface.onGetRandcodePosition(img.x,img.y);");
	}

	private void fillPersonalInfo(String username, String password) {
		wv.loadUrl("javascript:"
				+ "document.getElementById('username').value = '" + username
				+ "';" + "document.getElementById('password').value = '"
				+ password + "';");
	}

	private void fillRandcode(String positionStr) {
		wv.loadUrl("javascript:"
				+ "document.getElementsByName('randCode')[1].value = '"
				+ positionStr + "';");
	}

	public void captureRandCodePic(int randcodex, int randcodey) {
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

	@JavascriptInterface
	private void onSelectTrains(String info) {
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
	private void onCheckInfo(String info) {
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
	private void onGetRandcodePosition(int x, int y) {
		Toast.makeText(wv.getContext(), "x:" + x + ",y:" + y, Toast.LENGTH_LONG)
				.show();
		if (x != 0 || y != 0) {
			captureRandCodePic(x, y);
		}
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
}
