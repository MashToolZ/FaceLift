package xyz.mashtoolz.mixins;

import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mashtoolz.FaceLift;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    
    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    public void FL_scrollInHotbar(double scrollAmount, CallbackInfo ci) {
        FaceLift.handleHotbarScroll((PlayerInventory) (Object) this, scrollAmount, ci);
    }
}
