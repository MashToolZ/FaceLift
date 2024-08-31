package xyz.mashtoolz.utils;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.custom.FaceToolBlock;

public class PlayerUtils {

	public static FaceLift instance = FaceLift.getInstance();

	public static void clickSlot(int slotId, int button, SlotActionType actionType) {
		var syncId = instance.client.player.currentScreenHandler.syncId;
		instance.client.interactionManager.clickSlot(syncId, slotId, button, actionType, instance.client.player);
	}

	public static FaceTool getTargetTool(BlockHitResult blockHitResult) {
		var blockState = instance.client.world.getBlockState(blockHitResult.getBlockPos());
		var block = blockState.getBlock();
		if (block instanceof CropBlock) {
			var cropBlock = (CropBlock) block;
			if (cropBlock.getAge(blockState) != cropBlock.getMaxAge())
				return null;
		}

		if (block.equals(Blocks.BEDROCK))
			return FaceTool.BEDROCK;

		return FaceToolBlock.getFaceToolByBlock(block);
	}
}
