package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	public void FL_drawSlot_start(DrawContext context, Slot slot, CallbackInfo ci) {
		RenderHandler.drawSlot_start(context, slot, ci);
	}

	@Inject(method = "drawSlot", at = @At("TAIL"))
	public void FL_drawSlot_end(DrawContext context, Slot slot, CallbackInfo ci) {
		RenderHandler.drawSlot_end(context, slot);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void FL_render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		RenderHandler.onHandledScreenRender(context, mouseX, mouseY, delta);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void FL_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (RenderHandler.SEARCHBAR != null && RenderHandler.SEARCHBAR.isFocused() && INSTANCE.CLIENT.options.inventoryKey.matchesKey(keyCode, scanCode))
			cir.setReturnValue(true);
	}
}