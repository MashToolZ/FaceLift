package xyz.mashtoolz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Hand;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.helpers.ArenaTimer;
import xyz.mashtoolz.helpers.DPSMeter;
import xyz.mashtoolz.helpers.HudRenderer;
import xyz.mashtoolz.helpers.KeyHandler;

import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FaceLift implements ClientModInitializer {

	private static FaceLift instance;
	private static final String ip = "beta.face.land";
	private static final String skillCommand = "skills";
	public MinecraftClient client;
	public Config config;
	public DPSMeter dpsMeter;
	public KeyHandler keyHandler;
	public ArenaTimer arenaTimer;
	public HudRenderer hudRenderer;

	private final HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();
	private boolean rightMouseClickedLastTick = false;
	private Time throwTime = null;
	private Time reelTime = null;
	private final HashMap<Long, Integer> oneMinFish = new HashMap<>();
	                     //<TIME, EXP>
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

			if(config.xpCalculator.enabled) {
				boolean rightMouseClickedThisTick = client.mouse.wasRightButtonClicked();
				if (rightMouseClickedThisTick && !rightMouseClickedLastTick) {
					Hand hand = client.player.getActiveHand();
					ItemStack heldItem = client.player.getStackInHand(hand);

					if (heldItem.getItem() == Items.FISHING_ROD) {
						if (client.player.fishHook != null && client.player.fishHook.isInOpenWater()) { // Reeling
							// doing nothing here (for now) because autoFishing rods wouldn't work otherwise.
						} else { // Throwing
							throwTime = new Time(System.currentTimeMillis());
						}
					}
				}
				//player.getInventory();
				if (throwTime != null && reelTime != null) {
					handleReelTime();
				}

				rightMouseClickedLastTick = rightMouseClickedThisTick;
			}
			if(config.arenaTimer.enabled && arenaTimer.isActive()) {
				PlayerEntity player = MinecraftClient.getInstance().player;
				if (player != null && player.getHealth() <= 0) {
					arenaTimer.end();
				}
			}
		});

		ClientReceiveMessageEvents.CHAT.register((client, sender, message, messageType, UUID) -> {
            String messageText = Objects.requireNonNull(message).toString();
			handleChatMessage(messageText);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString();
					System.out.println("Server IP: " + serverAddress);
					if (serverAddress.contains(ip)) {
						System.out.println("Joined faceland <3");
						client.execute(this::skillCheck);
					} else {
						System.out.println("THAT'S NOT FACELAND!!!");
					}
				}
			});
		});


		HudRenderCallback.EVENT.register((context, delta) -> {
			hudRenderer.onHudRender(context, delta);
		});
	}

	private void skillCheck() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null) {
			System.out.println("[skillCheck] Client || Player is null");
			return;
		}
		client.player.networkHandler.sendCommand(skillCommand);
	}

	private void handleChatMessage(String message) {
		if(config.xpCalculator.enabled) {
			Matcher matcher = fishingXPRegex.matcher(message);
			if (matcher.find()) {
				reelTime = new Time(System.currentTimeMillis());
				System.out.println("[reelCheck] check");
				handleReelTime();
			}
			if(afkFishingCheck()){
				throwTime = new Time(System.currentTimeMillis());
				System.out.println("[skillCheck] afkFishing");
			}
		}
	}

	private boolean afkFishingCheck() {
		String[] afkRodNames = {"Automatic Fishing Rod", "Bewitched Fishing Rod", "Lazy Fishing Rod"};
		ItemStack heldItem = Objects.requireNonNull(client.player).getStackInHand(client.player.getActiveHand());

		if (heldItem == null || heldItem.isEmpty())
			return false;

		String itemName = heldItem.getName().getString();

		for (String rodName : afkRodNames) {
			if (itemName.contains(rodName)) {
				return true;
			}
		}

		return false;
	}
	private void handleReelTime() {
		if (throwTime != null && reelTime != null) {
			long reelDuration = (reelTime.getTime() - throwTime.getTime()) / (1000 * 60);
			System.out.println("reelDuration: " + reelDuration);
			oneMinFish.put(reelDuration, 1);
			throwTime = null;
			reelTime = null;
		}
	}

	public static FaceLift getInstance() {
		return instance;
	}

	public boolean isMounted() {
		Entity ridingEntity = Objects.requireNonNull(client.player).getVehicle();
		return ridingEntity != null && ridingEntity != client.player;
	}

	private void FallDamageCheck() {
		if (config.hurtTime == 0 && Objects.requireNonNull(client.player).hurtTime != 0)
			config.hurtTime = client.player.hurtTime;

		if (config.hurtTime == -1 && Objects.requireNonNull(client.player).hurtTime == 0)
			config.hurtTime = 0;

		if (config.hurtTime > 0) {
			config.hurtTime = -1;

			var recentDamageSource = Objects.requireNonNull(client.player).getRecentDamageSource();
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
}