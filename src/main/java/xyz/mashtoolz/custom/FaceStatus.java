package xyz.mashtoolz.custom;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.mashtoolz.FaceLift;

public enum FaceStatus {

	WELL_RESTED(12000, StatusEffectCategory.BENEFICIAL),
	ESCAPE_COOLDOWN(6000, StatusEffectCategory.HARMFUL);

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static final List<FaceStatus> EFFECTS = new ArrayList<>();

	private final int duration;
	private final StatusEffect effect;

	FaceStatus(int duration, StatusEffectCategory category) {
		this.duration = duration;
		this.effect = new FaceStatusEffect(category, 0x000000);
	}

	public int getMaxDuration() {
		return duration;
	}

	public StatusEffect getEffect() {
		return effect;
	}

	public void applyEffect() {
		var effect = new StatusEffectInstance(this.effect, this.duration, 0, false, false, true);
		INSTANCE.CLIENT.player.addStatusEffect(effect);
	}

	public void removeEffect() {
		INSTANCE.CLIENT.player.removeStatusEffect(this.effect);
	}

	public static void registerEffects() {
		for (FaceStatus status : FaceStatus.values()) {
			EFFECTS.add(status);
			Registry.register(Registries.STATUS_EFFECT, new Identifier("facelift", status.name().toLowerCase()), status.effect);
		}
	}

	public static void update() {
		var player = INSTANCE.CLIENT.player;
		EFFECTS.forEach(faceStatus -> {
			var effectInstance = player.getStatusEffect(faceStatus.getEffect());
			if (effectInstance != null && effectInstance.getDuration() <= 0)
				faceStatus.removeEffect();
		});
	}

	private static class FaceStatusEffect extends StatusEffect {

		protected FaceStatusEffect(StatusEffectCategory category, int color) {
			super(category, color);
		}

		@Override
		public boolean canApplyUpdateEffect(int duration, int amplifier) {
			return false;
		}
	}
}
