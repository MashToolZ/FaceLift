package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.option.KeyBinding;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

	@Shadow
	private boolean pressed;

	@Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
	public void FL_isPressed(CallbackInfoReturnable<Boolean> cir) {
		if (RenderHandler.SEARCHBAR != null && RenderHandler.SEARCHBAR.isFocused()) {
			cir.setReturnValue(false);
			return;
		}
		cir.setReturnValue(this.pressed);
	}
}
