package xyz.mashtoolz.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import xyz.mashtoolz.FaceLift;

public class FaceItem {

	private static FaceLift instance = FaceLift.getInstance();

	private ItemStack stack;
	private String name;
	private String tooltip;
	private FaceRarity rarity;
	private FaceTool tool = null;
	public boolean invalid;

	private List<FaceSlot> DUALWIELD_SLOTS = new ArrayList<FaceSlot>(Arrays.asList(FaceSlot.MAINHAND, FaceSlot.OFFHAND));
	private List<FaceSlot> TOOL_SLOTS = new ArrayList<FaceSlot>(Arrays.asList(FaceSlot.PICKAXE, FaceSlot.WOODCUTTINGAXE, FaceSlot.HOE));

	public FaceItem(ItemStack stack) {
		this.stack = stack;
		this.name = stack.getName().getString();
		this.tooltip = parseTooltip(Screen.getTooltipFromItem(instance.client, stack));
		for (FaceRarity rarity : FaceRarity.values()) {
			if (rarity.getString().equals("UNKNOWN"))
				continue;

			if (tooltip.contains(rarity.getUnicode())) {
				this.rarity = rarity;
				break;
			}
		}

		if (this.rarity == null)
			this.rarity = FaceRarity.UNKNOWN;

		for (var tool : FaceTool.values())
			if (tooltip.contains(tool.getFaceToolType().getName()))
				this.tool = tool;

		this.invalid = this.rarity.equals(FaceRarity.UNKNOWN);
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

	public FaceRarity getFaceRarity() {
		return rarity;
	}

	public FaceTool getFaceTool() {
		return tool;
	}

	public FaceSlot getFaceSlot(boolean isDualWielding) {
		var shiftDown = Screen.hasShiftDown();

		if (isDualWielding)
			return DUALWIELD_SLOTS.get(shiftDown ? 1 : 0);

		if (tool != null) {
			return TOOL_SLOTS.get(tool.getFaceToolType().ordinal());
		}

		var id = Registries.ITEM.getId(stack.getItem()).toString();
		return FaceEquipment.getSlot(id, shiftDown);
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

		char start = 'A';
		for (int i = 0; i < 26; i++) {
			text = text.replace(FaceCode.list1[i], String.valueOf((char) (start + i)));
			text = text.replace(FaceCode.list2[i], String.valueOf((char) (start + i)));
			text = text.replace(FaceCode.list3[i], String.valueOf((char) (start + i)));
		}

		for (var code : FaceCode.values())
			text = text.replace(code.getUnicode(), code.getText());

		return text;
	}
}
