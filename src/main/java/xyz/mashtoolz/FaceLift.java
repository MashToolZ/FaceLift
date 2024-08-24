package xyz.mashtoolz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.network.ClientConnection;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.helpers.ArenaTimer;
import xyz.mashtoolz.helpers.DPSMeter;
import xyz.mashtoolz.helpers.HudRenderer;
import xyz.mashtoolz.helpers.KeyHandler;
import xyz.mashtoolz.mixins.InGameHudInterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class FaceLift implements ClientModInitializer {

	private static FaceLift instance;

	public MinecraftClient client;
	public ClientPlayerEntity player;

	private final HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();

	@Override
	public void onInitializeClient() {

		instance = this;
		client = MinecraftClient.getInstance();

		Config.load();

		ScreenEvents.AFTER_INIT.register(HudRenderer::afterInitScreen);

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!Config.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display": {
					textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!Config.onFaceLand || client == null || client.player == null)
				return;

			player = client.player;

			CombatCheck();
			DPSNumbersCheck();
			MountCheck();

			if (Config.configKey.wasPressed())
				KeyHandler.onConfigKey();

			if (Config.mountKey.wasPressed())
				KeyHandler.onMountKey(this.isMounted());

			if (Config.spell1Key.wasPressed())
				KeyHandler.onSpell1Key();

			if (Config.spell2Key.wasPressed())
				KeyHandler.onSpell2Key();

			if (Config.spell3Key.wasPressed())
				KeyHandler.onSpell3Key();

			if (Config.spell4Key.wasPressed())
				KeyHandler.onSpell4Key();

			if (Config.arenaTimer.enabled && ArenaTimer.isActive() && (player != null && player.getHealth() <= 0))
				ArenaTimer.end();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString().toLowerCase();
					Config.onFaceLand = serverAddress.startsWith("local") || serverAddress.contains("face.land");
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Config.onFaceLand = false;
			ArenaTimer.end();
		});

		HudRenderCallback.EVENT.register(HudRenderer::onHudRender);
	}

	public static FaceLift getInstance() {
		return instance;
	}

	public boolean isMounted() {
		Entity ridingEntity = player.getVehicle();
		return ridingEntity != null && ridingEntity != player;
	}

	private void MountCheck() {
		if (Config.general.mountThirdPerson) {
			if (this.isMounted() && !Config.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
				client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				Config.isMounted = true;
			} else if (!this.isMounted() && Config.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
				client.options.setPerspective(Perspective.FIRST_PERSON);
				Config.isMounted = false;
			}
		}
	}

	private void CombatCheck() {

		var inGameHud = (InGameHudInterface) client.inGameHud;
		if (inGameHud == null)
			return;

		var overlayMessage = inGameHud.getOverlayMessage();
		if (overlayMessage != null) {
			for (var unicode : Config.combatUnicodes) {
				if (overlayMessage.getString().contains(unicode)) {
					Config.lastHurtTime = System.currentTimeMillis();
					break;
				}
			}
		}

		if (Config.hurtTime == 0 && player.hurtTime != 0)
			Config.hurtTime = player.hurtTime;

		if (Config.hurtTime == -1 && player.hurtTime == 0)
			Config.hurtTime = 0;

		if (Config.hurtTime > 0) {
			Config.hurtTime = -1;

			var recentDamageSource = player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().toString().equals("fall"))
				Config.lastHurtTime = System.currentTimeMillis();
		}
	}

	private void DPSNumbersCheck() {
		for (Iterator<TextDisplayEntity> iterator = textDisplayEntities.values().iterator(); iterator.hasNext();) {
			TextDisplayEntity textDisplayEntity = iterator.next();

			if (textDisplayEntity.getData() == null)
				continue;

			iterator.remove();

			var text = textDisplayEntity.getData().text();
			if (text == null)
				continue;

			var damage = DPSMeter.parseDamage(text.getString());
			if (damage <= 0)
				continue;

			DPSMeter.addDamage(damage);
		}
	}

	public void sendCommand(String command) {
		client.player.networkHandler.sendChatCommand(command);
	}
}