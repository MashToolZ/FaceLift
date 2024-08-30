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

	WELL_RESTED(12000, StatusEffectCategory.BENEFICIAL);

	private static final FaceLift instance = FaceLift.getInstance();

	private static final List<FaceStatus> effects = new ArrayList<>();

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
		instance.client.player.addStatusEffect(effect);
	}

	public void removeEffect() {
		instance.client.player.removeStatusEffect(this.effect);
	}

	public static void update() {
		for (FaceStatus faceStatus : effects) {
			var effectInstance = instance.client.player.getStatusEffect(faceStatus.getEffect());
			if (effectInstance != null && effectInstance.getDuration() <= 0)
				faceStatus.removeEffect();
		}
	}

	public static void registerEffects() {
		for (FaceStatus status : FaceStatus.values()) {
			effects.add(status);
			Registry.register(Registries.STATUS_EFFECT, new Identifier("facelift", status.name().toLowerCase()), status.effect);
		}
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
