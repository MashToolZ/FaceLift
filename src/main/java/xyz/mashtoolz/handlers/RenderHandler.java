package xyz.mashtoolz.handlers;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceEquipment;
import xyz.mashtoolz.custom.FaceFont;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceRarity;
import xyz.mashtoolz.custom.FaceTexture;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.displays.ArenaTimer;
import xyz.mashtoolz.displays.CombatTimer;
import xyz.mashtoolz.displays.DPSMeter;
import xyz.mashtoolz.displays.XPDisplay;
import xyz.mashtoolz.interfaces.EntityInterface;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import xyz.mashtoolz.mixins.ScreenAccessor;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.widget.DropDownMenu;
import xyz.mashtoolz.widget.SearchFieldWidget;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

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

		String title = screen.getTitle().getString();
		if (title.contains("库"))
			FaceEquipment.updateCache = true;

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

	public static void onHudRender(DrawContext context, float delta) {

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
			item = new FaceItem(stack);
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

	public static void drawHotbarItemSlot(DrawContext context, ItemStack stack, int x, int y, CallbackInfo ci) {

		if (!FaceConfig.General.onFaceLand || !ABILITY_ITEMS.contains(stack.getItem()))
			return;

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0.0f, 0.0f, 300.0f);
		RenderUtils.enableBlend();

		if (!stack.getEnchantments().isEmpty())
			drawGlintAnimation(context, x, y);

		if (stack.getItemBarStep() != 13)
			drawItemCooldown(context, stack, x, y);

		RenderUtils.disableBlend();
		matrices.translate(0.0f, 0.0f, -300.0f);
		matrices.pop();
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

	private static void drawItemCooldown(DrawContext context, ItemStack stack, int x, int y) {
		var tessellator = Tessellator.getInstance();
		var buffer = tessellator.getBuffer();
		var matrix = context.getMatrices().peek().getPositionMatrix();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		int centerX = x + 8;
		int centerY = y + 8;
		int numSegments = 64;
		float percent = 1.0F - (float) stack.getDamage() / stack.getMaxDamage();
		float step = (-360 + (360.0F * percent)) / numSegments;

		context.enableScissor(x, y, x + 16, y + 16);
		buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

		for (int i = 0; i < numSegments; i++) {
			float angle1 = -90 + i * step;
			float angle2 = -90 + (i + 1) * step;
			double rad1 = Math.toRadians(angle1);
			double rad2 = Math.toRadians(angle2);

			float x1 = centerX + 12 * (float) Math.cos(rad1);
			float y1 = centerY + 12 * (float) Math.sin(rad1);
			float x2 = centerX + 12 * (float) Math.cos(rad2);
			float y2 = centerY + 12 * (float) Math.sin(rad2);

			buffer.vertex(matrix, centerX, centerY, 0).color(0, 0, 0, 192).next();
			buffer.vertex(matrix, x1, y1, 0).color(0, 0, 0, 192).next();
			buffer.vertex(matrix, x2, y2, 0).color(0, 0, 0, 192).next();
		}

		tessellator.draw();
		context.disableScissor();
	}

	private static void drawGlintAnimation(DrawContext context, int x, int y) {
		int size = 16;
		int textureWidth = 960;
		int maxFrames = textureWidth / size;
		int stepTime = 1200 / maxFrames;

		context.fill(x, y, x + 16, y + 16, ColorUtils.hex2Int("#000000", 0x78));

		RenderSystem.setShaderTexture(0, FaceTexture.ABILITY_GLINT);
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
		context.drawTexture(FaceTexture.ABILITY_GLINT, x + 1, y + 1, 14, 14, 0 | (GLINT_FRAME * size), 0, size, size, maxFrames * size, size);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		context.drawTexture(FaceTexture.ABILITY_GLINT, x, y, 16, 16, 0 | (GLINT_FRAME * size), 0, size, size, maxFrames * size, size);

		if (System.currentTimeMillis() - GLINT_TIME >= stepTime) {
			GLINT_TIME = System.currentTimeMillis();
			GLINT_FRAME = (GLINT_FRAME + 1) % maxFrames;
		}
	}

	private static void renderToolSlot(DrawContext context, Slot slot, int x, int y) {
		var screen = (HandledScreenAccessor) CLIENT.currentScreen;
		var handler = screen.getHandler();
		if (handler.slots.size() == 46 && !CLIENT.currentScreen.getTitle().getString().isEmpty()) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
			if (slot.id == 44) {
				RenderSystem.setShaderTexture(0, FaceTexture.EMPTY_POTION);
				context.drawTexture(FaceTexture.EMPTY_POTION, x, y, 0, 0, 16, 16, 16, 16);
				return;
			}
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
		var rarity = item.getFaceRarity();
		var color = item.getColor();

		if (color == null || rarity.equals(FaceRarity.UNKNOWN)) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
			return;
		}

		float[] rgb = ColorUtils.getRGB(color);
		float opacity = hideItem ? 0.25F : CONFIG.inventory.rarity.opacity;

		matrices.translate(0.0f, 0.0f, 100.0f);
		RenderSystem.setShaderTexture(0, FaceTexture.ITEM_GLOW);
		RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], opacity);

		if (CONFIG.inventory.rarity.useTexture)
			context.drawTexture(FaceTexture.ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);
		else
			context.drawBorder(x, y, 16, 16, ColorUtils.hex2Int("#FFFFFF", 0xFF));

		matrices.translate(0.0f, 0.0f, -100.0f);

		if (rarity.getString().startsWith("MATERIAL_")) {
			int stars = Integer.parseInt(rarity.getString().split("_")[1]);
			matrices.translate(0.0f, 0.0f, 300.0f);
			RenderSystem.setShaderTexture(0, FaceTexture.ITEM_STAR);
			context.drawTexture(FaceTexture.ITEM_STAR, x, y, 0, 0, 3 * stars, 3, 3, 3);
			matrices.translate(0.0f, 0.0f, -300.0f);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
	}

}
