package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	public void FL_drawSlotHead(DrawContext context, Slot slot, CallbackInfo ci) {
		RenderHandler.preDrawItemSlot(context, slot, ci);
	}

	@Inject(method = "drawSlot", at = @At("TAIL"))
	public void FL_drawSlotTail(DrawContext context, Slot slot, CallbackInfo ci) {
		RenderHandler.postDrawItemSlot(context, slot);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void FL_renderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		RenderHandler.onHandledScreenRenderTail(context, mouseX, mouseY, delta);
	}
}