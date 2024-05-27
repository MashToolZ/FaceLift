package xyz.mashtoolz.helpers;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

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
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(0));
	}

	public void onSpell2Key() {
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(1));
	}

	public void onSpell3Key() {
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(2));
	}

	public void onSpell4Key() {
		client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(3));
	}
}
