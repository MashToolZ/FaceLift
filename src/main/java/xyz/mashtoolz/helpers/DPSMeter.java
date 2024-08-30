package xyz.mashtoolz.helpers;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.gui.DrawContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;

public class DPSMeter {

	private static FaceLift instance = FaceLift.getInstance();

	public static final ArrayList<String> damageNumbers = new ArrayList<>(
			Arrays.asList("０", "１", "２", "３", "４", "５", "６", "７", "８", "９"));

	private static long startTime = 0;
	private static long lastHitTime = 0;
	private static int damage = 0;
	private static int hits = 0;

	public static long getStartTime() {
		return DPSMeter.startTime;
	}

	public static long getLastHitTime() {
		return DPSMeter.lastHitTime;
	}

	public static void addDamage(int damage) {
		if (startTime == 0)
			DPSMeter.startTime = System.currentTimeMillis();
		DPSMeter.lastHitTime = System.currentTimeMillis();
		DPSMeter.damage += damage;
		DPSMeter.hits++;
	}

	public static int getDamage() {
		return DPSMeter.damage;
	}

	public static int getHits() {
		return DPSMeter.hits;
	}

	public static void reset() {
		DPSMeter.startTime = 0;
		DPSMeter.damage = 0;
		DPSMeter.hits = 0;
	}

	public static int getDPS() {
		if (DPSMeter.startTime == 0)
			return 0;
		var time = System.currentTimeMillis() - DPSMeter.startTime;
		if (time == 0)
			return 0;
		return Math.round(DPSMeter.damage / (time / 1000f));
	}

	public static Integer parseDamage(String text) {
		StringBuilder damage = new StringBuilder();
		Arrays.stream(text.split("")).filter(segment -> damageNumbers.contains(segment))
				.forEach(segment -> damage.append(segment));
		if (damage.length() == 0)
			return 0;
		return Integer.parseInt(damage.toString());
	}

	public static void draw(DrawContext context) {
		var remaining = instance.config.combat.dpsMeter.duration - (System.currentTimeMillis() - getLastHitTime());
		if (getStartTime() == 0)
			return;

		var ignoreTimer = instance.config.combat.dpsMeter.duration == -1;
		if (remaining <= 0 && !ignoreTimer) {
			reset();
			return;
		}

		String damageFormat = NumberFormatter.format(getDamage());
		String hitsFormat = NumberFormatter.format(getHits());
		String dpsFormat = NumberFormatter.format(getDPS());

		int damageWidth = instance.client.textRenderer.getWidth(damageFormat);
		int hitsWidth = instance.client.textRenderer.getWidth(hitsFormat);
		int dpsWidth = instance.client.textRenderer.getWidth(dpsFormat);

		int x = instance.config.combat.dpsMeter.position.x;
		int y = instance.config.combat.dpsMeter.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(5) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§cDPS Meter", x + 5, y + 5);

		if (!ignoreTimer && instance.config.combat.dpsMeter.showTimebar)
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, instance.config.combat.dpsMeter.duration, ColorUtils.hex2Int("FD3434", 0x90));

		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>Damage <#FDFDFD>", x + 5, y + 25 + RenderUtils.lh(0));
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + damageFormat, x + 107 - damageWidth, y + 25 + RenderUtils.lh(0));

		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>Hits <#FDFDFD>", x + 5, y + 25 + RenderUtils.lh(1));
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + hitsFormat, x + 107 - hitsWidth, y + 25 + RenderUtils.lh(1));

		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>DPS <#FDFDFD>", x + 5, y + 25 + RenderUtils.lh(2));
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + dpsFormat, x + 107 - dpsWidth, y + 25 + RenderUtils.lh(2));
	}
}
