package xyz.mashtoolz.config;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.mixins.KeyBindingAccessor;

public class Keybinds {

	public static FaceLift INSTANCE;

	public static KeyBinding MENU = add("facelift.key.menu");
	public static KeyBinding MOUNT = add("facelift.key.mount");

	public static KeyBinding SPELL_1 = add("facelift.key.spell_1");
	public static KeyBinding SPELL_2 = add("facelift.key.spell_2");
	public static KeyBinding SPELL_3 = add("facelift.key.spell_3");
	public static KeyBinding SPELL_4 = add("facelift.key.spell_4");

	public static KeyBinding POTION = add("facelift.key.potion");

	public static KeyBinding SET_TOOL_SLOT = add("facelift.key.set_tool_slot");

	public static KeyBinding COMPARE_TOOLTIP = add("facelift.key.compare_tooltip");

	public static KeyBinding add(String key) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "facelift.title"));
	}

	public static boolean isPressed(KeyBinding key) {
		var code = ((KeyBindingAccessor) key).getBoundKey().getCode();
		if (code == -1)
			return false;
		return InputUtil.isKeyPressed(INSTANCE.CLIENT.getWindow().getHandle(), code);
	}
}
