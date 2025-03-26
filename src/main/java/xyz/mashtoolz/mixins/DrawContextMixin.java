package xyz.mashtoolz.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mashtoolz.handlers.RenderHandler;

import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

	@Shadow
	public abstract int getScaledWindowWidth();

	@Shadow
	public abstract int getScaledWindowHeight();

	@Shadow
	@Final
	private MatrixStack matrices;

	@Shadow
	@Final
	private VertexConsumerProvider.Immediate vertexConsumers;

	@Inject(method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"), cancellable = true)
	public void FL_drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y,
			@Nullable String countOverride, CallbackInfo ci) {
		RenderHandler.drawItemInSlot((DrawContext) (Object) this, stack, x, y, countOverride, ci);
	}

	@Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At("HEAD"), cancellable = true)
	private void FL_drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y,
			int seed, int z, CallbackInfo ci) {
		RenderHandler.drawItem((DrawContext) (Object) this, vertexConsumers, entity, world, stack, x, y, seed, z, ci);
	}

	/**
	 * @author Sakurasou
	 * @reason Adjust Tooltip Z-Offset to prevent issues with Skills rendering on
	 *         top of it
	 */
	@Overwrite
	private void drawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y,
			TooltipPositioner positioner, @Nullable Identifier texture) {

		DrawContext drawContext = (DrawContext) (Object) this;

		if (!components.isEmpty()) {
			int i = 0;
			int j = components.size() == 1 ? -2 : 0;

			for (TooltipComponent tooltipComponent : components) {
				int k = tooltipComponent.getWidth(textRenderer);
				if (k > i) {
					i = k;
				}

				j += tooltipComponent.getHeight(textRenderer);
			}

			int l = i;
			int m = j;
			Vector2ic vector2ic = positioner.getPosition(this.getScaledWindowWidth(), this.getScaledWindowHeight(), x,
					y, i, j);
			int n = vector2ic.x();
			int o = vector2ic.y();
			this.matrices.push();
			TooltipBackgroundRenderer.render(drawContext, n, o, i, j, 400, texture);
			this.matrices.translate(0.0F, 0.0F, 600.0F);
			int q = o;

			for (int r = 0; r < components.size(); r++) {
				TooltipComponent tooltipComponent2 = components.get(r);
				tooltipComponent2.drawText(textRenderer, n, q, this.matrices.peek().getPositionMatrix(),
						this.vertexConsumers);
				q += tooltipComponent2.getHeight(textRenderer) + (r == 0 ? 2 : 0);
			}

			q = o;

			for (int r = 0; r < components.size(); r++) {
				TooltipComponent tooltipComponent2 = components.get(r);
				tooltipComponent2.drawItems(textRenderer, n, q, l, m, drawContext);
				q += tooltipComponent2.getHeight(textRenderer) + (r == 0 ? 2 : 0);
			}

			this.matrices.pop();
		}
	}
}
