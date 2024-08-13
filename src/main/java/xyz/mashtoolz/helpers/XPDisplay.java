package xyz.mashtoolz.helpers;

import xyz.mashtoolz.FaceLift;

public class XPDisplay {

	private FaceLift instance = FaceLift.getInstance();

	private String key;
	private String color;
	private int xp;
	private int lastxp;
	private long time;
	private long totalTime = 0;
	private boolean visible;

	public XPDisplay(String key, String color, int amount, long time, boolean visible) {
		this.key = key;
		this.color = color;
		this.xp = amount;
		this.time = time;
		this.visible = visible;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getXP() {
		return xp;
	}

	public void setXP(int amount) {
		this.lastxp = this.xp;
		this.xp = amount;
	}

	public int getGain() {
		return xp - lastxp;
	}

	public long getTotalTime() {
		return System.currentTimeMillis() - totalTime;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		if (this.totalTime == 0 && time > 0)
			this.totalTime = time;
		this.time = time;
		instance.config.lastXPDisplay = this;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void reset() {
		this.setXP(0);
		this.setVisible(false);
		this.totalTime = 0;
	}
}