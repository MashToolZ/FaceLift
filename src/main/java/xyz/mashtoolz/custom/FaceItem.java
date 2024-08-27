package xyz.mashtoolz.custom;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.helpers.HudRenderer;

public class FaceItem {

	private static FaceLift instance = FaceLift.getInstance();
	private static MinecraftClient client = instance.client;

	private ItemStack stack;
	private String name;
	private String tooltip;
	private FaceRarity rarity;

	public FaceItem(ItemStack stack) {

		this.stack = stack;
		this.name = stack.getName().getString();
		this.tooltip = parseTooltip(Screen.getTooltipFromItem(client, stack));
		for (FaceRarity rarity : FaceRarity.values()) {
			if (rarity.getName().equals("UNKNOWN"))
				continue;

			if (tooltip.contains(rarity.getUnicode())) {
				this.rarity = rarity;
				break;
			}
		}

		if (this.rarity == null)
			this.rarity = FaceRarity.UNKNOWN;
	}

	public ItemStack getStack() {
		return stack;
	}

	public String getName() {
		return name;
	}

	public String getTooltip() {
		return tooltip;
	}

	public FaceRarity getRarity() {
		return rarity;
	}

	public TextColor getColor() {
		var color = rarity.getColor();
		switch (rarity) {
			case SOCKET_GEM:
				var pattern = Pattern.compile("\\b[IV]+\\b");
				var matcher = pattern.matcher(stack.getName().getString());
				var _rarity = matcher.find() ? FaceRarity.fromTier(matcher.group()) : FaceRarity.UNIQUE;
				return _rarity.getColor();

			default:
				return color;
		}
	}

	private String parseTooltip(List<Text> tooltip) {

		if (tooltip.size() < 2)
			return "";

		var text = tooltip
				.subList(1, tooltip.size() - 1)
				.stream().map(Text::getString)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining("\n"))
				.replace("", "")
				.replace("乚", " ");

		char start = 'a';
		for (int i = 0; i < 26; i++) {
			text = text.replace(FaceCode.list1[i], String.valueOf((char) (start + i)));
			text = text.replace(FaceCode.list2[i], String.valueOf((char) (start + i)));
			text = text.replace(FaceCode.list3[i], String.valueOf((char) (start + i)));
		}

		for (var code : FaceCode.values())
			text = text.replace(code.getUnicode(), code.getText());

		return text;
	}

	public static JsonObject getItemData(ItemStack stack) {
		var item = stack.getItem();
		if (stack.isEmpty() || HudRenderer.IGNORED_ITEMS.contains(item) || HudRenderer.ABILITY_ITEMS.contains(item))
			return null;

		var nbt = stack.getNbt();
		if (nbt == null || !nbt.contains("PublicBukkitValues"))
			return null;

		var publicValues = nbt.getCompound("PublicBukkitValues");
		var itemData = publicValues.getString("loot:loot.item_data");
		if (itemData.length() <= 2)
			return null;

		var jsonObject = JsonParser.parseString(itemData).getAsJsonObject();
		var stringData = jsonObject.getAsJsonObject("stringData");
		return stringData;
	}
}
