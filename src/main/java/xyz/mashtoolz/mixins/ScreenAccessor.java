package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public interface ScreenAccessor {

	@Invoker("addDrawableChild")
	<T extends Element & Drawable> T invokeAddDrawableChild(T drawableElement);
}
