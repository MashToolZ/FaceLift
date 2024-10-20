package xyz.mashtoolz.handlers;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceEquipment;
import xyz.mashtoolz.custom.FaceFont;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceSpell;
import xyz.mashtoolz.custom.FaceTexture;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.custom.FaceType;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.displays.ArenaTimer;
import xyz.mashtoolz.displays.CombatTimer;
import xyz.mashtoolz.displays.DPSMeter;
import xyz.mashtoolz.displays.TeleportBar;
import xyz.mashtoolz.displays.XPDisplay;
import xyz.mashtoolz.interfaces.EntityInterface;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import xyz.mashtoolz.mixins.InGameHudAccessor;
import xyz.mashtoolz.mixins.ScreenAccessor;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.widget.DropDownMenu;
import xyz.mashtoolz.widget.SearchFieldWidget;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.crash.CrashCallable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;

public class RenderHandler {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static MinecraftClient CLIENT = INSTANCE.CLIENT;
	private static FaceConfig CONFIG = INSTANCE.CONFIG;

	public static SearchFieldWidget SEARCHBAR;
	private static DropDownMenu DROPDOWN;

	private static int GLINT_FRAME = 0;
	private static long GLINT_TIME = System.currentTimeMillis();

	public static final List<Item> ABILITY_ITEMS = Arrays.asList(Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE);
	public static final List<Item> IGNORED_ITEMS = Arrays.asList(Items.BARRIER, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.PLAYER_HEAD, Items.BARRIER);
	public static final List<Item> HIDDEN_ITEMS = Arrays.asList(Items.SHIELD, Items.TRIPWIRE_HOOK, Items.STRING);

	public static void afterInitScreen(MinecraftClient client, Screen screen, int width, int height) {

		if (!(screen instanceof HandledScreen))
			return;

		FaceItem.clearCache();

		String title = screen.getTitle().getString();
		// Equipment Screen
		if (title.contains("库")) {
			FaceEquipment.updateCache = true;
		}
		// Personal Bank Screen
		else if (title.contains("拽")) {

		}
		// Guild Bank Screen
		else if (title.contains("抭")) {

		}

		// FaceLift.info(true, TextUtils.escapeStringToUnicode(title, false));

		setupSearchBar(client, screen, width, height);
		setupDropdownMenu(screen, width, height);

		((ScreenAccessor) screen).invokeAddDrawableChild(SEARCHBAR);
		((ScreenAccessor) screen).invokeAddDrawableChild(DROPDOWN.getButton());
	}

	private static void setupSearchBar(MinecraftClient client, Screen screen, int width, int height) {
		var inventory = CONFIG.inventory;

		SEARCHBAR = new SearchFieldWidget(client.textRenderer, width / 2 - 90, height - 25, 180, 20, SEARCHBAR, Text.literal(inventory.searchbar.query));
		SEARCHBAR.setText(inventory.searchbar.query);
		SEARCHBAR.setMaxLength(255);

		SEARCHBAR.setChangedListener(text -> {
			inventory.searchbar.query = text;
			FaceConfig.save();
		});

		if (inventory.searchbar.highlight) {
			SEARCHBAR.highlighted = true;
			SEARCHBAR.setEditableColor(0xFFFF78);
		}
	}

	private static void setupDropdownMenu(Screen screen, int width, int height) {
		var inventory = CONFIG.inventory;

		DROPDOWN = new DropDownMenu(screen, "Options", width / 2 + 95, height - 25, 120, 20, true);

		DROPDOWN.addButton("Case-Sensitive: " + inventory.searchbar.caseSensitive, button -> {
			inventory.searchbar.caseSensitive = !inventory.searchbar.caseSensitive;
			button.setMessage(Text.literal(" Case-Sensitive: " + inventory.searchbar.caseSensitive));
			FaceConfig.save();
		}, inventory.searchbar.caseSensitive);
	}

