package xyz.mashtoolz.utils;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceEquipment;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceTool;

public class PlayerUtils {

	public static FaceLift instance = FaceLift.getInstance();

	public static boolean isMounted() {
		Entity ridingEntity = instance.client.player.getVehicle();
		return ridingEntity != null && ridingEntity != instance.client.player;
	}

	public static void clickSlot(int slotId, int button, SlotActionType actionType) {
		var syncId = instance.client.player.currentScreenHandler.syncId;
		instance.client.interactionManager.clickSlot(syncId, slotId, button, actionType, instance.client.player);
	}

	public static FaceTool getTargetTool(BlockHitResult blockHitResult, ItemStack currentStack) {
		var blockPos = blockHitResult.getBlockPos();
		var blockState = instance.client.world.getBlockState(blockPos);
		var block = blockState.getBlock();
		if (block instanceof CropBlock) {
			var cropBlock = (CropBlock) block;
			if (cropBlock.getAge(blockState) != cropBlock.getMaxAge())
				return null;
		}

		if (block.equals(Blocks.BEDROCK))
			return FaceTool.BEDROCK;

		var inventory = instance.client.player.getInventory();
		var blockRegistry = instance.client.world.getRegistryManager().get(RegistryKeys.BLOCK);
		var cachedBlockPos = new CachedBlockPosition(instance.client.player.getWorld(), blockPos, false);

		if (currentStack.canDestroy(blockRegistry, cachedBlockPos))
			return FaceTool.BEDROCK;

		List<FaceTool> possibleTools = new ArrayList<FaceTool>();
		for (var slot : FaceEquipment.slots) {
			if (!FaceEquipment.TOOL_TYPES.contains(slot.getFaceSlotType()))
				continue;

			var stack = inventory.getStack(slot.getIndex());
			if (stack.isEmpty())
				continue;

			if (!stack.canDestroy(blockRegistry, cachedBlockPos))
				continue;

			var item = new FaceItem(stack);
			if (item.invalid)
				continue;

			var tool = item.getFaceTool();
			if (tool == null)
				continue;

			possibleTools.add(tool);
		}

		if (possibleTools.size() == 1)
			return possibleTools.get(0);

		if (possibleTools.contains(FaceTool.HOE) && possibleTools.contains(FaceTool.WOODCUTTINGAXE))
			return FaceTool.HOE;

		return null;
	}
}
