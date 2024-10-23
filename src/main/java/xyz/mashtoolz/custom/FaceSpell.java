package xyz.mashtoolz.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.utils.RenderUtils;

import java.util.Objects;

public class FaceSpell {

	private static final FaceLift INSTANCE = FaceLift.getInstance();

	private static final FaceSpell[] SPELLS = new FaceSpell[4];

	public static float GLOBALCD = 0.0F;

	private static final int maxFrames = 10;
	private static final int animTime = 100;
	private static final int stepTime = animTime / maxFrames;
	private static final float cooldownScale = 16.0F;
	private static final float readyScale = 20.0F;

	private final int spellIndex;

	private int lastStep = 0;
	private int damage;
	private int maxDamage;
	private float size;

	private int currentFrame;
	private long time;
	private long animationStartTime;

	private boolean isToggled;
	private boolean onCooldown;

	private int maxCharges;
	private int charges;

    public FaceSpell(ItemStack stack, int spellIndex) {
		this.spellIndex = spellIndex;
		this.damage = stack.getDamage();
		this.maxDamage = stack.getMaxDamage();
	}

	public static FaceSpell from(ItemStack stack) {
        assert INSTANCE.CLIENT.player != null;
        var spellIndex =INSTANCE.CLIENT.player.getInventory().getSlotWithStack(stack);
		if (spellIndex > 3 || spellIndex < 0)
			return null;

		if (SPELLS[spellIndex] == null)
			SPELLS[spellIndex] = new FaceSpell(stack, spellIndex);
		return SPELLS[spellIndex];
	}

	public void cast() {
		Objects.requireNonNull(INSTANCE.CLIENT.getNetworkHandler()).sendPacket(new UpdateSelectedSlotC2SPacket(spellIndex));
	}

	public float getSize() {
		return size;
	}

	public boolean isToggled() {
		return isToggled;
	}

	public void setToggled(boolean toggled) {
		this.isToggled = toggled;
	}

	public void update(DrawContext context, ItemStack stack, int x, int y) {
		int damage = stack.getDamage();
		int maxDamage = stack.getMaxDamage();
		int step = stack.getItemBarStep();

		if (!this.onCooldown || (maxDamage != 0 && (damage != 0 || this.lastStep == 13))) {
			this.damage = damage;
			this.maxDamage = maxDamage;
			this.lastStep = step;
			String charges = String.valueOf(stack.getMaxCount());
			String[] chargeParts = charges.split("");
			if (chargeParts.length == 2) {
				this.maxCharges = Integer.parseInt(chargeParts[0]);
				this.charges = Integer.parseInt(chargeParts[1]);
			}
		}
		if (this.isToggled())
			return;

		if (this.maxCharges > 1 && INSTANCE.CONFIG.inventory.customHotbar) {
			int emptyCharges = this.maxCharges - this.charges;
			if (this.onCooldown) {
				context.drawText(INSTANCE.CLIENT.textRenderer, "☊".repeat(this.charges) + "☋".repeat(emptyCharges), x, y - 54, 0xFFFFFF, false);
			} else
				context.drawText(INSTANCE.CLIENT.textRenderer, "☊".repeat(this.charges) + "☋".repeat(emptyCharges), x - 2, y - 56, 0xFFFFFF, false);
		}
		drawCooldown(context, x, y);
	}

	private void drawCooldown(DrawContext context, int x, int y) {

		float percent = 1.0F - ((float) this.damage / this.maxDamage);
		int size = INSTANCE.CONFIG.inventory.customHotbar ? (int) getSize() : 16;
		int offset = (size - 16) / 2;
		int centerX = x + (size / 2) - offset;
		int centerY = y + (size / 2) - offset;
		int numSegments = 128;
		float step = (-360 + (360.0F * percent)) / numSegments;

		var matrices = context.getMatrices();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderUtils.enableBlend();
		context.enableScissor(x - offset, y - offset, x + size - offset, y + size - offset);
		matrices.translate(0.0f, 0.0f, 400.0f);

		var tessellator = Tessellator.getInstance();
		var matrix = context.getMatrices().peek().getPositionMatrix();
		var builder = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

		for (int i = 0; i <= numSegments; i++) {
			float angle1 = -90 + i * step;
			float angle2 = -90 + (i + 1) * step;
			double rad1 = Math.toRadians(angle1);
			double rad2 = Math.toRadians(angle2);

			float x1 = centerX + 14 * (float) Math.cos(rad1);
			float y1 = centerY + 14 * (float) Math.sin(rad1);
			float x2 = centerX + 14 * (float) Math.cos(rad2);
			float y2 = centerY + 14 * (float) Math.sin(rad2);

			builder.vertex(matrix, centerX, centerY, 0).color(0, 0, 0, 192);
			builder.vertex(matrix, x1, y1, 0).color(0, 0, 0, 192);
			builder.vertex(matrix, x2, y2, 0).color(0, 0, 0, 192);
		}

		BufferRenderer.drawWithGlobalProgram(builder.end());
		context.disableScissor();
		matrices.translate(0.0f, 0.0f, -400.0f);
	}

	public void animate(DrawContext context, ItemStack stack) {

		FaceSpell.GLOBALCD = INSTANCE.CLIENT.player == null ? 0.0F : INSTANCE.CLIENT.player.getItemCooldownManager().getCooldownProgress(stack.getItem(), INSTANCE.CLIENT.getRenderTickCounter().getTickDelta(true));
		boolean cooldown = (1.0F - (float) this.damage / this.maxDamage) != 1.0F;
		boolean onCooldown = (!this.isToggled() && FaceSpell.GLOBALCD != 0.0F) || cooldown;
		long currentTime = System.currentTimeMillis();
		if ((this.maxCharges > 1 && this.charges > 0) || (FaceSpell.GLOBALCD != 0.0F && !cooldown))
			onCooldown = false;

        if (onCooldown) {
			if (!this.onCooldown) {
				this.animationStartTime = currentTime;
				this.onCooldown = true;
                this.isToggled = false;
			}
			if (!INSTANCE.CONFIG.inventory.customHotbar)
				this.size = cooldownScale;
			else if (this.isToggled())
				this.size = readyScale;
			else {
				long timeSinceAnimationStart = currentTime - this.animationStartTime;
				if (timeSinceAnimationStart >= animTime) {
					this.size = cooldownScale;
				} else {
					float progress = (float) timeSinceAnimationStart / animTime;
					this.size = readyScale - (readyScale - cooldownScale) * progress;
				}
			}
		} else {
			if (this.onCooldown) {
				this.animationStartTime = currentTime;
				this.onCooldown = false;
            }
			if (!INSTANCE.CONFIG.inventory.customHotbar)
				this.size = cooldownScale;
			else if (this.isToggled())
				this.size = readyScale;
			else {
				long timeSinceAnimationStart = currentTime - this.animationStartTime;
				if (timeSinceAnimationStart >= animTime) {
					this.size = readyScale;
				} else {
					float progress = (float) timeSinceAnimationStart / animTime;
					this.size = cooldownScale + (readyScale - cooldownScale) * progress;
				}
			}
		}
		if (currentTime - this.time >= stepTime) {
			this.time = currentTime;
			this.currentFrame = (this.currentFrame + 1) % maxFrames;
		}
		context.getMatrices().scale(this.size, -this.size, 16.0F);
	}
}
