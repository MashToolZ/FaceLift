package xyz.mashtoolz.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mashtoolz.handlers.RenderHandler;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow
    public abstract int getScaledWindowWidth();

    @Shadow
    public abstract int getScaledWindowHeight();

    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    @Final
    private VertexConsumerProvider.Immediate vertexConsumers;

    @Inject(method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void FL_drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {
        RenderHandler.drawItemInSlot((DrawContext) (Object) this, stack, x, y, countOverride, ci);
    }

    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At("HEAD"), cancellable = true)
    private void FL_drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        RenderHandler.drawItem((DrawContext) (Object) this, vertexConsumers, entity, world, stack, x, y, seed, z, ci);
    }

    @ModifyArg(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), index = 2)
    public float modifiedTooltipZ(float originalZ) {
        return 600.0F;
    }

    @ModifyArg(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;render(Lnet/minecraft/client/gui/DrawContext;IIIIILnet/minecraft/util/Identifier;)V"), index = 5)
    public int modifiedTooltipBackgroundZ(int originalZ) {
        return 600;
    }
}
