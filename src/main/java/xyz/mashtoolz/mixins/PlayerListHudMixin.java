package xyz.mashtoolz.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Unique;
import xyz.mashtoolz.FaceLift;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	@Unique
	private static final FaceLift INSTANCE = FaceLift.getInstance();

	@Inject(method = "render", at = @At("HEAD"))
	private void FL_onRender(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
		context.getMatrices().push();
		context.getMatrices().translate(0, INSTANCE.CONFIG.general.playerListHeightOffset, 0);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void FL_onRenderReturn(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
		context.getMatrices().pop();
	}
}
