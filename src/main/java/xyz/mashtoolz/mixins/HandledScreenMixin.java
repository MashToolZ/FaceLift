package xyz.mashtoolz.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	public void FL_drawSlot_start(DrawContext context, Slot slot, CallbackInfo ci) {
		RenderHandler.drawSlot_start(context, slot, ci);
	}

	@Inject(method = "drawSlot", at = @At("TAIL"))
	public void FL_drawSlot_end(DrawContext context, Slot slot, CallbackInfo ci) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void FL_render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		RenderHandler.onHandledScreenRender(context, mouseX, mouseY);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void FL_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		RenderHandler.onHandledScreenKeyPressed(keyCode, scanCode, modifiers, cir);
	}
}