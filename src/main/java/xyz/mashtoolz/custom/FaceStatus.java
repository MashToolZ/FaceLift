package xyz.mashtoolz.custom;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

import java.util.ArrayList;
import java.util.List;

public enum FaceStatus {

    WELL_RESTED(12000, StatusEffectCategory.BENEFICIAL),
    ESCAPE_COOLDOWN(6000, StatusEffectCategory.NEUTRAL),
    CURSE_STACK(-1, StatusEffectCategory.HARMFUL);

    private static final FaceLift INSTANCE = FaceLift.getInstance();

    private static final List<FaceStatus> EFFECTS = new ArrayList<>();

    private final int duration;
    private final FaceStatusEffect effect;

    FaceStatus(int duration, StatusEffectCategory category) {
        this.duration = duration;
        this.effect = new FaceStatusEffect(category, 0x000000, this);
    }

    public FaceStatusEffect getEffect() {
        return effect;
    }

    public void applyEffect() {
        applyEffect(this, this.duration);
        INSTANCE.CONFIG.general.statusEffects.put(this, System.currentTimeMillis());
        FaceConfig.save();
    }

    private static void applyEffect(FaceStatus status, int duration) {
        if (status.equals(FaceStatus.CURSE_STACK))
            duration = -1;
        var effect = new FaceStatusEffectInstance(status, status.getEffect().getEntry(), duration, 0, false, false, true);
        assert INSTANCE.CLIENT.player != null;
        INSTANCE.CLIENT.player.addStatusEffect(effect);
    }

    public void removeEffect() {
        assert INSTANCE.CLIENT.player != null;
        INSTANCE.CLIENT.player.removeStatusEffect(this.getEffect().getEntry());
        INSTANCE.CONFIG.general.statusEffects.remove(this);
        xyz.mashtoolz.config.FaceConfig.save();
    }

    public static void getDescription(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir) {

        if (!(statusEffect instanceof FaceStatusEffectInstance faceStatusEffect))
            return;

        switch (faceStatusEffect.getFaceStatus()) {
            case CURSE_STACK -> cir.setReturnValue(Text.of("Curse Stacks: " + INSTANCE.CONFIG.general.curseStacks));
            default -> {
            }
        }
    }

    public static String getDuration(StatusEffectInstance statusEffect) {

        if (statusEffect.isInfinite())
            return I18n.translate("effect.duration.infinite");

        int ticks = MathHelper.floor((float) statusEffect.getDuration());
        int seconds = ticks / 20;

        if (seconds >= 3600) {
            return seconds / 3600 + "h";
        } else if (seconds >= 60) {
            return seconds / 60 + "m";
        } else {
            return seconds + "s";
        }
    }

    public static void registerEffects() {
        for (FaceStatus status : FaceStatus.values()) {
            EFFECTS.add(status);
            Registry.register(Registries.STATUS_EFFECT, Identifier.of("facelift", status.name().toLowerCase()), status.getEffect());
        }
    }

    public static void update() {
        var player = INSTANCE.CLIENT.player;
        if (player == null)
            return;

        for (var entry : INSTANCE.CONFIG.general.statusEffects.entrySet()) {
            var status = entry.getKey();
            var startTime = entry.getValue();
            var elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            var remainingTicks = (int) ((status.duration / 20) - elapsedTime) * 20;
            var statusEffect = player.getStatusEffect(status.getEffect().getEntry());
            if (statusEffect == null)
                applyEffect(status, remainingTicks);
        }

        for (var faceStatus : EFFECTS) {

            var statusEffect = player.getStatusEffect(faceStatus.getEffect().getEntry());
            if (statusEffect == null) {
                if (faceStatus.equals(FaceStatus.CURSE_STACK) && INSTANCE.CONFIG.general.curseStacks > 0)
                    faceStatus.applyEffect();
                continue;
            }

            var effect = (FaceStatusEffect) statusEffect.getEffectType().value();
            switch (effect.getFaceStatus()) {
                case CURSE_STACK -> {
                    if (INSTANCE.CONFIG.general.curseStacks <= 0) {
                        faceStatus.removeEffect();
                    }
                }

                default -> {
                    if (statusEffect.getDuration() <= 0)
                        faceStatus.removeEffect();
                }
            }
        }
    }

    public static class FaceStatusEffect extends StatusEffect {

        private final FaceStatus status;

        protected FaceStatusEffect(StatusEffectCategory category, int color, FaceStatus status) {
            super(category, color);
            this.status = status;
        }

        public FaceStatus getFaceStatus() {
            return status;
        }

        public RegistryEntry<StatusEffect> getEntry() {
            return Registries.STATUS_EFFECT.getEntry(this);
        }
    }

    public static class FaceStatusEffectInstance extends StatusEffectInstance {

        private final FaceStatus faceStatus;

        public FaceStatusEffectInstance(FaceStatus status, RegistryEntry<StatusEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showParticles) {
            super(effect, duration, amplifier, ambient, visible, showParticles);
            this.faceStatus = status;
        }

        public FaceStatus getFaceStatus() {
            return faceStatus;
        }
    }
}
