package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import xyz.mashtoolz.FaceLift;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

	private FaceLift instance;

	@Inject(method = "drawSlot", at = @At("HEAD"))
	public void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
		if (instance == null)
			instance = FaceLift.getInstance();

		instance.hudRenderer.drawItemSlot(context, slot);
	}
}