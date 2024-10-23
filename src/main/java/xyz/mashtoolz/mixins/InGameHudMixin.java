package xyz.mashtoolz.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mashtoolz.handlers.RenderHandler;

import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	private void FL_renderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		RenderHandler.renderHotbar((InGameHud) (Object) this, context, tickCounter, ci);
	}

	@Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
	private void FL_renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		RenderHandler.renderOverlayMessage((InGameHud) (Object) this);
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
	private void appendOverlayDrawing(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local List<Runnable> list, @Local StatusEffectInstance statusEffectInstance, @Local(ordinal = 4) int x, @Local(ordinal = 3) int y) {
		list.add(() -> RenderHandler.drawStatusEffectOverlay(context, statusEffectInstance, x, y));
	}
}