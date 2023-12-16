package com.mashtoolz.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

import com.mashtoolz.FaceLift;
import com.mashtoolz.config.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {

	private FaceLift instance = FaceLift.getInstance();

	private MinecraftClient client;
	private Config config;
	private DPSMeter dpsMeter;

	public HudRenderer() {
		this.client = instance.client;
		this.config = instance.config;
		this.dpsMeter = instance.dpsMeter;
	}

	public void onHudRender(DrawContext context, float delta) {

		context.getMatrices().push();

		if (config.combatTimerEnabled)
			this.showCombatTimer(context);

		if (config.dpsMeterEnabled)
			this.showDPSMeter(context);

		if (config.xpDisplayEnabled)
			this.showXPDisplay(context);

		context.getMatrices().pop();
	}

	private Integer hex2Int(String colorHex, int opacity) {
		try {
			return (opacity << 24) | Integer.parseInt(colorHex, 16);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void showCombatTimer(DrawContext context) {

		var time = Math.max(dpsMeter.getLastHitTime(), config.lastHurtTime);
		var remaining = 12000 - (System.currentTimeMillis() - time);
		if (remaining <= 0)
			return;

		BigDecimal decimal = new BigDecimal(remaining / 1000f);
		var seconds = decimal.setScale(1, RoundingMode.HALF_UP).toPlainString();
		var percent = remaining / 12000f;

		int secondsWidth = client.textRenderer.getWidth(seconds);

		if (seconds.length() == 3) {
			secondsWidth = client.textRenderer.getWidth("0" + seconds);
			seconds = "<#00D1D1D1>0<#FDFDFD>" + seconds;
		}

		int x = config.combatTimerPosX;
		int y = config.combatTimerPosY;

		context.fill(x, y, x + 112, y + h(2) + 2, 0x80000000);
		drawTextWithShadow(context, "§eCombat Timer", x + p(0), y + p(0));
		drawTextWithShadow(context, "<#FDFDFD>" + seconds, x + w(0) - secondsWidth, y + h(0));

		var hex = String.format("%02x%02x%02x", (int) (255 * percent), (int) (255 * (1 - percent)), 0);
		drawTimeBar(context, x, y, (int) remaining, 12000, hex2Int(hex, 0x90));
	}

	public void showDPSMeter(DrawContext context) {

		var remaining = config.dpsMeterTime - (System.currentTimeMillis() - dpsMeter.getLastHitTime());
		if (dpsMeter.getStartTime() == 0)
			return;

		if (remaining <= 0) {
			dpsMeter.reset();
			return;
		}

		String damageFormat = NumberFormatter.format(dpsMeter.getDamage());
		String hitsFormat = NumberFormatter.format(dpsMeter.getHits());
		String dpsFormat = NumberFormatter.format(dpsMeter.getDPS());

		int damageWidth = client.textRenderer.getWidth(damageFormat);
		int hitsWidth = client.textRenderer.getWidth(hitsFormat);
		int dpsWidth = client.textRenderer.getWidth(dpsFormat);

		int x = config.dpsMeterPosX;
		int y = config.dpsMeterPosY;

		context.fill(x, y, x + 112, y + h(5) + 2, 0x80000000);
		drawTextWithShadow(context, "§cDPS Meter", x + p(0), y + p(0));
		drawTimeBar(context, x, y, (int) remaining, config.dpsMeterTime, hex2Int("FD3434", 0x90));

		drawTextWithShadow(context, "<#FFB2CC>Damage <#FDFDFD>", x + p(0), y + tbh(3) + lh(0));
		drawTextWithShadow(context, "<#FDFDFD>" + damageFormat, x + w(0) - damageWidth, y + tbh(3) + lh(0));

		drawTextWithShadow(context, "<#FFB2CC>Hits <#FDFDFD>", x + p(0), y + tbh(3) + lh(1));
		drawTextWithShadow(context, "<#FDFDFD>" + hitsFormat, x + w(0) - hitsWidth, y + tbh(3) + lh(1));

		drawTextWithShadow(context, "<#FFB2CC>DPS <#FDFDFD>", x + p(0), y + tbh(3) + lh(2));
		drawTextWithShadow(context, "<#FDFDFD>" + dpsFormat, x + w(0) - dpsWidth, y + tbh(3) + lh(2));
	}

	public void showXPDisplay(DrawContext context) {

		if (config.lastXPDisplay == null)
			return;

		var remaining = config.xpDisplayTime - (System.currentTimeMillis() - config.lastXPDisplay.getTime());
		if (remaining <= 0) {
			if (config.lastXPDisplay.getXP() != 0)
				config.lastXPDisplay.reset();
			return;
		}

		int height = config.xpDisplays.values().stream().filter(display -> display.getXP() > 0).mapToInt(display -> 10).sum();

		int x = config.xpDisplayPosX;
		int y = config.xpDisplayPosY;

		context.fill(x, y, x + 112, y + height + h(2) + 2, 0x80000000);
		drawTextWithShadow(context, "§aXP Display", x + p(0), y + p(0));
		drawTimeBar(context, x, y, (int) remaining, config.xpDisplayTime, hex2Int("34FD34", 0x90));

		int i = 0;
		for (var display : config.xpDisplays.values()) {

			if (display.getXP() == 0)
				continue;

			if (display.isVisible() && display.getTime() + config.xpDisplayTime < System.currentTimeMillis()) {
				display.reset();
				continue;
			}

			if (!display.isVisible())
				display.setVisible(true);

			var skill = display.getColor() + display.getKey();
			var xp = NumberFormatter.format(display.getXP());

			int xpWidth = client.textRenderer.getWidth(xp);

			drawTextWithShadow(context, skill, x + p(0), y + tbh(3) + (i * 10));
			drawTextWithShadow(context, "<#FDFDFD>" + xp, x + w(-xpWidth), y + tbh(3) + (i * 10));

			i++;
		}
	}

	private void drawTimeBar(DrawContext context, int x, int y, int remaining, int max, int color) {
		int barWidth = x + p(1) + Math.round(remaining * 101 / max);
		context.fill(x + p(0), y + tbh(-7), x + w(0), y + tbh(0), hex2Int("D1D1D1", 0x40));
		context.fill(x + p(1), y + tbh(-6), x + w(-1), y + tbh(-1), hex2Int("D1D1D1", 0x40));
		context.fill(x + p(1), y + tbh(-6), barWidth, y + tbh(-1), color);
	}

	private void drawTextWithShadow(DrawContext context, String text, int x, int y) {

		int color = 0xD1D1D1;
		var pattern = Pattern.compile("<#([A-Fa-f0-9]{6,8})>");
		var matcher = pattern.matcher(text);
		var segments = text.split(pattern.pattern());

		for (var segment : segments) {
			context.drawTextWithShadow(client.textRenderer, segment, x, y, color);
			x += client.textRenderer.getWidth(segment);

			if (matcher.find()) {
				var group = matcher.group(1);
				var hex = group.substring(0, 6);
				int opacity = 0xFF;
				if (group.length() == 8) {
					hex = group.substring(2, 8);
					opacity = Math.max(4, Math.min(255, Integer.parseInt(group.substring(0, 2), 16)));
				}

				color = hex2Int(hex, opacity);
			} else {
				color = 0xD1D1D1;
			}
		}
	}

	private int p(int n) {
		return 5 + n;
	}

	private int w(int n) {
		return 112 - 5 + n;
	}

	private int h(int n) {
		return 10 * n + 5;
	}

	private int lh(int n) {
		return 10 * n;
	}

	private int tbh(int n) {
		return 22 + n;
	}
}
