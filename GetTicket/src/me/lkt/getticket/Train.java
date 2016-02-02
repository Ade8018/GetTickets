package me.lkt.getticket;

public class Train {
	String name;
	String startTime;
	boolean hasSeat;

	@Override
	public String toString() {
		return "班次:" + name + ", 发车时间:" + startTime + ", 余票:"
				+ (hasSeat ? "有" : "无");
	}

}
