package xyz.mashtoolz.custom;

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
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.utils.TextUtils;

public class FaceItem {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static final List<FaceSlot> TOOL_SLOTS = Arrays.asList(FaceSlot.PICKAXE, FaceSlot.WOODCUTTINGAXE, FaceSlot.HOE);
	private static final Pattern TIER_PATTERN = Pattern.compile("([IV]+)");

	private final ItemStack stack;
	private final String name;
	private final String tooltip;
	private final FaceRarity rarity;
	private FaceTool tool;

	public FaceItem(ItemStack stack, boolean ignoreExtras) {
		this.stack = stack;
		this.name = stack.getName().getString();
		this.tooltip = parseTooltip(Screen.getTooltipFromItem(INSTANCE.CLIENT, stack));
		this.rarity = FaceRarity.UNKNOWN;
		this.tool = null;
	}

	public FaceItem(ItemStack stack) {
		this.stack = stack;
		this.name = stack.getName().getString();
		this.tooltip = parseTooltip(Screen.getTooltipFromItem(INSTANCE.CLIENT, stack));

		this.rarity = Arrays.stream(FaceRarity.values())
				.filter(r -> !r.getString().equals("UNKNOWN") && tooltip.contains(r.getUnicode()))
				.findFirst()
				.orElse(FaceRarity.UNKNOWN);

		this.tool = Arrays.stream(FaceTool.values())
				.filter(t -> tooltip.contains(t.getFaceToolType().getName()))
				.findFirst()
				.orElse(null);
	}

	public boolean isInvalid() {
		return this.rarity.equals(FaceRarity.UNKNOWN);
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

	public FaceSlot getFaceSlot() {
		var shiftDown = Screen.hasShiftDown();

		if (tool != null)
			return TOOL_SLOTS.get(tool.getFaceToolType().ordinal());

		var id = Registries.ITEM.getId(stack.getItem()).toString();
		return FaceEquipment.getSlot(id, shiftDown);
	}

	public TextColor getColor() {
		var color = rarity.getColor();
		if (rarity.equals(FaceRarity.SOCKET_GEM)) {
			var name = stack.getName().getString();
			var matcher = TIER_PATTERN.matcher(name);
			var determinedRarity = matcher.find() ? FaceRarity.fromTier(matcher.group()) : FaceRarity.UNIQUE;
			return determinedRarity.getColor();
		}

		return color;
	}

	private String parseTooltip(List<Text> tooltip) {

		if (tooltip.size() < 2)
			return "";

		var textBuilder = new StringBuilder(tooltip.subList(1, tooltip.size() - 1)
				.stream()
				.map(Text::getString)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining("\n")));

		TextUtils.replaceAll(textBuilder, FaceFont.get(FType.ITEM_TOOLTIP));

		return textBuilder.toString();
	}
}
