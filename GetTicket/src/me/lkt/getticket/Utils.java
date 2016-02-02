package me.lkt.getticket;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.Log;

public class Utils {
	public static List<Train> getTrainInfos(String html) {
		try {
			Document doc = Jsoup.parse(html); 
			List<Node> nodes = doc.child(0).child(1).childNodes();
			List<Train> trains = new ArrayList<Train>();
			int lastTrainIndex = -1;
			boolean isLastTrianFindTicketLeftInfo = true;
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				if (node instanceof Element) {
					Element e = (Element) node;
					String id = e.attr("id");
					if (id != null && id.startsWith("train_num")) {
						if (!isLastTrianFindTicketLeftInfo) {
							trains.get(lastTrainIndex).hasSeat = true;
						}
						isLastTrianFindTicketLeftInfo = false;
						lastTrainIndex++;
						Train train = new Train();
						trains.add(train);
						train.name = e.child(0).child(0).child(0).text();
						train.startTime = e.child(2).child(0).text();
					}
				} else if (node instanceof TextNode) {
					TextNode tn = (TextNode) node;
					if ("预订".equals(tn.text())) {
						trains.get(lastTrainIndex).hasSeat = false;
						isLastTrianFindTicketLeftInfo = true;
					}
				}
			}
			return trains;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
