package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.toast.AdvancementToast;

@Mixin(AdvancementToast.class)
public interface IAdvancementToast {

	@Accessor("advancement")
	AdvancementEntry getAdvancement();

}
