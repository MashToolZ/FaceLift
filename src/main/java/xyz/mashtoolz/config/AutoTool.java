package xyz.mashtoolz.config;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.world.RaycastContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.custom.FaceToolType;
import xyz.mashtoolz.utils.PlayerUtils;

public class AutoTool {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	public int PICKAXE = 15;
	public int WOODCUTTINGAXE = 16;
	public int HOE = 17;

	public FaceTool get(FaceToolType type) {
		return FaceTool.getByType(type);
	}

	public static void update() {

		var client = INSTANCE.CLIENT;
		var player = client.player;
		var inventory = player.getInventory();
		var hotbarSlot = inventory.selectedSlot;
		var mainHandStack = player.getMainHandStack();

		var eyePos = player.getEyePos();
		var reach = ClientPlayerEntity.getReachDistance(false);
		var rayEnd = eyePos.add(player.getRotationVector().multiply(reach));
		var blockHitResult = client.world.raycast(new RaycastContext(eyePos, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

		var faceItem = new FaceItem(mainHandStack);
		var tooltip = faceItem.isInvalid() ? null : faceItem.getTooltip();
		var currentTool = getCurrentToolFromTooltip(tooltip);
		var targetTool = PlayerUtils.getTargetTool(blockHitResult, mainHandStack);

		if (targetTool != null && targetTool.getFaceToolType().equals(FaceToolType.BEDROCK))
			return;

		if (tooltip == null || currentTool == null) {
			handleNullTool(targetTool, hotbarSlot, inventory);
			return;
		}

		handleToolSwap(targetTool, currentTool, hotbarSlot, inventory);
	}

	private static FaceTool getCurrentToolFromTooltip(String tooltip) {
		if (tooltip == null)
			return null;

		for (var tool : FaceTool.values())
			if (tooltip.contains(tool.getFaceToolType().getName()))
				return tool;

		return null;
	}

	private static void handleNullTool(FaceTool targetTool, int hotbarSlot, PlayerInventory inventory) {
		if (targetTool != null && !inventory.getStack(targetTool.getSlotIndex()).isEmpty())
			PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
	}

	private static void handleToolSwap(FaceTool targetTool, FaceTool currentTool, int hotbarSlot, PlayerInventory inventory) {
		boolean isCurrentToolSlotEmpty = inventory.getStack(currentTool.getSlotIndex()).isEmpty();

		if (targetTool == null) {
			if (isCurrentToolSlotEmpty)
				swap(hotbarSlot, currentTool);
			else
				PlayerUtils.clickSlot(currentTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);

			return;
		}

		if (currentTool.getFaceToolType().equals(targetTool.getFaceToolType()))
			return;

		if (isCurrentToolSlotEmpty) {
			swap(hotbarSlot, currentTool);
			PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
		} else {
			PlayerUtils.clickSlot(currentTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
			PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
		}
	}

	private static void swap(int hotbarSlot, FaceTool tool) {
		PlayerUtils.clickSlot(36 + hotbarSlot, 0, SlotActionType.PICKUP);
		PlayerUtils.clickSlot(tool.getSlotIndex(), 0, SlotActionType.PICKUP);
	}
}
