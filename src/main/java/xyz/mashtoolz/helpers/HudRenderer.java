package xyz.mashtoolz.helpers;

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
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceRarity;
import xyz.mashtoolz.custom.FaceSlot;
import xyz.mashtoolz.custom.FaceTexture;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.custom.FaceSlotType;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import xyz.mashtoolz.mixins.ScreenInterface;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.widget.DropDownMenu;
import xyz.mashtoolz.widget.SearchFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class HudRenderer {

	private static FaceLift instance = FaceLift.getInstance();
	private static MinecraftClient client = instance.client;
	private static FaceConfig config = instance.config;

	public static SearchFieldWidget searchBar;
	private static DropDownMenu dropdown;

	private static int glintFrame = 0;
	private static long glintTime = System.currentTimeMillis();

	public static final ArrayList<Item> ABILITY_ITEMS = new ArrayList<>(Arrays.asList(Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE));
	public static final ArrayList<Item> IGNORED_ITEMS = new ArrayList<>(Arrays.asList(Items.BARRIER, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.PLAYER_HEAD, Items.BARRIER));
	public static final ArrayList<Item> HIDDEN_ITEMS = new ArrayList<>(Arrays.asList(Items.SHIELD, Items.TRIPWIRE_HOOK));
	public static final ArrayList<FaceSlotType> TOOL_TYPES = new ArrayList<>(Arrays.asList(FaceSlotType.PICKAXE, FaceSlotType.WOODCUTTINGAXE, FaceSlotType.HOE));

	public static void onHudRender(DrawContext context, float delta) {

		if (!FaceConfig.General.onFaceLand)
			return;

		if (!FaceEquipment.updateCache && FaceEquipment.handler != null && (client.currentScreen == null || !client.currentScreen.getTitle().getString().contains("库"))) {
			FaceEquipment.updateCachedEquipment();
			FaceEquipment.handler = null;
		}

		if (client.currentScreen == null)
			HudRenderer.searchBar = null;

		var matrices = context.getMatrices();
		matrices.push();

		if (config.combat.combatTimer.enabled)
			CombatTimer.draw(context);

		if (config.combat.dpsMeter.enabled)
			DPSMeter.draw(context);

		if (config.combat.arenaTimer.enabled) {
			ArenaTimer.updateTimer(context);
			ArenaTimer.draw(context);
		}

		if (config.general.xpDisplay.enabled)
			XPDisplay.draw(context);

		matrices.pop();
	}

	public static void onHandledScreenRenderTail(DrawContext context, int mouseX, int mouseY, float delta) {

		if (!FaceConfig.General.onFaceLand || client.currentScreen == null || !(client.currentScreen instanceof HandledScreen))
			return;

		var screen = (HandledScreenAccessor) client.currentScreen;
		var handler = screen.getHandler();
		if (handler == null)
			return;

		if (FaceEquipment.updateCache && client.currentScreen.getTitle().getString().contains("库")) {
			FaceEquipment.updateCache = false;
			FaceEquipment.handler = handler;
		}

		if (Keybinds.isPressed(Keybinds.compare))
			compareAndRenderTooltip(screen, context, mouseX, mouseY);
	}

	private static boolean compareStacks(ItemStack stack1, ItemStack stack2) {
		return stack1.getNbt().asString().equals(stack2.getNbt().asString());
	}

	private static FaceSlot getComparisonSlot(FaceItem item) {
		var slot = item.getFaceSlot(false);
		if (slot == null)
			return null;

		if (slot.getFaceType().equals(FaceSlotType.MAINHAND)) {
			var offHandStack = FaceSlot.OFFHAND.getStack();
			if (!offHandStack.isEmpty() && !new FaceItem(offHandStack).invalid) {
				var offHandSlot = new FaceItem(offHandStack).getFaceSlot(false);
				if (offHandSlot.getFaceType().equals(FaceSlotType.MAINHAND))
					slot = item.getFaceSlot(true);
			}
		}
		return slot;
	}

	private static void compareAndRenderTooltip(HandledScreenAccessor screen, DrawContext context, int mouseX, int mouseY) {
		var focusedSlot = screen.getFocusedSlot();
		if (focusedSlot == null || focusedSlot.getStack().isEmpty())
			return;

		var focusedItem = new FaceItem(focusedSlot.getStack());
		if (focusedItem.invalid)
			return;

		var compareSlot = getComparisonSlot(focusedItem);
		if (compareSlot == null)
			return;

		var compareStack = compareSlot.getStack();
		if (compareStack.isEmpty() || compareStacks(focusedSlot.getStack(), compareStack))
			return;

		RenderUtils.drawTooltip(context, compareStack, mouseX, mouseY);
	}

	public static void afterInitScreen(MinecraftClient client, Screen screen, int width, int height) {

		if (!FaceConfig.General.onFaceLand)
			return;

		if (screen instanceof HandledScreen) {

			if (client.currentScreen.getTitle().getString().contains("库")) {
				FaceEquipment.clearCache();
				FaceEquipment.updateCache = true;
			}

			var inventory = config.inventory;
			searchBar = new SearchFieldWidget(client.textRenderer, width / 2 - 90, height - 25, 180, 20, searchBar, Text.literal(inventory.searchbar.query));
			searchBar.setText(inventory.searchbar.query);
			searchBar.setMaxLength(255);

			searchBar.setChangedListener(text -> {
				inventory.searchbar.query = text;
				FaceConfig.save();
			});

			if (inventory.searchbar.highlight) {
				searchBar.highlighted = true;
				searchBar.setEditableColor(0xFFFF78);
			}

			dropdown = new DropDownMenu(screen, "Options", width / 2 + 95, height - 25, 90, 20, true);

			dropdown.addButton(" Case: " + inventory.searchbar.caseSensitive, button -> {
				inventory.searchbar.caseSensitive = !inventory.searchbar.caseSensitive;
				button.setMessage(Text.literal(" Case: " + inventory.searchbar.caseSensitive));
				FaceConfig.save();
			}, inventory.searchbar.caseSensitive);

			dropdown.addButton("Regex: " + inventory.searchbar.regex, button -> {
				inventory.searchbar.regex = !inventory.searchbar.regex;
				button.setMessage(Text.literal("Regex: " + inventory.searchbar.regex));
				FaceConfig.save();
			}, inventory.searchbar.regex);

			((ScreenInterface) screen).invokeAddDrawableChild(searchBar);
			((ScreenInterface) screen).invokeAddDrawableChild(dropdown.getButton());
		}
	}

	private static boolean searchbarCheck(FaceItem item) {
		var hideItem = false;
		if (searchBar != null && searchBar.highlighted) {
			var name = item.getName();
			var query = searchBar.getText();
			var tooltip = item.getTooltip();
			if (!config.inventory.searchbar.caseSensitive) {
				name = name.toLowerCase();
				query = query.toLowerCase();
				tooltip = tooltip.toLowerCase();
			}

			if (query.length() == 0 || name.toLowerCase().equals("air"))
				hideItem = true;

			if (config.inventory.searchbar.regex) {
				try {
					Pattern pattern = Pattern.compile(query, Pattern.DOTALL);
					if (!pattern.matcher(name).find() && !pattern.matcher(tooltip).find())
						hideItem = true;

					searchBar.setEditableColor(0xFFFF78);
				} catch (PatternSyntaxException e) {
					searchBar.setEditableColor(0xFF7878);
				}

			} else {
				if (!name.contains(query) && !tooltip.contains(query))
					hideItem = true;
			}
		}
		return hideItem;
	}

	private static void drawItemCooldown(DrawContext context, ItemStack stack, int x, int y) {

		var tessellator = Tessellator.getInstance();
		var buffer = tessellator.getBuffer();
		var matrix = context.getMatrices().peek().getPositionMatrix();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		int radius = 12;
		int centerX = x + 8;
		int centerY = y + 8;
		int numSegments = 64;
		float percent = 1.0F - (float) stack.getDamage() / stack.getMaxDamage();
		float step = (-360 + (360.0F * percent)) / numSegments;
		int r = 0, g = 0, b = 0, a = 192;

		context.enableScissor(x, y, x + 16, y + 16);
		buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

		for (int i = 0; i < numSegments; i++) {
			float angle1 = -90 + i * step;
			float angle2 = -90 + (i + 1) * step;

			float x1 = centerX + radius * (float) Math.cos(Math.toRadians(angle1));
			float y1 = centerY + radius * (float) Math.sin(Math.toRadians(angle1));
			float x2 = centerX + radius * (float) Math.cos(Math.toRadians(angle2));
			float y2 = centerY + radius * (float) Math.sin(Math.toRadians(angle2));

			buffer.vertex(matrix, centerX, centerY, 0).color(r, g, b, a).next();
			buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a).next();
			buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a).next();
		}

		tessellator.draw();
		context.disableScissor();
	}

	public static void preDrawHotbarItemSlot(DrawContext context, ItemStack stack, int x, int y, CallbackInfo ci) {

		if (!ABILITY_ITEMS.contains(stack.getItem()))
			return;

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0.0f, 0.0f, 300.0f);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		boolean isToggled = !stack.getEnchantments().isEmpty();
		if (isToggled) {

			int size = 16;
			int textureWidth = 960;
			int maxFrames = textureWidth / size;

			RenderSystem.setShaderTexture(0, FaceTexture.ABILITY_GLINT);
			RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
			context.drawTexture(FaceTexture.ABILITY_GLINT, x + 1, y + 1, 14, 14, 0 | (glintFrame * size), 0, size, size, maxFrames * size, size);

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			context.drawTexture(FaceTexture.ABILITY_GLINT, x, y, 16, 16, 0 | (glintFrame * size), 0, size, size, maxFrames * size, size);

			int stepTime = 1200 / maxFrames;
			if (System.currentTimeMillis() - glintTime >= stepTime) {
				glintTime = System.currentTimeMillis();
				if (glintFrame < maxFrames)
					glintFrame++;
				else if (glintFrame == maxFrames)
					glintFrame = 0;
			}
		}

		if (stack.getItemBarStep() != 13)
			drawItemCooldown(context, stack, x, y);

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		matrices.translate(0.0f, 0.0f, -300.0f);
		matrices.pop();
	}

	public static void preDrawItemSlot(DrawContext context, Slot slot, CallbackInfo ci) {

		if (!FaceConfig.General.onFaceLand)
			return;

		MatrixStack matrices = context.getMatrices();
		matrices.push();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		int x = slot.x, y = slot.y;
		var stack = slot.getStack();
		if ((stack.isEmpty() || IGNORED_ITEMS.contains(stack.getItem())) || ABILITY_ITEMS.contains(stack.getItem())) {
			var screen = (HandledScreenAccessor) client.currentScreen;
			var handler = screen.getHandler();
			if (handler.slots.size() == 46 && client.currentScreen.getTitle().getString().length() != 0) {
				for (var tool : FaceTool.values()) {
					if (slot.id == tool.getSlotIndex()) {
						RenderSystem.setShaderTexture(0, tool.getTexture());
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						context.drawTexture(tool.getTexture(), x, y, 0, 0, 16, 16, 16, 16);
						break;
					}
				}
			}
			matrices.pop();
			return;
		}

		FaceItem item = new FaceItem(stack);
		boolean hideItem = searchbarCheck(item);
		var rarity = item.getFaceRarity();
		var color = item.getColor();
		if (color != null && !rarity.equals(FaceRarity.UNKNOWN)) {
			float[] rgb = ColorUtils.getRGB(color);

			matrices.translate(0.0f, 0.0f, 100.0f);
			RenderSystem.setShaderTexture(0, FaceTexture.ITEM_GLOW);
			RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], hideItem ? 0.25F : config.inventory.rarity.opacity);
			if (config.inventory.rarity.useTexture)
				context.drawTexture(FaceTexture.ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);
			else
				context.drawBorder(x, y, 16, 16, ColorUtils.hex2Int("#FFFFFF", 0xFF));
			matrices.translate(0.0f, 0.0f, -100.0f);

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);

			if (rarity.getString().startsWith("MATERIAL_")) {
				matrices.translate(0.0f, 0.0f, 300.0f);
				var stars = Integer.parseInt(rarity.getString().split("_")[1]);
				RenderSystem.setShaderTexture(0, FaceTexture.ITEM_STAR);
				context.drawTexture(FaceTexture.ITEM_STAR, x, y, 0, 0, 3 * stars, 3, 3, 3);
				matrices.translate(0.0f, 0.0f, -300.0f);
			}
		}

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
		matrices.pop();

		if (hideItem && HIDDEN_ITEMS.contains(stack.getItem()))
			ci.cancel();
	}

	public static void postDrawItemSlot(DrawContext context, Slot slot) {
		if (!FaceConfig.General.onFaceLand)
			return;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void getFovMultiplierReturn(CallbackInfoReturnable<Float> cir) {

		if (!config.general.instantBowZoom)
			return;

		var player = instance.client.player;
		if (player == null || player.getActiveItem() == null)
			return;

		float f = 1.0F;

		ItemStack itemStack = player.getActiveItem();
		if (player.isUsingItem()) {
			if (itemStack.getItem() instanceof BowItem) {
				int i = player.getItemUseTime();
				float g = (float) i / 1.0F;
				g = (g > 1.0F ? 1.0F : g * g);
				f *= 1.0F - g * 0.061F;

				cir.setReturnValue(MathHelper.lerp(instance.client.options.getFovEffectScale().getValue().floatValue(), 1.0F, f));
			}
		}
	}
}
