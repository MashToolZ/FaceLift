package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;

public class FaceTexture {

    public static final Identifier ITEM_GLOW = id("textures/gui/item_glow.png");
    public static final Identifier ITEM_STAR = id("textures/gui/item_star.png");
    public static final Identifier ABILITY_GLINT = id("textures/gui/ability_glint.png");

    public static final Identifier HOTBAR = id("textures/gui/hotbar.png");
    public static final Identifier CUSTOM_HOTBAR = id("textures/gui/custom_hotbar.png");
    public static final Identifier SPELL_RED = id("textures/gui/spell_red.png");
    public static final Identifier SPELL_YELLOW = id("textures/gui/spell_yellow.png");
    public static final Identifier SPELL_BLUE = id("textures/gui/spell_blue.png");

    public static final Identifier EMPTY_PICKAXE = id("textures/gui/empty_slot/pickaxe.png");
    public static final Identifier EMPTY_WOODCUTTINGAXE = id("textures/gui/empty_slot/woodcuttingaxe.png");
    public static final Identifier EMPTY_HOE = id("textures/gui/empty_slot/hoe.png");

    private static Identifier id(String path) {
        return Identifier.of("facelift", path);
    }
}
