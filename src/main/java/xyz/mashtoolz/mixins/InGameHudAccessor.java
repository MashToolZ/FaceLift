package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {

	@Accessor("title")
	Text getTitle();

	@Accessor("subtitle")
	Text getSubtitle();

	@Accessor("overlayMessage")
	Text getOverlayMessage();

	@Accessor("overlayMessage")
	void setOverlayMessage(Text overlayMessage);

	@Invoker("getCameraPlayer")
	PlayerEntity invokeGetCameraPlayer();

	@Invoker("renderHotbarItem")
	void invokeRenderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

	@Accessor("HOTBAR_SELECTION_TEXTURE")
	Identifier hotbarSelectionTexture();

	@Accessor("HOTBAR_OFFHAND_LEFT_TEXTURE")
	Identifier hotbarOffhandLeftTexture();

	@Accessor("HOTBAR_OFFHAND_RIGHT_TEXTURE")
	Identifier hotbarOffhandRightTexture();

	@Accessor("HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE")
	Identifier hotbarAttackIndicatorBackgroundTexture();

	@Accessor("HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE")
	Identifier hotbarAttackIndicatorProgressTexture();
}
