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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.world.RaycastContext;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.helpers.ArenaTimer;
import xyz.mashtoolz.helpers.DPSMeter;
import xyz.mashtoolz.helpers.HudRenderer;
import xyz.mashtoolz.helpers.KeyHandler;
import xyz.mashtoolz.mixins.InGameHudInterface;
import xyz.mashtoolz.utils.PlayerUtils;

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
		config = AutoConfig.getConfigHolder(FaceConfig.class).getConfig();

		FaceStatus.registerEffects();

		ScreenEvents.AFTER_INIT.register(HudRenderer::afterInitScreen);
		HudRenderCallback.EVENT.register(HudRenderer::onHudRender);

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!config.general.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display": {
					textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!config.general.onFaceLand || client == null || client.player == null)
				return;

			CombatCheck();
			DPSNumbersCheck();
			MountCheck();

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
				AutoTool();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString().toLowerCase();
					config.general.onFaceLand = serverAddress.startsWith("local") || serverAddress.contains("face.land");
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			config.general.onFaceLand = false;
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
			if (this.isMounted() && !config.general.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
				client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				config.general.isMounted = true;
			} else if (!this.isMounted() && config.general.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
				client.options.setPerspective(Perspective.FIRST_PERSON);
				config.general.isMounted = false;
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
					config.general.lastHurtTime = System.currentTimeMillis();
					break;
				}
			}
		}

		if (config.general.hurtTime == 0 && client.player.hurtTime != 0)
			config.general.hurtTime = client.player.hurtTime;

		if (config.general.hurtTime == -1 && client.player.hurtTime == 0)
			config.general.hurtTime = 0;

		if (config.general.hurtTime > 0) {
			config.general.hurtTime = -1;

			var recentDamageSource = client.player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().toString().equals("fall"))
				config.general.lastHurtTime = System.currentTimeMillis();
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

	private void AutoTool() {

		try {

			var eyePos = client.player.getEyePos();
			var reach = ClientPlayerEntity.getReachDistance(false);
			var rayEnd = eyePos.add(client.player.getRotationVector().multiply(reach));
			var blockHitResult = client.world.raycast(new RaycastContext(eyePos, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
			var targetTool = PlayerUtils.getTargetTool(blockHitResult);
			if (targetTool != null && targetTool.getName().equals("bedrock"))
				return;

			var inventory = client.player.getInventory();
			var hotbarSlot = inventory.selectedSlot;
			var stack = client.player.getMainHandStack();
			var data = FaceItem.getItemData(stack);
			var currentTool = PlayerUtils.getCurrentTool(data);

			if (data == null || (data != null && config.inventory.autoTool.get(data.get("tier").getAsString()) == null)) {
				if (targetTool != null && !inventory.getStack(targetTool.getSlot()).isEmpty())
					this.clickSlot(targetTool.getSlot(), hotbarSlot, SlotActionType.SWAP);
				else if (targetTool != null)
					client.player.sendMessage(Text.literal("§7[§eFaceLift§7] §cMissing Tool: " + targetTool.getName()));
				return;
			}

			var isSlotEmpty = inventory.getStack(currentTool.getSlot()).isEmpty();
			if (targetTool == null) {
				if (isSlotEmpty) {
					this.clickSlot(36 + hotbarSlot, 0, SlotActionType.PICKUP);
					this.clickSlot(currentTool.getSlot(), 0, SlotActionType.PICKUP);
					return;
				}
				this.clickSlot(currentTool.getSlot(), hotbarSlot, SlotActionType.SWAP);
				return;
			}

			if (currentTool.getName().equals(targetTool.getName()))
				return;

			if (isSlotEmpty) {
				this.clickSlot(36 + hotbarSlot, 0, SlotActionType.PICKUP);
				this.clickSlot(currentTool.getSlot(), 0, SlotActionType.PICKUP);
				this.clickSlot(targetTool.getSlot(), hotbarSlot, SlotActionType.SWAP);
			} else {
				this.clickSlot(currentTool.getSlot(), hotbarSlot, SlotActionType.SWAP);
				this.clickSlot(targetTool.getSlot(), hotbarSlot, SlotActionType.SWAP);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void clickSlot(int slotId, int button, SlotActionType actionType) {
		var syncId = client.player.currentScreenHandler.syncId;
		client.interactionManager.clickSlot(syncId, slotId, button, actionType, client.player);
	}

	public void sendCommand(String command) {
		client.player.networkHandler.sendChatCommand(command);
	}

	// public void info(String message) {
	// player.sendMessage(Text.literal("§7[§cFaceLift§7]"));
	// }
}