	public static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {

		if (!FaceConfig.General.onFaceLand)
			return;

		if (!FaceEquipment.updateCache && FaceEquipment.handler != null && (CLIENT.currentScreen == null || !CLIENT.currentScreen.getTitle().getString().contains("库"))) {
			FaceEquipment.updateCachedEquipment();
			FaceEquipment.handler = null;
		}

		if (CLIENT.currentScreen == null)
			RenderHandler.SEARCHBAR = null;

		var matrices = context.getMatrices();
		matrices.push();

		if (CONFIG.combat.combatTimer.enabled)
			CombatTimer.draw(context);

		if (CONFIG.combat.dpsMeter.enabled)
			DPSMeter.draw(context);

		if (CONFIG.combat.arenaTimer.enabled) {
			ArenaTimer.updateTimer(context);
			ArenaTimer.draw(context);
		}

		if (CONFIG.general.xpDisplay.enabled)
			XPDisplay.draw(context);

		if (CONFIG.general.teleportBar.enabled)
			TeleportBar.draw(context);

		matrices.pop();
	}

	public static void beforeEntities(WorldRenderContext context) {

		if (!FaceConfig.General.onFaceLand)
			return;

		var world = context.world();

		world.getEntities().forEach(entity -> {

			if (entity.isRemoved() || !entity.isAlive() || !entity.isLiving() || entity.getType().equals(EntityType.ARMOR_STAND))
				return;

			var text = entity.getCustomName();
			if (text == null || text.equals(Text.EMPTY))
				return;

			var list = text.getSiblings();
			if (list.size() < 3)
				return;

			var flEntity = (EntityInterface) entity;
			var tag = list.get(0).getString().trim();
			int forceGlowingValue = 2;
			boolean tagFound = false;

			var optionalEntry = FaceFont.entries(FType.MOB_TAG).stream().filter(e -> e.getKey().equals(tag)).findFirst();
			if (optionalEntry.isPresent()) {
				var entry = optionalEntry.get();
				flEntity.FL_setGlowingColor(ColorUtils.hex2Int(entry.getValue(), 0xFF));
				tagFound = true;
			}

			if (!tagFound) {
				var color = list.get(0).getStyle().getColor();
				if (color != null)
					flEntity.FL_setGlowingColor(ColorUtils.hex2Int(color.getHexCode(), 0xFF));
				else
					forceGlowingValue = 1;
			}

			flEntity.FL_setForceGlowing(forceGlowingValue);
		});
	}

	public static void onHandledScreenRender(DrawContext context, int mouseX, int mouseY, float delta) {

		if (!FaceConfig.General.onFaceLand || CLIENT.currentScreen == null || !(CLIENT.currentScreen instanceof HandledScreen))
			return;

		var screen = (HandledScreenAccessor) CLIENT.currentScreen;
		var handler = screen.getHandler();
		if (handler == null)
			return;

		if (FaceEquipment.updateCache && CLIENT.currentScreen.getTitle().getString().contains("库")) {
			FaceEquipment.updateCache = false;
			FaceEquipment.handler = handler;
		}

		if (Keybinds.isPressed(Keybinds.COMPARE_TOOLTIP))
			RenderUtils.compareAndRenderTooltip(screen, context, mouseX, mouseY);
	}

