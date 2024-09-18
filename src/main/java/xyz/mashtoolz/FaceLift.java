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
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.custom.FaceTool;
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

	private static FaceLift INSTANCE;

	public MinecraftClient CLIENT;
	public FaceConfig CONFIG;
	public Keybinds KEYBINDS = new Keybinds();

	@Override
	public void onInitializeClient() {

		INSTANCE = this;
		CLIENT = MinecraftClient.getInstance();

		AutoConfig.register(FaceConfig.class, GsonConfigSerializer::new);
		var holder = AutoConfig.getConfigHolder(FaceConfig.class);
		CONFIG = holder.getConfig();
		FaceConfig.holder = holder;

		FaceStatus.registerEffects();

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

		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (CONFIG.inventory.autoTool.enabled)
				FaceTool.update();
			return false;
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!FaceConfig.General.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display" -> DPSMeter.TEXT_DISPLAYS.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!FaceConfig.General.onFaceLand || client == null || client.player == null)
				return;

			PlayerUtils.update();
			CombatTimer.update();
			DPSMeter.update();
			FaceStatus.update();

			if (Keybinds.INSTANCE == null)
				Keybinds.INSTANCE = this;

			handleKeybinds(client);

			if (CONFIG.combat.arenaTimer.enabled && ArenaTimer.isActive() && (client.player != null && client.player.getHealth() <= 0))
				ArenaTimer.end();
		});

		ScreenEvents.AFTER_INIT.register(RenderHandler::afterInitScreen);
		HudRenderCallback.EVENT.register(RenderHandler::onHudRender);
		WorldRenderEvents.BEFORE_ENTITIES.register(RenderHandler::beforeEntities);
	}

	public static FaceLift getInstance() {
		return INSTANCE;
	}

	public void sendCommand(String command) {
		CLIENT.player.networkHandler.sendChatCommand(command);
	}

	public static void info(boolean console, String message) {
		if (console)
			System.out.println("[FaceLift] " + message);
		else
			INSTANCE.CLIENT.player.sendMessage(Text.literal("§7[§cFaceLift§7] " + message));
	}

	private void handleKeybinds(MinecraftClient client) {
		if (Keybinds.MENU.wasPressed())
			KeyHandler.MENU();

		if (Keybinds.MOUNT.wasPressed())
			KeyHandler.MOUNT(PlayerUtils.isMounted());

		if (Keybinds.ESCAPE.wasPressed())
			KeyHandler.ESCAPE();

		if (Keybinds.POTION.wasPressed())
			KeyHandler.POTION();

		if (Keybinds.SPELL_1.wasPressed())
			KeyHandler.SPELL_1();

		if (Keybinds.SPELL_2.wasPressed())
			KeyHandler.SPELL_2();

		if (Keybinds.SPELL_3.wasPressed())
			KeyHandler.SPELL_3();

		if (Keybinds.SPELL_4.wasPressed())
			KeyHandler.SPELL_4();

		if (Keybinds.isPressed(Keybinds.SET_TOOL_SLOT))
			KeyHandler.SET_TOOL_SLOT();
	}
}