package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

	@Shadow
	public int selectedSlot;

	@Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
	public void FL_scrollInHotbar(double scrollAmount, CallbackInfo ci) {
		int i = (int) Math.signum(scrollAmount);
		this.selectedSlot -= i;

		while (this.selectedSlot <= 3)
			this.selectedSlot += 5;

		while (this.selectedSlot >= 9)
			this.selectedSlot -= 5;

		ci.cancel();
	}
}
