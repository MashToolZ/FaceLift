package xyz.mashtoolz.handlers;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;

public class KeyHandler {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	public static ScreenHandler handler;

	public static void MENU() {
		INSTANCE.CLIENT.setScreen(AutoConfig.getConfigScreen(FaceConfig.class, INSTANCE.CLIENT.currentScreen).get());
	}

	public static void MOUNT(boolean isMounted) {
		if (!isMounted) {
			INSTANCE.CLIENT.player.setSprinting(false);
			INSTANCE.sendCommand("mount");
		}
	}

	public static void ESCAPE() {
		INSTANCE.sendCommand("escape");
	}

	public static void POTION() {
	}

	public static void SPELL_1() {
		INSTANCE.CLIENT.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(0));
	}

	public static void SPELL_2() {
		INSTANCE.CLIENT.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(1));
	}

	public static void SPELL_3() {
		INSTANCE.CLIENT.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(2));
	}

	public static void SPELL_4() {
		INSTANCE.CLIENT.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(3));
	}

	public static void SET_TOOL_SLOT() {

		if (INSTANCE.CLIENT.currentScreen == null || !(INSTANCE.CLIENT.currentScreen instanceof HandledScreen))
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
