package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import xyz.mashtoolz.handlers.ToastHandler;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

	@Inject(method = "add", at = @At("HEAD"))
	public void FL_add(Toast toast, CallbackInfo ci) {
		ToastHandler.add(toast);
	}
}