package me.lkt.getticket.entity;

public class Train {
	public String name;
	public String startTime;
	public boolean hasSeat;
	public String jsOrder;

	@Override
	public String toString() {
		return "班次:" + name + ", 发车时间:" + startTime + ", 余票:"
				+ (hasSeat ? "有" : "无");
	}

}
