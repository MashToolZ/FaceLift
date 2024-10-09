package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	private void FL_renderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		RenderHandler.renderHotbar((InGameHud) (Object) this, context, tickCounter, ci);
	}
}