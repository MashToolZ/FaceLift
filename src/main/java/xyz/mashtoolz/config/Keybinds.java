package xyz.mashtoolz.config;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.mixins.KeyBindingAccessor;

public class Keybinds {

	public static FaceLift INSTANCE;

	public static KeyBinding MENU = add("facelift.key.menu", GLFW.GLFW_KEY_UNKNOWN);

	public static KeyBinding MOUNT = add("facelift.key.mount", GLFW.GLFW_KEY_V);
	public static KeyBinding ESCAPE = add("facelift.key.escape", GLFW.GLFW_KEY_O);

	public static KeyBinding SET_TOOL_SLOT = add("facelift.key.set_tool_slot", GLFW.GLFW_KEY_LEFT_BRACKET);
	public static KeyBinding COMPARE_TOOLTIP = add("facelift.key.compare_tooltip", GLFW.GLFW_KEY_LEFT_ALT);

	public static KeyBinding add(String key, int defaultKey) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, defaultKey, "facelift.title"));
	}

	public static boolean isPressed(KeyBinding key) {
		var code = ((KeyBindingAccessor) key).getBoundKey().getCode();
		if (code == -1)
			return false;
		return InputUtil.isKeyPressed(INSTANCE.CLIENT.getWindow().getHandle(), code);
	}
}
