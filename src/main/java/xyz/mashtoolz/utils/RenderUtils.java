package xyz.mashtoolz.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.mixins.HandledScreenAccessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenderUtils {

	private static final FaceLift INSTANCE = FaceLift.getInstance();

	private static final Pattern COLOR_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6,8})>");
	private static final int DEFAULT_COLOR = 0xD1D1D1;

	public static void drawTextWithShadow(DrawContext context, String text, int x, int y) {
		Matcher matcher = COLOR_PATTERN.matcher(text);
		int color = DEFAULT_COLOR;
		int lastEnd = 0;

		while (matcher.find()) {
			String segment = text.substring(lastEnd, matcher.start());
			context.drawTextWithShadow(INSTANCE.CLIENT.textRenderer, segment, x, y, color);
			x += INSTANCE.CLIENT.textRenderer.getWidth(segment);

			String group = matcher.group(1);
			String hex = group.length() == 8 ? group.substring(2, 8) : group.substring(0, 6);
			int opacity = group.length() == 8 ? Math.max(4, Math.min(255, Integer.parseInt(group.substring(0, 2), 16))) : 0xFF;

			color = ColorUtils.hex2Int(hex, opacity);
			lastEnd = matcher.end();
		}

		if (lastEnd < text.length())
			context.drawTextWithShadow(INSTANCE.CLIENT.textRenderer, text.substring(lastEnd), x, y, color);
	}

	public static void drawTimeBar(DrawContext context, int x, int y, int remaining, int max, int color) {
		int barWidth = Math.min(x + 6 + Math.round(remaining * 101 / (float) max), x + 107);
		int colorWithAlpha = ColorUtils.hex2Int("D1D1D1", 0x40);
		context.fill(x + 5, y + 15, x + 107, y + 22, colorWithAlpha);
		context.fill(x + 6, y + 16, x + 106, y + 21, colorWithAlpha);
		context.fill(x + 6, y + 16, barWidth, y + 21, color);
	}

	public static int h(int n) {
		return 10 * n + 5;
	}

	public static void enableBlend() {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}

	public static void disableBlend() {
		RenderSystem.disableBlend();
	}

	public static void drawTooltip(DrawContext context, ItemStack stack, int mouseX, int mouseY) {
		var matrices = context.getMatrices();
		matrices.push();
		RenderSystem.disableDepthTest();

		List<Text> text = stack.getTooltip(TooltipContext.DEFAULT, INSTANCE.CLIENT.player, INSTANCE.CLIENT.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);
		var components = text.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList();
		int maxWidth = components.stream().mapToInt(c -> c.getWidth(INSTANCE.CLIENT.textRenderer)).max().orElse(0);

		context.drawTooltip(INSTANCE.CLIENT.textRenderer, text, stack.getTooltipData(), mouseX - maxWidth - 25, mouseY);

		RenderSystem.enableDepthTest();
		matrices.pop();
	}

	public static void compareAndRenderTooltip(HandledScreenAccessor screen, DrawContext context, int mouseX, int mouseY) {
		var focusedSlot = screen.getFocusedSlot();
		if (focusedSlot == null || focusedSlot.getStack().isEmpty())
			return;

		var focusedItem = FaceItem.from(focusedSlot.getStack());
		if (focusedItem.isInvalid())
			return;

		var compareSlot = focusedItem.getFaceSlot();
		if (compareSlot == null)
			return;

		var compareStack = compareSlot.getStack();
		if (compareStack.isEmpty() || ItemUtils.compareStacks(focusedSlot.getStack(), compareStack))
			return;

		RenderUtils.drawTooltip(context, compareStack, mouseX, mouseY);
	}
}
