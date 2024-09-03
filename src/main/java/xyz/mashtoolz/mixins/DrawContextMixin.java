package xyz.mashtoolz.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

	@Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"), cancellable = true)
	public void FL_drawItemInSlotHead(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {
		RenderHandler.preDrawHotbarItemSlot((DrawContext) (Object) this, stack, x, y, ci);
	}
}
