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
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.helpers.ArenaTimer;
import xyz.mashtoolz.helpers.DPSMeter;
import xyz.mashtoolz.helpers.HudRenderer;
import xyz.mashtoolz.helpers.KeyHandler;
import xyz.mashtoolz.mixins.InGameHudInterface;
import xyz.mashtoolz.utils.PlayerUtils;

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

		FaceStatus.registerEffects();

		ScreenEvents.AFTER_INIT.register(HudRenderer::afterInitScreen);
		HudRenderCallback.EVENT.register(HudRenderer::onHudRender);

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

			if (Config.isPressed(Config.setToolKey))
				KeyHandler.onSetToolKey();

			if (Config.arenaTimer.enabled && ArenaTimer.isActive() && (player != null && player.getHealth() <= 0))
				ArenaTimer.end();

			if (client.options.attackKey.isPressed())
				AutoTool();
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

			if (data == null || (data != null && Config.inventory.toolSlots.getTool(data.get("tier").getAsString()) == null)) {
				if (targetTool != null && !inventory.getStack(targetTool.getSlot()).isEmpty())
					this.clickSlot(targetTool.getSlot(), hotbarSlot, SlotActionType.SWAP);
				else if (targetTool != null)
					player.sendMessage(Text.literal("§7[§eFaceLift§7] §cMissing Tool: " + targetTool.getName()));
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