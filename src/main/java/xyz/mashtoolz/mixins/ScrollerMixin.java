package xyz.mashtoolz.mixins;

import net.minecraft.client.input.Scroller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.mashtoolz.FaceLift;

@Mixin(Scroller.class)
public class ScrollerMixin {

    @Inject(method = "scrollCycling", at = @At("HEAD"), cancellable = true)
    private static void FL_scrollCycling(double amount, int selectedIndex, int total, CallbackInfoReturnable<Integer> cir) {
        FaceLift.handleHotbarScroll(amount, selectedIndex, total, cir);
    }
}
