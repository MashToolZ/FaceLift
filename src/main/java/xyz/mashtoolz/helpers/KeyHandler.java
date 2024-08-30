package xyz.mashtoolz.helpers;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class KeyHandler {

	private static FaceLift instance = FaceLift.getInstance();

	public static void onConfigKey() {
		instance.client.setScreen(AutoConfig.getConfigScreen(FaceConfig.class, instance.client.currentScreen).get());
	}

	public static void onMountKey(boolean isMounted) {
		if (!isMounted) {
			instance.client.player.setSprinting(false);
			while (instance.client.player.isSprinting()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			instance.sendCommand("mount");
		}
	}

	public static void onPotionKey(String category) {
	}

	public static void onSpell1Key() {
		instance.client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(0));
	}

	public static void onSpell2Key() {
		instance.client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(1));
	}

	public static void onSpell3Key() {
		instance.client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(2));
	}

	public static void onSpell4Key() {
		instance.client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(3));
	}

	public static void onSetToolKey() {

		if (instance.client.currentScreen == null || !(instance.client.currentScreen instanceof HandledScreen))
			return;

		var screen = (HandledScreenAccessor) instance.client.currentScreen;
		var handler = screen.getHandler();
		if (handler == null)
			return;

		var focusedSlot = screen.getFocusedSlot();
		if (focusedSlot == null || focusedSlot.id < 9 || handler.slots.size() != 46)
			return;

		var stringData = FaceItem.getItemData(focusedSlot.getStack());
		if (stringData == null)
			return;

		var tier = stringData.get("tier").getAsString();
		for (var entry : instance.config.inventory.autoTool.map().entrySet()) {
			var tool = entry.getValue();
			if (tool.getName().equals(tier) && tool.getSlot() != focusedSlot.id) {
				instance.config.inventory.autoTool.update(tool, focusedSlot.id);
				break;
			}
		}
	}
}
