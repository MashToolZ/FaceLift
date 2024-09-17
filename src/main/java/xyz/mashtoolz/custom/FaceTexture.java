package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;

public class FaceTexture {

	public static final Identifier ITEM_GLOW = id("textures/gui/item_glow.png");
	public static final Identifier ITEM_STAR = id("textures/gui/item_star.png");
	public static final Identifier ABILITY_GLINT = id("textures/gui/ability_glint.png");

	public static final Identifier EMPTY_PICKAXE = id("textures/gui/empty_slot/pickaxe.png");
	public static final Identifier EMPTY_WOODCUTTINGAXE = id("textures/gui/empty_slot/woodcuttingaxe.png");
	public static final Identifier EMPTY_HOE = id("textures/gui/empty_slot/hoe.png");
	public static final Identifier EMPTY_POTION = id("textures/gui/empty_slot/potion.png");

	private static Identifier id(String path) {
		return new Identifier("facelift", path);
	}
}
