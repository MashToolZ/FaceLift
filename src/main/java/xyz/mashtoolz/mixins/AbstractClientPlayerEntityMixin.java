package xyz.mashtoolz.mixins;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @Unique
    private static final FaceLift INSTANCE = FaceLift.getInstance();

    @Inject(method = "getFovMultiplier", at = @At(value = "RETURN"), cancellable = true)
    public void FL_getFovMultiplierReturn(CallbackInfoReturnable<Float> cir) {

        if (!FaceConfig.General.onFaceLand || !INSTANCE.CONFIG.general.instantBowZoom)
            return;

        var player = INSTANCE.CLIENT.player;
        if (player == null || player.getActiveItem() == null)
            return;

        if (player.isUsingItem() && player.getActiveItem().getItem() instanceof BowItem) {
            int useTime = player.getItemUseTime();
            float useProgress = Math.min((float) useTime, 1.0F);
            float zoomMultiplier = 1.0F - useProgress * 0.061F;
            float fovScale = INSTANCE.CLIENT.options.getFovEffectScale().getValue().floatValue();
            float fovMultiplier = MathHelper.lerp(fovScale, 1.0F, zoomMultiplier);
            cir.setReturnValue(fovMultiplier);
        }
    }
}