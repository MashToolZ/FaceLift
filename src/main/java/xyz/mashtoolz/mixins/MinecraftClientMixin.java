package xyz.mashtoolz.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import xyz.mashtoolz.FaceLift;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Redirect(method = "handleInputEvents", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = Opcodes.PUTFIELD))
	private void FL_handleInputEvents(PlayerInventory inventory, int index) {
		FaceLift.handleHotbarChange(inventory, index);
	}
}