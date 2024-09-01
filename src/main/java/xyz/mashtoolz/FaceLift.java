package xyz.mashtoolz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import xyz.mashtoolz.config.AutoTool;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.helpers.ArenaTimer;
import xyz.mashtoolz.helpers.DPSMeter;
import xyz.mashtoolz.helpers.HudRenderer;
import xyz.mashtoolz.helpers.KeyHandler;
import xyz.mashtoolz.mixins.InGameHudInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class FaceLift implements ClientModInitializer {

	private static FaceLift instance;
	public MinecraftClient client;
	public FaceConfig config;
	public Keybinds keybinds = new Keybinds();

	private final HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();
	public ArrayList<String> combatUnicodes = new ArrayList<>(Arrays.asList("丞", "丟"));

	@Override
	public void onInitializeClient() {

		instance = this;
		client = MinecraftClient.getInstance();

		AutoConfig.register(FaceConfig.class, GsonConfigSerializer::new);
		var holder = AutoConfig.getConfigHolder(FaceConfig.class);
		config = holder.getConfig();
		FaceConfig.holder = holder;

		FaceStatus.registerEffects();

		ScreenEvents.AFTER_INIT.register(HudRenderer::afterInitScreen);
		HudRenderCallback.EVENT.register(HudRenderer::onHudRender);

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!FaceConfig.General.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display": {
					textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!FaceConfig.General.onFaceLand || client == null || client.player == null)
				return;

			CombatCheck();
			DPSNumbersCheck();
			MountCheck();
			FaceStatus.update();

			if (Keybinds.instance == null)
				Keybinds.instance = this;

			if (Keybinds.menu.wasPressed())
				KeyHandler.onConfigKey();

			if (Keybinds.mount.wasPressed())
				KeyHandler.onMountKey(this.isMounted());

			if (Keybinds.spell1.wasPressed())
				KeyHandler.onSpell1Key();

			if (Keybinds.spell2.wasPressed())
				KeyHandler.onSpell2Key();

			if (Keybinds.spell3.wasPressed())
				KeyHandler.onSpell3Key();

			if (Keybinds.spell4.wasPressed())
				KeyHandler.onSpell4Key();

			if (Keybinds.isPressed(Keybinds.setToolSlot))
				KeyHandler.onSetToolKey();

			if (config.combat.arenaTimer.enabled && ArenaTimer.isActive() && (client.player != null && client.player.getHealth() <= 0))
				ArenaTimer.end();

			if (client.options.attackKey.isPressed())
				AutoTool.update();

			// automatically let go off rightclick for pistols
			// if (client.options.useKey.isPressed()) {
			// var remaining = ((LivingEntity) client.player).getItemUseTimeLeft();
			// var stack = client.player.getMainHandStack();
			// var bow = (BowItem) stack.getItem();
			// var progress = BowItem.getPullProgress(bow.getMaxUseTime(stack) - remaining);
			// if (progress >= 0.08)
			// client.options.useKey.setPressed(false);
			// }
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString().toLowerCase();
					FaceConfig.General.onFaceLand = serverAddress.startsWith("local") || serverAddress.contains("face.land");
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			FaceConfig.General.onFaceLand = false;
			ArenaTimer.end();
		});
	}

	public static FaceLift getInstance() {
		return instance;
	}

	public boolean isMounted() {
		Entity ridingEntity = client.player.getVehicle();
		return ridingEntity != null && ridingEntity != client.player;
	}

	private void MountCheck() {
		if (config.general.mountThirdPerson) {
			if (this.isMounted() && !FaceConfig.General.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
				client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				FaceConfig.General.isMounted = true;
			} else if (!this.isMounted() && FaceConfig.General.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
				client.options.setPerspective(Perspective.FIRST_PERSON);
				FaceConfig.General.isMounted = false;
			}
		}
	}

	private void CombatCheck() {

		var inGameHud = (InGameHudInterface) client.inGameHud;
		if (inGameHud == null)
			return;

		var overlayMessage = inGameHud.getOverlayMessage();
		if (overlayMessage != null) {
			for (var unicode : combatUnicodes) {
				if (overlayMessage.getString().contains(unicode)) {
					FaceConfig.General.lastHurtTime = System.currentTimeMillis();
					break;
				}
			}
		}

		if (FaceConfig.General.hurtTime == 0 && client.player.hurtTime != 0)
			FaceConfig.General.hurtTime = client.player.hurtTime;

		if (FaceConfig.General.hurtTime == -1 && client.player.hurtTime == 0)
			FaceConfig.General.hurtTime = 0;

		if (FaceConfig.General.hurtTime > 0) {
			FaceConfig.General.hurtTime = -1;

			var recentDamageSource = client.player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().toString().equals("fall"))
				FaceConfig.General.lastHurtTime = System.currentTimeMillis();
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

	public static void info(boolean console, String message) {
		if (console)
			System.out.println("[FaceLift] " + message);
		else
			instance.client.player.sendMessage(Text.literal("§7[§cFaceLift§7] " + message));
	}
}