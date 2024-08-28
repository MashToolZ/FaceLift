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

	private final HashMap<String, TextDisplayEntity> textDisplayEntities = new HashMap<>();

	@Override
	public void onInitializeClient() {

		instance = this;
		client = MinecraftClient.getInstance();

		FaceConfig.load();

		FaceStatus.registerEffects();

		ScreenEvents.AFTER_INIT.register(HudRenderer::afterInitScreen);
		HudRenderCallback.EVENT.register(HudRenderer::onHudRender);

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (!FaceConfig.onFaceLand)
				return;

			switch (entity.getName().getString()) {
				case "Text Display": {
					textDisplayEntities.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
					break;
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (!FaceConfig.onFaceLand || client == null || client.player == null)
				return;

			CombatCheck();
			DPSNumbersCheck();
			MountCheck();

			if (FaceConfig.configKey.wasPressed())
				KeyHandler.onConfigKey();

			if (FaceConfig.mountKey.wasPressed())
				KeyHandler.onMountKey(this.isMounted());

			if (FaceConfig.spell1Key.wasPressed())
				KeyHandler.onSpell1Key();

			if (FaceConfig.spell2Key.wasPressed())
				KeyHandler.onSpell2Key();

			if (FaceConfig.spell3Key.wasPressed())
				KeyHandler.onSpell3Key();

			if (FaceConfig.spell4Key.wasPressed())
				KeyHandler.onSpell4Key();

			if (FaceConfig.isPressed(FaceConfig.setToolKey))
				KeyHandler.onSetToolKey();

			if (FaceConfig.arenaTimer.enabled && ArenaTimer.isActive() && (client.player != null && client.player.getHealth() <= 0))
				ArenaTimer.end();

			if (client.options.attackKey.isPressed())
				AutoTool();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
				if (connection != null && connection.getAddress() != null) {
					String serverAddress = connection.getAddress().toString().toLowerCase();
					FaceConfig.onFaceLand = serverAddress.startsWith("local") || serverAddress.contains("face.land");
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			FaceConfig.onFaceLand = false;
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
		if (FaceConfig.general.mountThirdPerson) {
			if (this.isMounted() && !FaceConfig.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
				client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
				FaceConfig.isMounted = true;
			} else if (!this.isMounted() && FaceConfig.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
				client.options.setPerspective(Perspective.FIRST_PERSON);
				FaceConfig.isMounted = false;
			}
		}
	}

	private void CombatCheck() {

		var inGameHud = (InGameHudInterface) client.inGameHud;
		if (inGameHud == null)
			return;

		var overlayMessage = inGameHud.getOverlayMessage();
		if (overlayMessage != null) {
			for (var unicode : FaceConfig.combatUnicodes) {
				if (overlayMessage.getString().contains(unicode)) {
					FaceConfig.lastHurtTime = System.currentTimeMillis();
					break;
				}
			}
		}

		if (FaceConfig.hurtTime == 0 && client.player.hurtTime != 0)
			FaceConfig.hurtTime = client.player.hurtTime;

		if (FaceConfig.hurtTime == -1 && client.player.hurtTime == 0)
			FaceConfig.hurtTime = 0;

		if (FaceConfig.hurtTime > 0) {
			FaceConfig.hurtTime = -1;

			var recentDamageSource = client.player.getRecentDamageSource();
			if (recentDamageSource != null && !recentDamageSource.getType().msgId().toString().equals("fall"))
				FaceConfig.lastHurtTime = System.currentTimeMillis();
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

			if (data == null || (data != null && FaceConfig.inventory.toolSlots.getTool(data.get("tier").getAsString()) == null)) {
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