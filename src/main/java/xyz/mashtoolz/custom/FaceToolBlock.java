package xyz.mashtoolz.custom;

import xyz.mashtoolz.FaceLift;

public enum FaceToolBlock {

	COAL_ORE("minecraft:coal_ore", "pickaxe"),
	IRON_ORE("minecraft:iron_ore", "pickaxe"),
	GOLD_ORE("minecraft:gold_ore", "pickaxe"),
	DIAMOND_ORE("minecraft:diamond_ore", "pickaxe"),
	EMERALD_ORE("minecraft:emerald_ore", "pickaxe"),
	LAPIS_ORE("minecraft:lapis_ore", "pickaxe"),
	REDSTONE_ORE("minecraft:redstone_ore", "pickaxe"),
	DEAD_TUBE_CORAL_WALL_FAN("minecraft:dead_tube_coral_wall_fan", "pickaxe"),

	OAK_WOOD("minecraft:oak_wood", "woodcuttingaxe"),
	DARK_OAK_WOOD("minecraft:dark_oak_wood", "woodcuttingaxe"),
	SPRUCE_WOOD("minecraft:spruce_wood", "woodcuttingaxe"),
	JUNGLE_WOOD("minecraft:jungle_wood", "woodcuttingaxe"),
	ACACIA_WOOD("minecraft:acacia_wood", "woodcuttingaxe"),

	WHEAT("minecraft:wheat", "hoe"),
	AZURE_BLUET("minecraft:azure_bluet", "hoe"),
	BROWN_MUSHROOM("minecraft:brown_mushroom", "hoe"),
	RED_MUSHROOM("minecraft:red_mushroom", "hoe"),
	JUNGLE_SAPLING("minecraft:jungle_sapling", "hoe");

	private static FaceLift instance = FaceLift.getInstance();

	private final String id;
	private final String name;

	private FaceToolBlock(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public FaceTool getTool() {
		return instance.config.inventory.autoTool.get(name);
	}

	public static FaceTool getById(String id) {
		for (FaceToolBlock block : FaceToolBlock.values()) {
			if (block.getId().equals(id))
				return block.getTool();
		}
		return null;
	}
}
