package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

	@Accessor("handler")
	<T extends ScreenHandler> T getHandler();

	@Accessor("focusedSlot")
	Slot getFocusedSlot();
}
