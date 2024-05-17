package xyz.mashtoolz.helpers;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import net.minecraft.client.MinecraftClient;

public class KeyHandler {

	private FaceLift instance = FaceLift.getInstance();

	private MinecraftClient client;
	private Config config;

	public KeyHandler() {
		this.client = instance.client;
		this.config = instance.config;
	}

	public void onConfigKey() {
		client.setScreen(config.getScreen());
	}

	public void onMountKey() {
		if (!config.mounted) {

			client.player.setSprinting(false);

			while (client.player.isSprinting()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			client.player.networkHandler.sendChatCommand("mount");
		}
	}

	public void onPotionKey(String category) {
	}

	public void onSpell1Key() {
		client.player.getInventory().selectedSlot = 0;
	}

	public void onSpell2Key() {
		client.player.getInventory().selectedSlot = 1;
	}

	public void onSpell3Key() {
		client.player.getInventory().selectedSlot = 2;
	}

	public void onSpell4Key() {
		client.player.getInventory().selectedSlot = 3;
	}
}
