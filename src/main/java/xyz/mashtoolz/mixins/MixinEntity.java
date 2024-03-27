package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public abstract class MixinEntity {

	@Unique
	private int glowingColor = -1;

	@Unique
	private int forceGlowing = 1;

	public void setGlowingColor(int glowingColor) {
		this.glowingColor = glowingColor & 0xFFFFFF;
	}

	public void resetColor() {
		glowingColor = -1;
	}

	@Inject(method = "getTeamColorValue()I", cancellable = true, at = @At("HEAD"))
	public void getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
		if (glowingColor != -1) {
			cir.setReturnValue(glowingColor);
			cir.cancel();
		}
	}

	public void setForceGlowing(int glowing) {
		forceGlowing = glowing;
	}

	@Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
	public void isGlowing(CallbackInfoReturnable<Boolean> cir) {
		if (forceGlowing == 0) {
			cir.setReturnValue(false);
		} else if (forceGlowing == 2) {
			cir.setReturnValue(true);
		}
	}
}
