package xyz.mashtoolz.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import net.minecraft.client.gui.DrawContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;

public class CombatTimer {

	private static FaceLift instance = FaceLift.getInstance();

	public static void draw(DrawContext context) {

		var time = Math.max(DPSMeter.getLastHitTime(), instance.config.general.lastHurtTime);
		var remaining = 12000 - (System.currentTimeMillis() - time);
		if (remaining <= 0)
			return;

		BigDecimal decimal = new BigDecimal(remaining / 1000f);
		var seconds = decimal.setScale(1, RoundingMode.HALF_UP).toPlainString();
		var percent = remaining / 12000f;

		int secondsWidth = instance.client.textRenderer.getWidth(seconds);

		if (seconds.length() == 3) {
			secondsWidth = instance.client.textRenderer.getWidth("0" + seconds);
			seconds = "<#00D1D1D1>0<#FDFDFD>" + seconds;
		}

		int x = instance.config.combat.combatTimer.position.x;
		int y = instance.config.combat.combatTimer.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§eCombat Timer", x + 5, y + 5);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + seconds, x + 107 - secondsWidth, y + RenderUtils.h(0));

		var hex = String.format("%02x%02x%02x", (int) (255 * percent), (int) (255 * (1 - percent)), 0);
		RenderUtils.drawTimeBar(context, x, y, (int) remaining, 12000, ColorUtils.hex2Int(hex, 0x90));
	}
}
