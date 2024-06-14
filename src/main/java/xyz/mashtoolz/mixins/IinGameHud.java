package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;

@Mixin(InGameHud.class)
public interface IinGameHud {

	@Accessor("title")
	Text getTitle();

	@Accessor("subtitle")
	Text getSubtitle();

	@Accessor("overlayMessage")
	Text getOverlayMessage();
}
