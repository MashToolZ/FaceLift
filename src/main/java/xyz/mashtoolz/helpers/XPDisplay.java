package xyz.mashtoolz.helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;

public class XPDisplay {

	private static FaceLift instance = FaceLift.getInstance();

	private static MinecraftClient client = instance.client;
	private static Config config = instance.config;

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
		config.lastXPDisplay = this;
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

	public static void draw(DrawContext context) {
		if (config.lastXPDisplay == null)
			return;

		var ignoreTimer = config.xpDisplay.duration == -1;
		var remaining = config.xpDisplay.duration - (System.currentTimeMillis() - config.lastXPDisplay.getTime());
		if (remaining <= 0 && !ignoreTimer) {
			if (config.lastXPDisplay.getXP() != 0)
				config.lastXPDisplay.reset();
			return;
		}

		int height = config.xpDisplays.values().stream().filter(display -> display.getXP() > 0).mapToInt(display -> 10).sum();
		int x = config.xpDisplay.position.x;
		int y = config.xpDisplay.position.y;

		context.fill(x, y, x + 112, y + height + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§aXP Display", x + 5, y + 5);

		if (!ignoreTimer && config.xpDisplay.showTimebar)
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, config.xpDisplay.duration, ColorUtils.hex2Int("34FD34", 0x90));

		int i = 0;
		for (var display : config.xpDisplays.values())
			if (!display.draw(context, x, y, i, ignoreTimer))
				continue;
	}

	public boolean draw(DrawContext context, int x, int y, int i, boolean ignoreTimer) {

		if (getXP() == 0)
			return false;

		if (isVisible() && getTime() + config.xpDisplay.duration < System.currentTimeMillis() && !ignoreTimer) {
			this.reset();
			return false;
		}

		if (!this.isVisible())
			this.setVisible(true);

		var skill = this.getColor() + this.getKey();
		var xp = NumberFormatter.format(this.getXP());
		var gain = config.xpDisplay.showLastGain ? "  +" + NumberFormatter.format(getGain()) : "";

		RenderUtils.drawTextWithShadow(context, skill, x + 5, y + 25 + (i * 10));

		int type = config.xpDisplay.displayType;
		var perN = getTotalTime() / (1000.0 * 60 * (type == 2 ? 60 : 1));
		if (type != 0)
			xp = NumberFormatter.format((int) (getXP() / perN));

		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + xp + gain, x + 107 - client.textRenderer.getWidth(xp), y + 25 + (i * 10));

		i++;

		return true;
	}
}