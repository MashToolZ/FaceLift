package xyz.mashtoolz.handlers;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class KeyHandler {

	private static final FaceLift INSTANCE = FaceLift.getInstance();

	public static void MENU() {
		INSTANCE.CLIENT.setScreen(AutoConfig.getConfigScreen(FaceConfig.class, INSTANCE.CLIENT.currentScreen).get());
	}

	public static void MOUNT(boolean isMounted) {
		if (!isMounted) {
            assert INSTANCE.CLIENT.player != null;
            INSTANCE.CLIENT.player.setSprinting(false);
			INSTANCE.sendCommand("mount");
		}
	}

	public static void ESCAPE() {
		INSTANCE.sendCommand("escape");
	}

	public static void SET_TOOL_SLOT() {

		if (!(INSTANCE.CLIENT.currentScreen instanceof HandledScreen))
			return;

		var screen = (HandledScreenAccessor) INSTANCE.CLIENT.currentScreen;
		var handler = screen.getHandler();
		if (handler == null)
			return;

		var focusedSlot = screen.getFocusedSlot();
		if (focusedSlot == null || focusedSlot.id < 9 || focusedSlot.id >= 40 || handler.slots.size() != 46)
			return;

		var item = FaceItem.from(focusedSlot.getStack());
		var tooltip = item.getTooltip();
		for (var tool : FaceTool.values()) {
			if (tooltip.contains(tool.getFaceToolType().getName()) && tool.getSlotIndex() != focusedSlot.id) {
				tool.setSlotIndex(focusedSlot.id);
				break;
			}
		}

	}
}
