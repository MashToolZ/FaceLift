package xyz.mashtoolz.mixins;

import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.mashtoolz.custom.FaceStatus;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectsDisplayMixin {

    @Inject(method = "getStatusEffectDescription", at = @At("HEAD"), cancellable = true)
    private void getStatusEffectDescription(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir) {
        FaceStatus.getDescription(statusEffect, cir);
    }
}
