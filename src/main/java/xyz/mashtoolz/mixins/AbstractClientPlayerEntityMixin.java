package xyz.mashtoolz.mixins;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import xyz.mashtoolz.handlers.RenderHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

	@Inject(method = "getFovMultiplier", at = @At(value = "RETURN"), cancellable = true)
	public void FL_getFovMultiplierReturn(CallbackInfoReturnable<Float> cir) {
		RenderHandler.getFovMultiplierReturn(cir);
	}
}