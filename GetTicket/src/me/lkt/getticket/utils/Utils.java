package me.lkt.getticket.utils;

import java.util.ArrayList;
import java.util.List;

import me.lkt.getticket.entity.Train;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class Utils {
	public static List<Train> getTrainInfos(String html) {
		try {
			Document doc = Jsoup.parse(html);
			List<Node> nodes = doc.child(0).child(1).childNodes();
			List<Train> trains = new ArrayList<Train>();
			int lastTrainIndex = -1;
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				if (node instanceof Element) {
					Element e = (Element) node;
					String id = e.attr("id");
					String className = e.attr("class");
					String onclick = e.attr("onclick");
					if (id != null && id.startsWith("train_num")) {// 票信息行
						lastTrainIndex++;
						Train train = new Train();
						trains.add(train);
						train.name = e.child(0).child(0).child(0).text();
						train.startTime = e.child(2).child(0).text();
					} else if ("btn72".equals(className)) {// 预订按钮
						trains.get(lastTrainIndex).jsOrder = onclick;
						trains.get(lastTrainIndex).hasSeat = true;
					}
				} else if (node instanceof TextNode) {
					TextNode tn = (TextNode) node;
					if ("预订".equals(tn.text()) || tn.text().contains("系统维护时间")
							|| "列车停运".equals(tn.text())) {
						trains.get(lastTrainIndex).hasSeat = false;
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
