package xyz.mashtoolz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import xyz.mashtoolz.config.AutoTool;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.displays.ArenaTimer;
import xyz.mashtoolz.displays.CombatTimer;
import xyz.mashtoolz.displays.DPSMeter;
import xyz.mashtoolz.handlers.KeyHandler;
import xyz.mashtoolz.handlers.RenderHandler;
import xyz.mashtoolz.utils.PlayerUtils;

import java.util.Objects;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class FaceLift implements ClientModInitializer {

	private static FaceLift instance;
	public MinecraftClient client;
	public FaceConfig config;
	public Keybinds keybinds = new Keybinds();

	@Override
	public void onInitializeClient() {

		instance = this;
		client = MinecraftClient.getInstance();

		AutoConfig.register(FaceConfig.class, GsonConfigSerializer::new);
		var holder = AutoConfig.getConfigHolder(FaceConfig.class);
		config = holder.getConfig();
		FaceConfig.holder = holder;

		FaceStatus.registerEffects();

		ScreenEvents.AFTER_INIT.register(RenderHandler::afterInitScreen);
		HudRenderCallback.EVENT.register(RenderHandler::onHudRender);
		WorldRenderEvents.BEFORE_ENTITIES.register(RenderHandler::beforeEntities);

		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			AutoTool.update();
			return false;
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!FaceConfig.General.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display": {
					DPSMeter.textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!FaceConfig.General.onFaceLand || client == null || client.player == null)
				return;

			MountCheck();
			CombatTimer.update();
			DPSMeter.update();
			FaceStatus.update();

			if (Keybinds.instance == null)
				Keybinds.instance = this;

			if (Keybinds.menu.wasPressed())
				KeyHandler.onConfigKey();

			if (Keybinds.mount.wasPressed())
				KeyHandler.onMountKey(PlayerUtils.isMounted());

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

	private void MountCheck() {
		if (config.general.mountThirdPerson) {
			if (PlayerUtils.isMounted() && !FaceConfig.General.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
				client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				FaceConfig.General.isMounted = true;
			} else if (!PlayerUtils.isMounted() && FaceConfig.General.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
				client.options.setPerspective(Perspective.FIRST_PERSON);
				FaceConfig.General.isMounted = false;
			}
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