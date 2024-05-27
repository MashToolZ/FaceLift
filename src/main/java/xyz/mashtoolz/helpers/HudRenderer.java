package xyz.mashtoolz.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.mixins.IinGameHud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HudRenderer {

	private FaceLift instance = FaceLift.getInstance();

	private MinecraftClient client;
	private Config config;
	private DPSMeter dpsMeter;
	private ArenaTimer arenaTimer;

	public HudRenderer() {
		this.client = instance.client;
		this.config = instance.config;
		this.dpsMeter = instance.dpsMeter;
		this.arenaTimer = instance.arenaTimer;
	}

	public void onHudRender(DrawContext context, float delta) {

		context.getMatrices().push();

		if (config.combatTimer.enabled)
			this.drawCombatTimer(context);

		if (config.dpsMeter.enabled)
			this.drawDPSMeter(context);

		if (config.xpDisplay.enabled)
			this.drawXPDisplay(context);

		if (config.arenaTimer.enabled) {
			this.updateArenaTimer();
			this.drawArenaTimer(context);
		}

		context.getMatrices().pop();
	}

	private Integer hex2Int(String colorHex, int opacity) {
		try {
			return (opacity << 24) | Integer.parseInt(colorHex, 16);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void drawCombatTimer(DrawContext context) {

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

		int x = config.combatTimer.position.x;
		int y = config.combatTimer.position.y;

		context.fill(x, y, x + 112, y + h(2) + 2, 0x80000000);
		drawTextWithShadow(context, "§eCombat Timer", x + p(0), y + p(0));
		drawTextWithShadow(context, "<#FDFDFD>" + seconds, x + w(0) - secondsWidth, y + h(0));

		var hex = String.format("%02x%02x%02x", (int) (255 * percent), (int) (255 * (1 - percent)), 0);
		drawTimeBar(context, x, y, (int) remaining, 12000, hex2Int(hex, 0x90));
	}

	public void drawDPSMeter(DrawContext context) {

		var remaining = config.dpsMeter.duration - (System.currentTimeMillis() - dpsMeter.getLastHitTime());
		if (dpsMeter.getStartTime() == 0)
			return;

		var ignoreTimer = config.dpsMeter.duration == -1;

		if (remaining <= 0 && !ignoreTimer) {
			dpsMeter.reset();
			return;
		}

		String damageFormat = NumberFormatter.format(dpsMeter.getDamage());
		String hitsFormat = NumberFormatter.format(dpsMeter.getHits());
		String dpsFormat = NumberFormatter.format(dpsMeter.getDPS());

		int damageWidth = client.textRenderer.getWidth(damageFormat);
		int hitsWidth = client.textRenderer.getWidth(hitsFormat);
		int dpsWidth = client.textRenderer.getWidth(dpsFormat);

		int x = config.dpsMeter.position.x;
		int y = config.dpsMeter.position.y;

		context.fill(x, y, x + 112, y + h(5) + 2, 0x80000000);
		drawTextWithShadow(context, "§cDPS Meter", x + p(0), y + p(0));

		if (!ignoreTimer)
			drawTimeBar(context, x, y, (int) remaining, config.dpsMeter.duration, hex2Int("FD3434", 0x90));

		drawTextWithShadow(context, "<#FFB2CC>Damage <#FDFDFD>", x + p(0), y + tbh(3) + lh(0));
		drawTextWithShadow(context, "<#FDFDFD>" + damageFormat, x + w(0) - damageWidth, y + tbh(3) + lh(0));

		drawTextWithShadow(context, "<#FFB2CC>Hits <#FDFDFD>", x + p(0), y + tbh(3) + lh(1));
		drawTextWithShadow(context, "<#FDFDFD>" + hitsFormat, x + w(0) - hitsWidth, y + tbh(3) + lh(1));

		drawTextWithShadow(context, "<#FFB2CC>DPS <#FDFDFD>", x + p(0), y + tbh(3) + lh(2));
		drawTextWithShadow(context, "<#FDFDFD>" + dpsFormat, x + w(0) - dpsWidth, y + tbh(3) + lh(2));
	}

	public void drawXPDisplay(DrawContext context) {

		if (config.lastXPDisplay == null)
			return;

		var ignoreTimer = config.xpDisplay.duration == -1;

		var remaining = config.xpDisplay.duration - (System.currentTimeMillis() - config.lastXPDisplay.getTime());
		if (remaining <= 0 && !ignoreTimer) {
			if (config.lastXPDisplay.getXP() != 0)
				config.lastXPDisplay.reset();
			return;
		}

		int height = config.xpDisplays.values().stream().filter(display -> display.getXP() > 0).mapToInt(display -> 10)
				.sum();

		int x = config.xpDisplay.position.x;
		int y = config.xpDisplay.position.y;

		context.fill(x, y, x + 112, y + height + h(2) + 2, 0x80000000);
		drawTextWithShadow(context, "§aXP Display", x + p(0), y + p(0));

		if (!ignoreTimer)
			drawTimeBar(context, x, y, (int) remaining, config.xpDisplay.duration, hex2Int("34FD34", 0x90));

		int i = 0;
		for (var display : config.xpDisplays.values()) {

			if (display.getXP() == 0)
				continue;

			if (display.isVisible() && display.getTime() + config.xpDisplay.duration < System.currentTimeMillis()
					&& !ignoreTimer) {
				display.reset();
				continue;
			}

			if (!display.isVisible())
				display.setVisible(true);

			var skill = display.getColor() + display.getKey();
			var xp = NumberFormatter.format(display.getXP());

			int xpWidth = client.textRenderer.getWidth(xp);

			drawTextWithShadow(context, skill, x + p(0), y + tbh(3) + (i * 10));

			switch (config.xpDisplay.displayType) {

				default: {
					drawTextWithShadow(context, "<#FDFDFD>" + xp, x + w(-xpWidth), y + tbh(3) + (i * 10));
					break;
				}

				case 1: {
					var displayMinutes = display.getTotalTime() / (1000.0 * 60);
					var xpm = NumberFormatter.format((int) (display.getXP() / displayMinutes));
					int xpmWidth = client.textRenderer.getWidth(xpm);
					drawTextWithShadow(context, "<#FDFDFD>" + xpm, x + w(-xpmWidth), y + tbh(3) + (i * 10));
					break;
				}

				case 2: {
					var displayHours = display.getTotalTime() / (1000.0 * 60 * 60);
					var xph = NumberFormatter.format((int) (display.getXP() / displayHours));
					int xphWidth = client.textRenderer.getWidth(xph);
					drawTextWithShadow(context, "<#FDFDFD>" + xph, x + w(-xphWidth), y + tbh(3) + (i * 10));
					break;
				}

			}

			i++;
		}
	}

	public void updateArenaTimer() {

		var inGameHud = (IinGameHud) client.inGameHud;
		if (inGameHud == null)
			return;

		var title = inGameHud.getTitle();
		if (inGameHud.getTitle() == null)
			return;

		var subtitle = inGameHud.getSubtitle() != null ? inGameHud.getSubtitle() : Text.empty();

		for (var regex : arenaTimer.regexes) {

			String[] arr = regex.getKey().split("\\.");
			var type = arr[0];
			var key = arr[1];

			var match = regex.getPattern().matcher(type.equals("title") ? title.getString() : subtitle.getString());

			if (!match.find())
				continue;

			switch (key) {
				case "waveStart": {
					if (!arenaTimer.isActive())
						arenaTimer.start();

					if (arenaTimer.isPaused())
						arenaTimer.startWave();
					break;
				}

				case "waveEnd": {
					if (arenaTimer.isActive() && !arenaTimer.isPaused())
						arenaTimer.endWave();
					break;
				}

				case "arenaEnd": {
					if (arenaTimer.isActive())
						arenaTimer.end();
					break;
				}
			}
		}
	}

	public void drawArenaTimer(DrawContext context) {

		if (!arenaTimer.isActive())
			return;

		var totalTime = arenaTimer.getTotalTime();

		var totalHMS = timeToHMS(totalTime);
		var totalStr = String.format("%02d:%02d.%d", totalHMS[1], totalHMS[2], totalHMS[3]);
		var totalStrWidth = client.textRenderer.getWidth(totalStr);

		var waveHMS = timeToHMS(arenaTimer.getCurrentWaveTime());
		var waveStr = String.format("%02d:%02d.%d", waveHMS[1], waveHMS[2], waveHMS[3]);
		var waveStrWidth = client.textRenderer.getWidth(waveStr);

		int x = config.arenaTimer.position.x;
		int y = config.arenaTimer.position.y;

		context.fill(x, y, x + 112, y + h(2) + 2, 0x80000000);
		drawTextWithShadow(context, "§3Arena Timer", x + p(0), y + p(0));
		drawTextWithShadow(context, "<#FDFDFD>" + totalStr, x + w(0) - totalStrWidth, y + p(0));
		drawTextWithShadow(context, "§bWave Timer", x + p(0), y + p(0) + 10);
		drawTextWithShadow(context, "<#FDFDFD>" + waveStr, x + w(0) - waveStrWidth, y + p(0) + 10);
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

	private long[] timeToHMS(long time) {
		var ms = time % 1000;
		time /= 1000;
		var s = time % 60;
		time /= 60;
		var m = time % 60;
		time /= 60;
		var h = time % 24;
		return new long[] { h, m, s, ms / 100 };
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
