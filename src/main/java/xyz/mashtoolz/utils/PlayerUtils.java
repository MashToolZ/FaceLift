package xyz.mashtoolz.utils;

import com.google.gson.JsonObject;

import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.config.ToolSlots.Tool;
import xyz.mashtoolz.custom.FaceToolBlock;

public class PlayerUtils {

	public static FaceLift instance = FaceLift.getInstance();

	public static MinecraftClient client = instance.client;

	public static Tool getCurrentTool(JsonObject itemData) {
		try {
			var tier = itemData.get("tier").getAsString();
			var currentTool = Config.inventory.toolSlots.getTool(tier);
			return currentTool;
		} catch (Exception e) {
			return null;
		}
	}

	public static Tool getTargetTool(BlockHitResult blockHitResult) {
		var blockState = client.world.getBlockState(blockHitResult.getBlockPos());
		var block = blockState.getBlock();
		if (block instanceof CropBlock) {
			var cropBlock = (CropBlock) block;
			if (cropBlock.getAge(blockState) != cropBlock.getMaxAge())
				return null;
		}

		var blockId = Registries.BLOCK.getId(block).toString();
		if (blockId.equals("minecraft:bedrock"))
			return new Tool("bedrock", -1, null);

		return FaceToolBlock.getById(blockId);
	}
}