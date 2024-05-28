package xyz.mashtoolz.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import xyz.mashtoolz.FaceLift;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	private FaceLift instance;

	@Inject(method = "render", at = @At("HEAD"))
	private void onRender(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {

		if (instance == null)
			instance = FaceLift.getInstance();

		context.getMatrices().push();
		context.getMatrices().translate(0, instance.config.general.tabHeightOffset, 0);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void onRenderReturn(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
		context.getMatrices().pop();
	}
}
