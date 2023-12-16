package com.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancement.Advancement;
import net.minecraft.client.toast.AdvancementToast;

@Mixin(AdvancementToast.class)
public interface IAdvancementToast {

	@Accessor("advancement")
	Advancement getAdvancement();

}
