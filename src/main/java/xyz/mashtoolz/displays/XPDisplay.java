package xyz.mashtoolz.displays;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.DrawContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig.General.Display.DisplayType;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.NumberUtils;
import xyz.mashtoolz.utils.RenderUtils;

public class XPDisplay {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	public static Map<String, XPDisplay> DISPLAYS = new HashMap<>();
	public static XPDisplay lastDisplay;

	private String key;
	private String color;
	private int xp;
	private int lastXp;
	private long time;
	private long totalTime;
	private boolean visible;

	public XPDisplay(String key, String color, int amount, long time, boolean visible) {
		this.key = key;
		this.color = color;
		this.xp = amount;
		this.time = time;
		this.visible = visible;
		this.totalTime = 0;
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
		this.lastXp = this.xp;
		this.xp = amount;
	}

	public int getGain() {
		return xp - lastXp;
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
		lastDisplay = this;
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
		if (lastDisplay == null)
			return;

		var config = INSTANCE.CONFIG.general.xpDisplay;
		boolean ignoreTimer = config.duration == -1;
		long remaining = config.duration - (System.currentTimeMillis() - lastDisplay.getTime());

		if (remaining <= 0 && !ignoreTimer) {
			if (lastDisplay.getXP() != 0)
				lastDisplay.reset();
			return;
		}

		int height = DISPLAYS.values().stream().filter(display -> display.getXP() > 0).mapToInt(display -> 10).sum();

		int x = config.position.x;
		int y = config.position.y;

		context.fill(x, y, x + 112, y + height + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§aXP Display", x + 5, y + 5);

		if (!ignoreTimer && config.showTimebar)
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, config.duration, ColorUtils.hex2Int("34FD34", 0x90));

		int i = 0;
		for (var display : DISPLAYS.values())
			if (display.draw(context, x, y, i, ignoreTimer))
				i++;
	}

	public boolean draw(DrawContext context, int x, int y, int i, boolean ignoreTimer) {

		if (getXP() == 0)
			return false;

		if (isVisible() && getTime() + INSTANCE.CONFIG.general.xpDisplay.duration < System.currentTimeMillis() && !ignoreTimer) {
			this.reset();
			return false;
		}

		if (!isVisible())
			this.setVisible(true);

		var skill = getColor() + getKey();
		var xpFormatted = NumberUtils.format(getXP());
		var gain = INSTANCE.CONFIG.general.xpDisplay.showLastGain ? "  +" + NumberUtils.format(getGain()) : "";

		RenderUtils.drawTextWithShadow(context, skill, x + 5, y + 25 + (i * 10));

		var type = INSTANCE.CONFIG.general.xpDisplay.displayType;
		var perN = getTotalTime() / (1000.0 * 60 * (type == DisplayType.PER_HOUR ? 60 : 1));
		if (type != DisplayType.DEFAULT)
			xpFormatted = NumberUtils.format((int) (getXP() / perN));

		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + xpFormatted + gain, x + 107 - INSTANCE.CLIENT.textRenderer.getWidth(xpFormatted), y + 25 + (i * 10));

		return true;
	}
}
