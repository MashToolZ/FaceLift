package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;

@Mixin(TooltipBackgroundRenderer.class)
public abstract class TooltipBackgroundRendererMixin {

	@Inject(method = "render", at = @At("HEAD"))
	private static void render(DrawContext context, int x, int y, int width, int height, int z, CallbackInfo ci) {
		int i = x - 3;
		int j = y - 3;
		int k = width + 3 + 3;
		int l = height + 3 + 3;
		z = 600;
		renderHorizontalLine(context, i, j - 1, k, z, -267386864);
		renderHorizontalLine(context, i, j + l, k, z, -267386864);
		renderRectangle(context, i, j, k, l, z, -267386864);
		renderVerticalLine(context, i - 1, j, l, z, -267386864);
		renderVerticalLine(context, i + k, j, l, z, -267386864);
		renderBorder(context, i, j + 1, k, l, z, 1347420415, 1344798847);
	}

	private static void renderBorder(DrawContext context, int x, int y, int width, int height, int z, int startColor, int endColor) {
		renderVerticalLine(context, x, y, height - 2, z, startColor, endColor);
		renderVerticalLine(context, x + width - 1, y, height - 2, z, startColor, endColor);
		renderHorizontalLine(context, x, y - 1, width, z, startColor);
		renderHorizontalLine(context, x, y - 1 + height - 1, width, z, endColor);
	}

	private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int color) {
		context.fill(x, y, x + 1, y + height, z, color);
	}

	private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int startColor, int endColor) {
		context.fillGradient(x, y, x + 1, y + height, z, startColor, endColor);
	}

	private static void renderHorizontalLine(DrawContext context, int x, int y, int width, int z, int color) {
		context.fill(x, y, x + width, y + 1, z, color);
	}

	private static void renderRectangle(DrawContext context, int x, int y, int width, int height, int z, int color) {
		context.fill(x, y, x + width, y + height, z, color);
	}
}