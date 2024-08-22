package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import xyz.mashtoolz.helpers.HudRenderer;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	public void drawSlotHead(DrawContext context, Slot slot, CallbackInfo ci) {
		HudRenderer.preDrawItemSlot(context, slot, ci);
	}

	@Inject(method = "drawSlot", at = @At("TAIL"))
	public void drawSlotTail(DrawContext context, Slot slot, CallbackInfo ci) {
		HudRenderer.postDrawItemSlot(context, slot);
	}
}