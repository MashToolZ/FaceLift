package xyz.mashtoolz.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import xyz.mashtoolz.FaceLift;

public enum FaceToolBlock {

	COAL_ORE(Blocks.COAL_ORE, FaceToolType.PICKAXE),
	IRON_ORE(Blocks.IRON_ORE, FaceToolType.PICKAXE),
	GOLD_ORE(Blocks.GOLD_ORE, FaceToolType.PICKAXE),
	DIAMOND_ORE(Blocks.DIAMOND_ORE, FaceToolType.PICKAXE),
	EMERALD_ORE(Blocks.EMERALD_ORE, FaceToolType.PICKAXE),
	LAPIS_ORE(Blocks.LAPIS_ORE, FaceToolType.PICKAXE),
	REDSTONE_ORE(Blocks.REDSTONE_ORE, FaceToolType.PICKAXE),
	DEAD_TUBE_CORAL_WALL_FAN(Blocks.DEAD_TUBE_CORAL_WALL_FAN, FaceToolType.PICKAXE),

	OAK_WOOD(Blocks.OAK_WOOD, FaceToolType.WOODCUTTINGAXE),
	DARK_OAK_WOOD(Blocks.DARK_OAK_WOOD, FaceToolType.WOODCUTTINGAXE),
	SPRUCE_WOOD(Blocks.SPRUCE_WOOD, FaceToolType.WOODCUTTINGAXE),
	JUNGLE_WOOD(Blocks.JUNGLE_WOOD, FaceToolType.WOODCUTTINGAXE),
	ACACIA_WOOD(Blocks.ACACIA_WOOD, FaceToolType.WOODCUTTINGAXE),

	WHEAT(Blocks.WHEAT, FaceToolType.HOE),
	AZURE_BLUET(Blocks.AZURE_BLUET, FaceToolType.HOE),
	BROWN_MUSHROOM(Blocks.BROWN_MUSHROOM, FaceToolType.HOE),
	RED_MUSHROOM(Blocks.RED_MUSHROOM, FaceToolType.HOE),
	JUNGLE_SAPLING(Blocks.JUNGLE_SAPLING, FaceToolType.HOE);

	private static FaceLift instance = FaceLift.getInstance();

	private final Block block;
	private final FaceToolType type;

	private FaceToolBlock(Block block, FaceToolType type) {
		this.block = block;
		this.type = type;
	}

	public Block getBlock() {
		return block;
	}

	public FaceTool getFaceTool() {
		return instance.config.inventory.autoTool.get(type);
	}

	public static FaceTool getFaceToolByBlock(Block block) {
		for (FaceToolBlock toolBlock : FaceToolBlock.values()) {
			if (toolBlock.getBlock().equals(block))
				return toolBlock.getFaceTool();
		}
		return null;
	}
}
