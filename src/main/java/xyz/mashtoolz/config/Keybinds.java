package xyz.mashtoolz.config;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.mixins.KeyBindingInterface;

public class Keybinds {

	public static FaceLift instance;

	public static KeyBinding menu = add("facelift.key.menu");
	public static KeyBinding mount = add("facelift.key.mount");

	public static KeyBinding spell1 = add("facelift.key.spell1");
	public static KeyBinding spell2 = add("facelift.key.spell2");
	public static KeyBinding spell3 = add("facelift.key.spell3");
	public static KeyBinding spell4 = add("facelift.key.spell4");

	public static KeyBinding setToolSlot = add("facelift.key.setToolSlot");

	public static KeyBinding add(String key) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "facelift.title"));
	}

	public static boolean isPressed(KeyBinding key) {
		var code = ((KeyBindingInterface) key).getBoundKey().getCode();
		if (code == -1)
			return false;
		return InputUtil.isKeyPressed(instance.client.getWindow().getHandle(), code);
	}
}