	public static void onHandledScreenKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (RenderHandler.SEARCHBAR != null && RenderHandler.SEARCHBAR.isFocused()) {
			var options = INSTANCE.CLIENT.options;
			List<KeyBinding> keys = new ArrayList<>(List.of(options.inventoryKey, options.dropKey, options.pickItemKey));
			keys.addAll(Arrays.asList(options.hotbarKeys));
			if (keys.stream().anyMatch(key -> key.matchesKey(keyCode, scanCode)))
				cir.setReturnValue(true);
		}
	}

	public static void drawItem(DrawContext context, @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
		if (!stack.isEmpty()) {
			var matrices = context.getMatrices();
			BakedModel bakedModel = CLIENT.getItemRenderer().getModel(stack, world, entity, seed);
			matrices.push();
			matrices.translate((float) (x + 8), (float) (y + 8), (float) (150 + (bakedModel.hasDepth() ? z : 0)));

			try {
				var isSpell = ABILITY_ITEMS.contains(stack.getItem());
				if (isSpell && !(CLIENT.currentScreen instanceof HandledScreen)) {
					var spell = FaceSpell.from(stack);
					if (spell == null)
						return;
					spell.animate(context, stack, x, y);
				} else
					matrices.scale(16.0F, -16.0F, 16.0F);

				boolean bl = !bakedModel.isSideLit();
				if (bl) {
					DiffuseLighting.disableGuiDepthLighting();
				}

				CLIENT.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, matrices, context.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
				context.draw();
				if (bl) {
					DiffuseLighting.enableGuiDepthLighting();
				}
			} catch (Throwable var12) {
				CrashReport crashReport = CrashReport.create(var12, "Rendering item");
				CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
				crashReportSection.add("Item Type", (CrashCallable<String>) (() -> String.valueOf(stack.getItem())));
				crashReportSection.add("Item Components", (CrashCallable<String>) (() -> String.valueOf(stack.getComponents())));
				crashReportSection.add("Item Foil", (CrashCallable<String>) (() -> String.valueOf(stack.hasGlint())));
				throw new CrashException(crashReport);
			}

			matrices.pop();
		}

		ci.cancel();
	}

	public static void renderHotbar(InGameHud inGameHud, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (!FaceConfig.General.onFaceLand || !CONFIG.inventory.customHotbar)
			return;

		var hud = ((InGameHudAccessor) inGameHud);
		var matrices = context.getMatrices();
		var player = hud.invokeGetCameraPlayer();

		if (player != null) {

			ItemStack itemStack = player.getOffHandStack();
			Arm arm = player.getMainArm().getOpposite();
			int i = context.getScaledWindowWidth() / 2;
			RenderSystem.enableBlend();
			matrices.push();
			matrices.translate(0.0F, 0.0F, -90.0F);

			context.drawTexture(FaceTexture.HOTBAR_TEXTURE, i - 91, context.getScaledWindowHeight() - 22, 0, 0, 182, 22, 182, 22);

			var inventory = player.getInventory();
			int selectedSlot = inventory.selectedSlot;
			if (selectedSlot >= 4 && selectedSlot <= 8) {
				int visualIndex = selectedSlot - 4;
				context.drawGuiTexture(hud.hotbarSelectionTexture(), i - 91 - 1 + 30 + visualIndex * 25, context.getScaledWindowHeight() - 22 - 1, 24, 23);
			}
			if (!itemStack.isEmpty())
				if (arm == Arm.LEFT)
					context.drawGuiTexture(hud.hotbarOffhandLeftTexture(), i - 91 - 29, context.getScaledWindowHeight() - 23, 29, 24);
				else
					context.drawGuiTexture(hud.hotbarOffhandRightTexture(), i + 91, context.getScaledWindowHeight() - 23, 29, 24);

			matrices.pop();
			RenderSystem.disableBlend();
			int l = 1;

			for (int m = 0; m < 9; m++) {
				boolean isSpellSlot = m < 4;
				int x = isSpellSlot ? (i - 90 + 42 + m * 25 + 2) : (i - 90 + 30 + (m - 4) * 25 + 2);
				int y = isSpellSlot ? (context.getScaledWindowHeight() / 2 + 35) : (context.getScaledWindowHeight() - 16 - 3);
				hud.invokeRenderHotbarItem(context, x, y, tickCounter, player, player.getInventory().main.get(m), l++);
			}

			if (!itemStack.isEmpty()) {
				int m = context.getScaledWindowHeight() - 16 - 3;
				if (arm == Arm.LEFT) {
					hud.invokeRenderHotbarItem(context, i - 91 - 26, m, tickCounter, player, itemStack, l++);
				} else {
					hud.invokeRenderHotbarItem(context, i + 91 + 10, m, tickCounter, player, itemStack, l++);
				}
			}

			if (INSTANCE.CLIENT.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
				RenderSystem.enableBlend();
				float f = INSTANCE.CLIENT.player.getAttackCooldownProgress(0.0F);
				if (f < 1.0F) {
					int n = context.getScaledWindowHeight() - 20;
					int o = i + 91 + 6;
					if (arm == Arm.RIGHT) {
						o = i - 91 - 22;
					}

					int p = (int) (f * 19.0F);
					context.drawGuiTexture(hud.hotbarAttackIndicatorBackgroundTexture(), o, n, 18, 18);
					context.drawGuiTexture(hud.hotbarAttackIndicatorProgressTexture(), 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
				}

				RenderSystem.disableBlend();
			}
		}

		ci.cancel();
	}

	public static void drawSlot_start(DrawContext context, Slot slot, CallbackInfo ci) {
		if (!FaceConfig.General.onFaceLand)
			return;

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		RenderUtils.enableBlend();

		int x = slot.x, y = slot.y;
		var stack = slot.getStack();

		FaceItem item;
		boolean hideItem = false;

		if (stack.isEmpty() || IGNORED_ITEMS.contains(stack.getItem()))
			renderToolSlot(context, slot, x, y);
		else {
			item = FaceItem.from(stack);
			hideItem = searchbarCheck(item);
			renderNormalItem(context, matrices, item, x, y, hideItem);
		}

		RenderUtils.disableBlend();
		matrices.pop();

		if (hideItem && HIDDEN_ITEMS.contains(stack.getItem()))
			ci.cancel();
	}

	public static void drawSlot_end(DrawContext context, Slot slot) {
		if (!FaceConfig.General.onFaceLand)
			return;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void drawItemInSlot(TextRenderer textRenderer, DrawContext context, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {

		if (!FaceConfig.General.onFaceLand || !ABILITY_ITEMS.contains(stack.getItem()))
			return;

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		RenderUtils.enableBlend();

		var spell = FaceSpell.from(stack);
		if (!stack.getEnchantments().isEmpty() || (spell != null && spell.isToggled())) {
			drawGlintAnimation(context, stack, x, y);
			if (spell != null) {
				if (!spell.isToggled())
					spell.setToggled(true);
				spell.update(context, stack, x, y);
			}
		}

		RenderUtils.disableBlend();
		matrices.pop();

		if (!stack.isEmpty()) {
			matrices.push();
			if (stack.getCount() != 1 || countOverride != null) {
				String string = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
				matrices.translate(0.0F, 0.0F, 200.0F);
				context.drawText(CLIENT.textRenderer, string, x + 19 - 2 - CLIENT.textRenderer.getWidth(string), y + 6 + 3, 16777215, true);
			}

			if (stack.isItemBarVisible()) {
				int i = stack.getItemBarStep();
				int j = stack.getItemBarColor();
				int k = x + 2;
				int l = y + 13;
				context.fill(RenderLayer.getGuiOverlay(), k, l, k + 13, l + 2, Colors.BLACK);
				context.fill(RenderLayer.getGuiOverlay(), k, l, k + i, l + 1, j | Colors.BLACK);
			}
			matrices.pop();
		}
		ci.cancel();
	}

	private static boolean searchbarCheck(FaceItem item) {
		if (SEARCHBAR == null || !SEARCHBAR.highlighted)
			return false;

		var query = SEARCHBAR.getText();
		if (query.isEmpty() || query.length() == 0)
			return true;

		var name = item.getName();
		if (name.equalsIgnoreCase("air"))
			return true;

		var tooltip = item.getTooltip();

		try {
			Pattern pattern = Pattern.compile(query, Pattern.DOTALL | (CONFIG.inventory.searchbar.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE));
			if (!pattern.matcher(tooltip).find())
				return true;
			SEARCHBAR.setEditableColor(0xFFFF78);
		} catch (PatternSyntaxException e) {
			SEARCHBAR.setEditableColor(0xFF7878);
		}

		return false;
	}

	private static void drawGlintAnimation(DrawContext context, ItemStack stack, int x, int y) {

		var spell = FaceSpell.from(stack);
		var matrices = context.getMatrices();
		int size = 16;
		float f = CLIENT.player == null ? 0.0F : CLIENT.player.getItemCooldownManager().getCooldownProgress(stack.getItem(), CLIENT.getRenderTickCounter().getTickDelta(true));
		boolean onCooldown = f != 0.0F || (1.0F - (float) stack.getDamage() / stack.getMaxDamage()) != 1.0F;
		int iSize = CLIENT.currentScreen instanceof HandledScreen || !CONFIG.inventory.customHotbar ? 16 : (spell != null && spell.isToggled()) ? 20 : onCooldown ? 16 : 20;

		int iOffset = (iSize - size) / 2;
		int maxFrames = 960 / size;
		int stepTime = 1200 / maxFrames;

		matrices.translate(0.0f, 0.0f, 400.0f);
		context.fill(x - iOffset, y - iOffset, x + iSize - iOffset, y + iSize - iOffset, ColorUtils.hex2Int("#000000", 0x78));

		RenderSystem.setShaderTexture(0, FaceTexture.ABILITY_GLINT);
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
		context.drawTexture(FaceTexture.ABILITY_GLINT, x - iOffset + 1, y - iOffset + 1, iSize - 2, iSize - 2, 0 | (GLINT_FRAME * size), 0, size, size, maxFrames * size, size);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		context.drawTexture(FaceTexture.ABILITY_GLINT, x - iOffset, y - iOffset, iSize, iSize, 0 | (GLINT_FRAME * size), 0, size, size, maxFrames * size, size);
		matrices.translate(0.0f, 0.0f, -400.0f);

		if (System.currentTimeMillis() - GLINT_TIME >= stepTime) {
			GLINT_TIME = System.currentTimeMillis();
			GLINT_FRAME = (GLINT_FRAME + 1) % maxFrames;
		}
	}

	private static void renderToolSlot(DrawContext context, Slot slot, int x, int y) {
		if (!CONFIG.inventory.autoTool.enabled)
			return;

		var screen = (HandledScreenAccessor) CLIENT.currentScreen;
		var handler = screen.getHandler();
		if (handler.slots.size() == 46 && !CLIENT.currentScreen.getTitle().getString().isEmpty()) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
			for (var tool : FaceTool.values()) {
				if (slot.id == tool.getSlotIndex()) {
					RenderSystem.setShaderTexture(0, tool.getTexture());
					context.drawTexture(tool.getTexture(), x, y, 0, 0, 16, 16, 16, 16);
					break;
				}
			}
		}
	}

	private static void renderNormalItem(DrawContext context, MatrixStack matrices, FaceItem item, int x, int y, boolean hideItem) {
		if (!CONFIG.inventory.itemColors.enabled)
			return;

		var type = item.getFaceType();
		var color = item.getColor();

		if (color == null || type.equals(FaceType.UNKNOWN)) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
			return;
		}

		float[] rgb = ColorUtils.getRGB(color);
		float opacity = hideItem ? 0.25F : CONFIG.inventory.itemColors.opacity;

		matrices.translate(0.0f, 0.0f, 100.0f);
		RenderSystem.setShaderTexture(0, FaceTexture.ITEM_GLOW);
		RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], opacity);

		if (CONFIG.inventory.itemColors.useTexture)
			context.drawTexture(FaceTexture.ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);
		else
			context.drawBorder(x, y, 16, 16, ColorUtils.hex2Int("#FFFFFF", 0xFF));

		matrices.translate(0.0f, 0.0f, -100.0f);

		if (type.getString().startsWith("MATERIAL_")) {
			int stars = Integer.parseInt(type.getString().split("_")[1]);
			matrices.translate(0.0f, 0.0f, 300.0f);
			RenderSystem.setShaderTexture(0, FaceTexture.ITEM_STAR);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			context.drawTexture(FaceTexture.ITEM_STAR, x, y, 0, 0, 3 * stars, 3, 3, 3);
			matrices.translate(0.0f, 0.0f, -300.0f);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
	}

}
