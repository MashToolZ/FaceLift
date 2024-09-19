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
import xyz.mashtoolz.custom.FaceFont.FFont;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.utils.TextUtils;

public class FaceItem {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static final List<FaceSlot> TOOL_SLOTS = Arrays.asList(FaceSlot.PICKAXE, FaceSlot.WOODCUTTINGAXE, FaceSlot.HOE);
	private static final Pattern TIER_PATTERN = Pattern.compile(".*\\b([IV]+)\\b$");

	private final ItemStack stack;
	private final String name;
	private String tooltip = null;
	private FaceType type = null;
	private FaceTool tool = null;

	public FaceItem(ItemStack stack) {
		this.stack = stack;
		this.name = stack.getName().getString();
	}

	public boolean isInvalid() {
		return getFaceType().equals(FaceType.UNKNOWN);
	}

	public ItemStack getStack() {
		return stack;
	}

	public String getName() {
		return name;
	}

	public String getTooltip() {
		return getTooltip(false);
	}

	private String getTooltip(boolean noParse) {
		if (noParse)
			return parseTooltip(Screen.getTooltipFromItem(INSTANCE.CLIENT, stack), true);
		if (tooltip == null)
			tooltip = parseTooltip(Screen.getTooltipFromItem(INSTANCE.CLIENT, stack), false);
		return tooltip;
	}

	public FaceType getFaceType() {
		if (type == null)
			type = Arrays.stream(FaceType.values())
					.filter(r -> !r.equals(FaceType.UNKNOWN) && getTooltip(true).contains(r.getUnicode()))
					.findFirst()
					.orElse(FaceType.UNKNOWN);
		return type;
	}

	public FaceTool getFaceTool() {
		if (tool == FaceTool.BEDROCK)
			tool = Arrays.stream(FaceTool.values())
					.filter(t -> getTooltip().contains(t.getFaceToolType().getName()))
					.findFirst()
					.orElse(null);
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
		var type = getFaceType();
		var color = type.getColor();
		if (type.equals(FaceType.SOCKET_GEM)) {
			var name = stack.getName().getString();
			var matcher = TIER_PATTERN.matcher(name);
			var determinedType = matcher.find() && matcher.groupCount() == 1 ? FaceType.fromTier(matcher.group(1)) : FaceType.UNIQUE;
			if (determinedType == null)
				return color;
			return determinedType.getColor();
		}
		return color;
	}

	private String parseTooltip(List<Text> tooltip, boolean noReplace) {

		if (tooltip.size() < 2)
			return "";

		var textBuilder = new StringBuilder(tooltip.subList(1, tooltip.size() - 1).stream().map(Text::getString).filter(s -> !s.isBlank()).collect(Collectors.joining("\n")));
		if (noReplace)
			return textBuilder.toString();

		TextUtils.replaceAll(textBuilder, FaceFont.get(FType.ITEM_TOOLTIP));
		TextUtils.replaceAll(textBuilder, FaceType.map());

		var gemBuilder = new StringBuilder("\n");
		int totalSlots = 0;
		for (int i = 0; i < FFont.GEM_SLOTS.getUnicodes().length; i++) {
			var unicode = FFont.GEM_SLOTS.getUnicode(i);
			if (textBuilder.indexOf(unicode) == -1)
				continue;
			var text = FFont.GEM_SLOTS.getText(i);
			var matches = TextUtils.countMatches(textBuilder, unicode);
			TextUtils.replaceAll(textBuilder, unicode, "");
			gemBuilder.append(text + "_Slots" + "=" + matches).append("\n");
			totalSlots += matches;
		}
		gemBuilder.append("Total_Slots=" + totalSlots);

		return textBuilder.append(gemBuilder).toString();
	}
}
