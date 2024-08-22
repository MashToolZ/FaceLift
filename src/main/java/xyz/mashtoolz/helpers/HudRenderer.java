package xyz.mashtoolz.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import xyz.mashtoolz.FaceLift;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HudRenderer {

	private static final FaceLift instance = FaceLift.getInstance();

	private static Config config = instance.config;

	private static SearchFieldWidget searchBar;
	private static DropDownMenu dropdown;

	private static final Identifier ITEM_GLOW = new Identifier("facelift", "textures/gui/item_glow.png");
	private static final Identifier ITEM_STAR = new Identifier("facelift", "textures/gui/item_star.png");

	public static final ArrayList<Item> ABILITY_ITEMS = new ArrayList<>(Arrays.asList(Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE));
	private static final ArrayList<Item> IGNORED_ITEMS = new ArrayList<>(Arrays.asList(Items.BARRIER, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.PLAYER_HEAD, Items.BARRIER));
	private static final ArrayList<Item> HIDDEN_ITEMS = new ArrayList<>(Arrays.asList(Items.SHIELD, Items.TRIPWIRE_HOOK));

	public static void onHudRender(DrawContext context, float delta) {

		if (!config.onFaceLand)
			return;

		context.getMatrices().push();

		if (config.combatTimer.enabled)
			CombatTimer.draw(context);

		if (config.dpsMeter.enabled)
			DPSMeter.draw(context);

		if (config.xpDisplay.enabled)
			XPDisplay.draw(context);

		if (config.arenaTimer.enabled) {
			ArenaTimer.updateTimer(context);
			ArenaTimer.draw(context);
		}

		context.getMatrices().pop();
	}

	public static void afterInitScreen(MinecraftClient client, Screen screen, int width, int height) {

		if (!config.onFaceLand)
			return;

		if (screen instanceof HandledScreen) {

			var inventory = config.inventory;
			searchBar = new SearchFieldWidget(client.textRenderer, width / 2 - 90, height - 25, 180, 20, searchBar, Text.literal(inventory.searchbar.query));
			searchBar.setChangedListener(text -> {
				inventory.searchbar.query = text;
				config.save();
			});

			if (inventory.searchbar.highlight) {
				searchBar.highlighted = true;
				searchBar.setEditableColor(0xFFFF78);
			}

			dropdown = new DropDownMenu(screen, "Options", width / 2 + 95, height - 25, 90, 20, true);

			dropdown.addButton(" Case: " + inventory.searchbar.caseSensitive, button -> {
				inventory.searchbar.caseSensitive = !inventory.searchbar.caseSensitive;
				config.save();
				button.setMessage(Text.literal(" Case: " + inventory.searchbar.caseSensitive));
			}, inventory.searchbar.caseSensitive);

			dropdown.addButton("Regex: " + inventory.searchbar.regex, button -> {
				inventory.searchbar.regex = !inventory.searchbar.regex;
				config.save();
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

	public static void preDrawHotbarItemSlot(DrawContext context, ItemStack stack, int x, int y, CallbackInfo ci) {

		if (!ABILITY_ITEMS.contains(stack.getItem()))
			return;

		var matrices = context.getMatrices();

		matrices.push();
		matrices.translate(0.0f, 0.0f, 300.0f);

		var toggled = stack.getEnchantments().size() > 0;
		if (toggled)
			context.drawBorder(x, y, 16, 16, ColorUtils.hex2Int("#FF0000", 0xFF));

		matrices.translate(0.0f, 0.0f, -300.0f);
		matrices.pop();
	}

	public static void preDrawItemSlot(DrawContext context, Slot slot, CallbackInfo ci) {

		if (!config.onFaceLand)
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
			RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], hideItem ? 0.25F : config.inventory.rarityOpacity);
			context.drawTexture(ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);
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
		if (!config.onFaceLand)
			return;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
