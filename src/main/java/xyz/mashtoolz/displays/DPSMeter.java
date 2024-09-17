package xyz.mashtoolz.displays;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceFont;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.NumberUtils;
import xyz.mashtoolz.utils.RenderUtils;

public class DPSMeter {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	public static final Map<String, TextDisplayEntity> TEXT_DISPLAYS = new HashMap<>();

	private static long startTime = 0;
	private static long lastHitTime = 0;
	private static int damage = 0;
	private static int hits = 0;

	public static long getStartTime() {
		return startTime;
	}

	public static long getLastHitTime() {
		return lastHitTime;
	}

	public static void addDamage(int damage) {
		if (startTime == 0)
			startTime = System.currentTimeMillis();
		lastHitTime = System.currentTimeMillis();
		DPSMeter.damage += damage;
		hits++;
	}

	public static int getDamage() {
		return damage;
	}

	public static int getHits() {
		return hits;
	}

	public static void reset() {
		startTime = 0;
		damage = 0;
		hits = 0;
	}

	public static int getDPS() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		return (startTime == 0 || elapsedTime == 0) ? 0 : Math.round(damage / (elapsedTime / 1000f));
	}

	public static Integer parseDamage(String text) {
		String numericString = text.chars()
				.mapToObj(c -> String.valueOf((char) c))
				.filter(c -> FaceFont.keys(FType.DAMAGE_NUMBERS).contains(c.toString()))
				.collect(Collectors.joining());
		return numericString.isEmpty() ? 0 : Integer.parseInt(numericString);
	}

	public static void draw(DrawContext context) {

		var remaining = INSTANCE.CONFIG.combat.dpsMeter.duration - (System.currentTimeMillis() - getLastHitTime());
		if (getStartTime() == 0)
			return;

		var ignoreTimer = INSTANCE.CONFIG.combat.dpsMeter.duration == -1;
		if (remaining <= 0 && !ignoreTimer) {
			reset();
			return;
		}

		String damageFormat = NumberUtils.format(damage);
		String hitsFormat = NumberUtils.format(hits);
		String dpsFormat = NumberUtils.format(getDPS());

		int x = INSTANCE.CONFIG.combat.dpsMeter.position.x;
		int y = INSTANCE.CONFIG.combat.dpsMeter.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(5) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§cDPS Meter", x + 5, y + 5);

		if (!ignoreTimer && INSTANCE.CONFIG.combat.dpsMeter.showTimebar) {
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, INSTANCE.CONFIG.combat.dpsMeter.duration, ColorUtils.hex2Int("FD3434", 0x90));
		}

		int[] yOffsets = { 25, 35, 45 };

		drawStats(context, "Damage", damageFormat, x, y + yOffsets[0]);
		drawStats(context, "Hits", hitsFormat, x, y + yOffsets[1]);
		drawStats(context, "DPS", dpsFormat, x, y + yOffsets[2]);
	}

	private static void drawStats(DrawContext context, String label, String value, int x, int y) {
		int valueWidth = INSTANCE.CLIENT.textRenderer.getWidth(value);
		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>" + label + " <#FDFDFD>", x + 5, y);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + value, x + 112 - valueWidth - 5, y);
	}

	public static void update() {
		for (Iterator<TextDisplayEntity> iterator = TEXT_DISPLAYS.values().iterator(); iterator.hasNext();) {
			TextDisplayEntity entity = iterator.next();
			if (entity.getData() == null || entity.getData().text() == null)
				continue;
			String text = entity.getData().text().getString();
			int damage = parseDamage(text);
			if (damage > 0)
				addDamage(damage);
			iterator.remove();
		}
	}
}
