package xyz.mashtoolz.helpers;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class KeyHandler {

	private static final FaceLift instance = FaceLift.getInstance();

	private static MinecraftClient client = instance.client;
	private static Config config = instance.config;

	public static void onConfigKey() {
		client.setScreen(config.getScreen());
	}

	public static void onMountKey(boolean isMounted) {
		if (!isMounted) {
			client.player.setSprinting(false);
			while (client.player.isSprinting()) {
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
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(0));
	}

	public static void onSpell2Key() {
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(1));
	}

	public static void onSpell3Key() {
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(2));
	}

	public static void onSpell4Key() {
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(3));
	}
}
