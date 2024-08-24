package xyz.mashtoolz.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceRarity;
import xyz.mashtoolz.mixins.ScreenInterface;
import xyz.mashtoolz.utils.ColorUtils;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HudRenderer {

	private static SearchFieldWidget searchBar;
	private static DropDownMenu dropdown;

	private static final Identifier ITEM_GLOW = new Identifier("facelift", "textures/gui/item_glow.png");
	private static final Identifier ITEM_STAR = new Identifier("facelift", "textures/gui/item_star.png");
	private static final Identifier ABILITY_GLINT = new Identifier("facelift", "textures/gui/ability_glint.png");
	private static int glintFrame = 0;
	private static long glintTime = System.currentTimeMillis();

	public static final ArrayList<Item> ABILITY_ITEMS = new ArrayList<>(
			Arrays.asList(Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE));
	private static final ArrayList<Item> IGNORED_ITEMS = new ArrayList<>(Arrays.asList(Items.BARRIER,
			Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.PLAYER_HEAD, Items.BARRIER));
	private static final ArrayList<Item> HIDDEN_ITEMS = new ArrayList<>(
			Arrays.asList(Items.SHIELD, Items.TRIPWIRE_HOOK));

	public static void onHudRender(DrawContext context, float delta) {

		if (!Config.onFaceLand)
			return;

		context.getMatrices().push();

		if (Config.combatTimer.enabled)
			CombatTimer.draw(context);

		if (Config.dpsMeter.enabled)
			DPSMeter.draw(context);

		if (Config.xpDisplay.enabled)
			XPDisplay.draw(context);

		if (Config.arenaTimer.enabled) {
			ArenaTimer.updateTimer(context);
			ArenaTimer.draw(context);
		}

		context.getMatrices().pop();
	}

	public static void afterInitScreen(MinecraftClient client, Screen screen, int width, int height) {

		if (!Config.onFaceLand)
			return;

		if (screen instanceof HandledScreen) {

			var inventory = Config.inventory;
			searchBar = new SearchFieldWidget(client.textRenderer, width / 2 - 90, height - 25, 180, 20, searchBar,
					Text.literal(inventory.searchbar.query));
			searchBar.setChangedListener(text -> {
				inventory.searchbar.query = text;
				Config.save();
			});

			if (inventory.searchbar.highlight) {
				searchBar.highlighted = true;
				searchBar.setEditableColor(0xFFFF78);
			}

			dropdown = new DropDownMenu(screen, "Options", width / 2 + 95, height - 25, 90, 20, true);

			dropdown.addButton(" Case: " + inventory.searchbar.caseSensitive, button -> {
				inventory.searchbar.caseSensitive = !inventory.searchbar.caseSensitive;
				Config.save();
				button.setMessage(Text.literal(" Case: " + inventory.searchbar.caseSensitive));
			}, inventory.searchbar.caseSensitive);

			dropdown.addButton("Regex: " + inventory.searchbar.regex, button -> {
				inventory.searchbar.regex = !inventory.searchbar.regex;
				Config.save();
				button.setMessage(Text.literal("Regex: " + inventory.searchbar.regex));
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
			if (!Config.inventory.searchbar.caseSensitive) {
				name = name.toLowerCase();
				query = query.toLowerCase();
				tooltip = tooltip.toLowerCase();
			}

			if (query.length() == 0 || name.toLowerCase().equals("air"))
				hideItem = true;

			if (Config.inventory.searchbar.regex) {
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

			RenderSystem.setShaderTexture(0, ABILITY_GLINT);
			RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
			context.drawTexture(ABILITY_GLINT, x + 1, y + 1, 14, 14, 0 | (glintFrame * size), 0, size, size, maxFrames * size, size);

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			context.drawTexture(ABILITY_GLINT, x, y, 16, 16, 0 | (glintFrame * size), 0, size, size, maxFrames * size, size);

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

		if (!Config.onFaceLand)
			return;

		ItemStack stack = slot.getStack();
		if ((stack.isEmpty() || IGNORED_ITEMS.contains(stack.getItem())) || ABILITY_ITEMS.contains(stack.getItem()))
			return;

		int x = slot.x, y = slot.y;
		FaceItem item = new FaceItem(stack);

		boolean hideItem = searchbarCheck(item);
		var rarity = item.getRarity();
		var color = item.getColor();

		MatrixStack matrices = context.getMatrices();
		matrices.push();

		if (color != null && !rarity.equals(FaceRarity.UNKNOWN)) {
			float[] rgb = ColorUtils.getRGB(color);

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			matrices.translate(0.0f, 0.0f, 100.0f);
			RenderSystem.setShaderTexture(0, ITEM_GLOW);
			RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], hideItem ? 0.25F : Config.inventory.rarityOpacity);
			if (Config.inventory.rarityTexture)
				context.drawTexture(ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);
			else
				context.drawBorder(x, y, 16, 16, ColorUtils.hex2Int("#FFFFFF", 0xFF));
			matrices.translate(0.0f, 0.0f, -100.0f);

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);

			if (rarity.getName().startsWith("MATERIAL_")) {
				matrices.translate(0.0f, 0.0f, 300.0f);
				var stars = Integer.parseInt(rarity.getName().split("_")[1]);
				RenderSystem.setShaderTexture(0, ITEM_STAR);
				context.drawTexture(ITEM_STAR, x, y, 0, 0, 3 * stars, 3, 3, 3);
				matrices.translate(0.0f, 0.0f, -300.0f);
			}

			RenderSystem.disableBlend();
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);

		matrices.pop();

		if (hideItem && HIDDEN_ITEMS.contains(stack.getItem()))
			ci.cancel();
	}

	public static void postDrawItemSlot(DrawContext context, Slot slot) {
		if (!Config.onFaceLand)
			return;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
