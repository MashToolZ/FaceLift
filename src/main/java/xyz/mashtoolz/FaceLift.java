package xyz.mashtoolz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
import xyz.mashtoolz.mixins.IinGameHud;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class FaceLift implements ClientModInitializer {

	private static FaceLift instance;
	public MinecraftClient client;
	public ClientPlayerEntity player;
	public Config config;
	public DPSMeter dpsMeter;
	public KeyHandler keyHandler;
	public ArenaTimer arenaTimer;
	public HudRenderer hudRenderer;

	private final HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();

	@Override
	public void onInitializeClient() {

		instance = this;
		client = MinecraftClient.getInstance();

		config = new Config();
		dpsMeter = new DPSMeter();
		keyHandler = new KeyHandler();
		arenaTimer = new ArenaTimer();
		hudRenderer = new HudRenderer();

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

			if (client == null || client.player == null || !config.onFaceLand)
				return;

			player = client.player;

			CombatCheck();
			DPSNumbersCheck();

			if (config.general.mountThirdPerson && this.isMounted() != config.mounted) {
				config.mounted = !config.mounted;
				if (config.mounted)
					client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				else
					client.options.setPerspective(Perspective.FIRST_PERSON);
			}

			if (config.configKey.wasPressed())
				keyHandler.onConfigKey();

			if (config.mountKey.wasPressed())
				keyHandler.onMountKey();

			if (config.spell1Key.wasPressed())
				keyHandler.onSpell1Key();

			if (config.spell2Key.wasPressed())
				keyHandler.onSpell2Key();

			if (config.spell3Key.wasPressed())
				keyHandler.onSpell3Key();

			if (config.spell4Key.wasPressed())
				keyHandler.onSpell4Key();

			if (config.arenaTimer.enabled && arenaTimer.isActive() && (player != null && player.getHealth() <= 0))
				arenaTimer.end();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString();
					config.onFaceLand = serverAddress.toLowerCase().contains("face.land");
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			config.onFaceLand = false;
			arenaTimer.end();
		});

		HudRenderCallback.EVENT.register((context, delta) -> {
			hudRenderer.onHudRender(context, delta);
		});
	}

	public static FaceLift getInstance() {
		return instance;
	}

	public boolean isMounted() {
		Entity ridingEntity = player.getVehicle();
		return ridingEntity != null && ridingEntity != player;
	}

	private void CombatCheck() {

		var inGameHud = (IinGameHud) client.inGameHud;
		if (inGameHud == null)
			return;

		var overlayMessage = inGameHud.getOverlayMessage();
		if (overlayMessage != null) {
			for (var unicode : config.combatTimer.unicodes) {
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

			var damage = instance.dpsMeter.parseDamage(text.getString());
			if (damage <= 0)
				continue;

			instance.dpsMeter.addDamage(damage);
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