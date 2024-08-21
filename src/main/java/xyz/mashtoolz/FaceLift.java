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
	public Config config;

	private final HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();

	@Override
	public void onInitializeClient() {

		instance = this;
		client = MinecraftClient.getInstance();
		config = new Config();

		ScreenEvents.AFTER_INIT.register(HudRenderer::afterInitScreen);

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!config.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display": {
					textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!config.onFaceLand || client == null || client.player == null)
				return;

			player = client.player;

			CombatCheck();
			DPSNumbersCheck();
			MountCheck();

			if (config.configKey.wasPressed())
				KeyHandler.onConfigKey();

			if (config.mountKey.wasPressed())
				KeyHandler.onMountKey(this.isMounted());

			if (config.spell1Key.wasPressed())
				KeyHandler.onSpell1Key();

			if (config.spell2Key.wasPressed())
				KeyHandler.onSpell2Key();

			if (config.spell3Key.wasPressed())
				KeyHandler.onSpell3Key();

			if (config.spell4Key.wasPressed())
				KeyHandler.onSpell4Key();

			if (config.arenaTimer.enabled && ArenaTimer.isActive() && (player != null && player.getHealth() <= 0))
				ArenaTimer.end();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString().toLowerCase();
					config.onFaceLand = serverAddress.startsWith("local") || serverAddress.contains("face.land");
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			config.onFaceLand = false;
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
		if (config.general.mountThirdPerson) {
			if (this.isMounted() && !config.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
				client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				config.isMounted = true;
			} else if (!this.isMounted() && config.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
				client.options.setPerspective(Perspective.FIRST_PERSON);
				config.isMounted = false;
			}
		}
	}

	private void CombatCheck() {

		var inGameHud = (InGameHudMixin) client.inGameHud;
		if (inGameHud == null)
			return;

		var overlayMessage = inGameHud.getOverlayMessage();
		if (overlayMessage != null) {
			for (var unicode : config.combatUnicodes) {
				if (overlayMessage.getString().contains(unicode)) {
					config.lastHurtTime = System.currentTimeMillis();
					break;
				}
			}
		}

		if (config.hurtTime == 0 && player.hurtTime != 0)
			config.hurtTime = player.hurtTime;

		if (config.hurtTime == -1 && player.hurtTime == 0)
			config.hurtTime = 0;

		if (config.hurtTime > 0) {
			config.hurtTime = -1;

			var recentDamageSource = player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().toString().equals("fall"))
				config.lastHurtTime = System.currentTimeMillis();
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

	public String escapeStringToUnicode(String input) {
		StringBuilder builder = new StringBuilder();
		for (char ch : input.toCharArray()) {
			if (ch < 128) {
				builder.append(ch);
			} else {
				builder.append(String.format("\\u%04x", (int) ch));
			}
		}
		return builder.toString();
	}
}