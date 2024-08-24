package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import xyz.mashtoolz.helpers.HudRenderer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow
	public abstract Item getItem();

	@Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
	public void hasGlint(CallbackInfoReturnable<Boolean> cir) {
		if (HudRenderer.ABILITY_ITEMS.contains(this.getItem())) {
			cir.setReturnValue(false);
			return;
		}
	}

	@Inject(method = "isItemBarVisible", at = @At("HEAD"), cancellable = true)
	public void isItemBarVisible(CallbackInfoReturnable<Boolean> cir) {
		if (HudRenderer.ABILITY_ITEMS.contains(this.getItem())) {
			cir.setReturnValue(false);
			return;
		}
	}
}
