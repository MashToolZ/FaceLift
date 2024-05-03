package xyz.mashtoolz;

import java.sql.Time;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.helpers.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
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
	private boolean rightMouseClickedLastTick = false;
	private Time throwTime;
	private Time reelTime;
	private HashMap<Long, Integer> oneMinFish = new HashMap<>();
	private final Pattern fishingXPRegex = Pattern.compile("Gained Fishing XP! \\(\\+(\\d+)XP\\)");


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

			boolean rightMouseClickedThisTick = client.mouse.wasRightButtonClicked();
			if (rightMouseClickedThisTick && !rightMouseClickedLastTick) {
				Hand hand = client.player.getActiveHand();
				ItemStack heldItem = client.player.getStackInHand(hand);

				if (heldItem.getItem() == Items.FISHING_ROD) {
					if (client.player.fishHook != null && client.player.fishHook.isInOpenWater()) { // Reeling
						// doing nothing here because autoFishing rods wouldn't work otherwise.
					} else { // Throwing
						throwTime = new Time(System.currentTimeMillis());
					}
				}
			}

			if (throwTime != null && reelTime != null) {
				handleReelTime();
			}

			rightMouseClickedLastTick = rightMouseClickedThisTick;
		});

		ClientReceiveMessageEvents.CHAT.register((client, sender, message, messageType, UUID) -> {
			String messageText = message.toString();
			handleChatMessage(messageText);
		});

		HudRenderCallback.EVENT.register((context, delta) -> {
			hudRenderer.onHudRender(context, delta);
		});


	}

	private void handleChatMessage(String message) {
		Matcher matcher = fishingXPRegex.matcher(message);
		if (matcher.find()) {
			reelTime = new Time(System.currentTimeMillis());
			handleReelTime();
		}
	}

	private void handleReelTime() {
		long reelDuration = (reelTime.getTime() - throwTime.getTime()) / (1000 * 60);
		oneMinFish.put(reelDuration, 1);
		System.out.println("reelDuration: " + reelDuration);
		throwTime = null;
		reelTime = null;
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