package xyz.mashtoolz.displays;

import net.minecraft.client.gui.DrawContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceFont;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.mixins.InGameHudAccessor;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CombatTimer {

	private static final FaceLift INSTANCE = FaceLift.getInstance();

	public static void draw(DrawContext context) {

		long time = Math.max(DPSMeter.getLastHitTime(), FaceConfig.General.lastHurtTime);
		long remaining = 12000 - (System.currentTimeMillis() - time);
		if (remaining <= 0)
			return;

		BigDecimal decimal = new BigDecimal(remaining / 1000.0);
		String seconds = decimal.setScale(1, RoundingMode.HALF_UP).toPlainString();
		float percent = remaining / 12000f;

		int secondsWidth = INSTANCE.CLIENT.textRenderer.getWidth(seconds);

		if (seconds.length() == 3) {
			secondsWidth = INSTANCE.CLIENT.textRenderer.getWidth("0" + seconds);
			seconds = "<#00D1D1D1>0<#FDFDFD>" + seconds;
		}

		int x = INSTANCE.CONFIG.combat.combatTimer.position.x;
		int y = INSTANCE.CONFIG.combat.combatTimer.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§eCombat Timer", x + 5, y + 5);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + seconds, x + 107 - secondsWidth, y + RenderUtils.h(0));

		int barColor = ColorUtils.hex2Int(String.format("#%02x%02x00", (int) (255 * percent), (int) (255 * (1 - percent))), 0x90);
		RenderUtils.drawTimeBar(context, x, y, (int) remaining, 12000, barColor);
	}

	public static void update() {
		var inGameHud = (InGameHudAccessor) INSTANCE.CLIENT.inGameHud;
		if (inGameHud == null)
			return;

		var overlayMessage = inGameHud.getOverlayMessage();
		if (overlayMessage != null) {
			String overlayText = overlayMessage.getString();
			if (FaceFont.keys(FType.HURT_TIME).stream().anyMatch(overlayText::contains))
				FaceConfig.General.lastHurtTime = System.currentTimeMillis();
		}

        assert INSTANCE.CLIENT.player != null;
        int playerHurtTime = INSTANCE.CLIENT.player.hurtTime;
		if (FaceConfig.General.hurtTime == 0 && playerHurtTime != 0)
			FaceConfig.General.hurtTime = playerHurtTime;

		if (FaceConfig.General.hurtTime == -1 && playerHurtTime == 0)
			FaceConfig.General.hurtTime = 0;

		if (FaceConfig.General.hurtTime > 0) {
			FaceConfig.General.hurtTime = -1;
			var recentDamageSource = INSTANCE.CLIENT.player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().equals("fall"))
				FaceConfig.General.lastHurtTime = System.currentTimeMillis();
		}
	}
}
