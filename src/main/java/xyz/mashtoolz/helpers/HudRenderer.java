package xyz.mashtoolz.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.mixins.InGameHudMixin;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.utils.TimeUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public class HudRenderer {

	private FaceLift instance = FaceLift.getInstance();

	private MinecraftClient client;
	private Config config;

	private DPSMeter dpsMeter;
	private ArenaTimer arenaTimer;

	private final Identifier ITEM_GLOW = new Identifier("facelift", "textures/gui/item_glow.png");
	private final Identifier ICON_TOOL = new Identifier("facelift", "textures/gui/icon/tool.png");

	final ArrayList<Item> IGNORED_ITEMS = new ArrayList<>(Arrays.asList(Items.BARRIER, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.PLAYER_HEAD));

	public HudRenderer() {
		this.client = instance.client;
		this.config = instance.config;
		this.dpsMeter = instance.dpsMeter;
		this.arenaTimer = instance.arenaTimer;
	}

	public void onHudRender(DrawContext context, float delta) {

		if (!config.onFaceLand)
			return;

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

	public void drawItemSlot(DrawContext context, Slot slot) {

		ItemStack stack = slot.getStack();
		if (stack.isEmpty() || IGNORED_ITEMS.contains(stack.getItem()))
			return;

		int x = slot.x, y = slot.y;
		TextColor color = ColorUtils.getItemColor(stack);

		if (color == null)
			return;

		float[] rgb = ColorUtils.getRGB(color);

		MatrixStack matrices = context.getMatrices();
		matrices.push();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		matrices.translate(0.0f, 0.0f, 100.0f);
		RenderSystem.setShaderTexture(0, ITEM_GLOW);
		RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], config.general.rarityOpacity);
		context.drawTexture(ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);

		// matrices.translate(0.0f, 0.0f, 200.0f);
		// RenderSystem.setShaderTexture(0, ICON_TOOL);
		// RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		// context.drawTexture(ICON_TOOL, x, y, 0, 0, 4, 4, 4, 4);

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		// drawLine(context, x + 2, y + 2, x + 12, y + 12, hex2Int("#DF3434", 0xFF), 2);

		matrices.pop();
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

		context.fill(x, y, x + 112, y + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§eCombat Timer", x + 5, y + 5);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + seconds, x + 107 - secondsWidth, y + RenderUtils.h(0));

		var hex = String.format("%02x%02x%02x", (int) (255 * percent), (int) (255 * (1 - percent)), 0);

		if (config.combatTimer.showTimebar)
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, 12000, ColorUtils.hex2Int(hex, 0x90));
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

		context.fill(x, y, x + 112, y + RenderUtils.h(5) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§cDPS Meter", x + 5, y + 5);

		if (!ignoreTimer && config.dpsMeter.showTimebar)
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, config.dpsMeter.duration, ColorUtils.hex2Int("FD3434", 0x90));

		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>Damage <#FDFDFD>", x + 5, y + 25 + RenderUtils.lh(0));
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + damageFormat, x + 107 - damageWidth, y + 25 + RenderUtils.lh(0));

		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>Hits <#FDFDFD>", x + 5, y + 25 + RenderUtils.lh(1));
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + hitsFormat, x + 107 - hitsWidth, y + 25 + RenderUtils.lh(1));

		RenderUtils.drawTextWithShadow(context, "<#FFB2CC>DPS <#FDFDFD>", x + 5, y + 25 + RenderUtils.lh(2));
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + dpsFormat, x + 107 - dpsWidth, y + 25 + RenderUtils.lh(2));
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

		int height = config.xpDisplays.values().stream().filter(display -> display.getXP() > 0).mapToInt(display -> 10).sum();
		int x = config.xpDisplay.position.x;
		int y = config.xpDisplay.position.y;

		context.fill(x, y, x + 112, y + height + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§aXP Display", x + 5, y + 5);

		if (!ignoreTimer && config.xpDisplay.showTimebar)
			RenderUtils.drawTimeBar(context, x, y, (int) remaining, config.xpDisplay.duration, ColorUtils.hex2Int("34FD34", 0x90));

		int i = 0;
		for (var display : config.xpDisplays.values()) {

			if (display.getXP() == 0)
				continue;

			if (display.isVisible() && display.getTime() + config.xpDisplay.duration < System.currentTimeMillis() && !ignoreTimer) {
				display.reset();
				continue;
			}

			if (!display.isVisible())
				display.setVisible(true);

			var skill = display.getColor() + display.getKey();
			var xp = NumberFormatter.format(display.getXP());
			var gain = config.xpDisplay.showLastGain ? "  +" + NumberFormatter.format(display.getGain()) : "";

			RenderUtils.drawTextWithShadow(context, skill, x + 5, y + 25 + (i * 10));

			int type = config.xpDisplay.displayType;
			var perN = display.getTotalTime() / (1000.0 * 60 * (type == 2 ? 60 : 1));
			if (type != 0)
				xp = NumberFormatter.format((int) (display.getXP() / perN));

			RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + xp + gain, x + 107 - client.textRenderer.getWidth(xp), y + 25 + (i * 10));

			i++;
		}
	}

	public void updateArenaTimer() {

		var inGameHud = (InGameHudMixin) client.inGameHud;
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

		var totalHMS = TimeUtils.timeToHMS(totalTime);
		var totalStr = String.format("%02d:%02d.%d", totalHMS[1], totalHMS[2], totalHMS[3]);
		var totalStrWidth = client.textRenderer.getWidth(totalStr);

		var waveHMS = TimeUtils.timeToHMS(arenaTimer.getCurrentWaveTime());
		var waveStr = String.format("%02d:%02d.%d", waveHMS[1], waveHMS[2], waveHMS[3]);
		var waveStrWidth = client.textRenderer.getWidth(waveStr);

		int x = config.arenaTimer.position.x;
		int y = config.arenaTimer.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§3Arena Timer", x + 5, y + 5);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + totalStr, x + 107 - totalStrWidth, y + 5);
		RenderUtils.drawTextWithShadow(context, "§bWave Timer", x + 5, y + 5 + 10);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + waveStr, x + 107 - waveStrWidth, y + 5 + 10);
	}
}
