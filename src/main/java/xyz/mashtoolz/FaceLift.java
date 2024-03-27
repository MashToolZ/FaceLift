package xyz.mashtoolz;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.helpers.ArenaTimer;
import xyz.mashtoolz.helpers.DPSMeter;
import xyz.mashtoolz.helpers.HudRenderer;
import xyz.mashtoolz.helpers.KeyHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;

public class FaceLift implements ClientModInitializer {

	private static FaceLift instance;

	public MinecraftClient client;

	public Config config;
	public DPSMeter dpsMeter;
	public KeyHandler keyHandler;
	public ArenaTimer arenaTimer;
	public HudRenderer hudRenderer;

	private HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();
	private HashSet<UUID> textDisplayEntitiesToRemove = new HashSet<>();

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
			switch (entity.getName().getString()) {
				case "Text Display": {
					textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (client == null || client.player == null)
				return;

			FallDamageCheck();
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

			// if (config.potionKey.wasPressed())
			// keyHandler.onPotionKey(1);

			if (config.spell1Key.wasPressed())
				keyHandler.onSpell1Key();

			if (config.spell2Key.wasPressed())
				keyHandler.onSpell2Key();

			if (config.spell3Key.wasPressed())
				keyHandler.onSpell3Key();

			if (config.spell4Key.wasPressed())
				keyHandler.onSpell4Key();

		});

		HudRenderCallback.EVENT.register((context, delta) -> {
			hudRenderer.onHudRender(context, delta);
		});

	}

	public static FaceLift getInstance() {
		return instance;
	}

	public boolean isMounted() {
		Entity ridingEntity = client.player.getVehicle();
		return ridingEntity != null && ridingEntity != client.player;
	}

	private void FallDamageCheck() {
		if (config.hurtTime == 0 && client.player.hurtTime != 0)
			config.hurtTime = client.player.hurtTime;

		if (config.hurtTime == -1 && client.player.hurtTime == 0)
			config.hurtTime = 0;

		if (config.hurtTime > 0) {
			config.hurtTime = -1;

			var recentDamageSource = client.player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().toString().equals("fall"))
				config.lastHurtTime = System.currentTimeMillis();
		}
	}

	private void DPSNumbersCheck() {
		if (textDisplayEntitiesToRemove.size() > 0) {
			for (UUID uuid : textDisplayEntitiesToRemove)
				textDisplayEntities.remove(uuid.toString());
			textDisplayEntitiesToRemove.clear();
		}

		for (TextDisplayEntity textDisplayEntity : textDisplayEntities.values()) {

			if (textDisplayEntity.getData() == null)
				continue;

			textDisplayEntitiesToRemove.add(textDisplayEntity.getUuid());

			var text = textDisplayEntity.getData().text();
			if (text == null)
				continue;

			var damage = instance.dpsMeter.parseDamage(text.getString());
			if (damage <= 0)
				continue;

			instance.dpsMeter.addDamage(damage);
		}
	}
}