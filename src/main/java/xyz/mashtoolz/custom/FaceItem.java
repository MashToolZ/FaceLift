package xyz.mashtoolz.custom;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceFont.FFont;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.utils.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class FaceItem {

	private static final FaceLift INSTANCE = FaceLift.getInstance();

	private static final ConcurrentHashMap<ItemStack, FaceItem> CACHE = new ConcurrentHashMap<>();
	private static final List<FaceSlot> TOOL_SLOTS = Arrays.asList(FaceSlot.PICKAXE, FaceSlot.WOODCUTTINGAXE, FaceSlot.HOE);
	private static final Pattern TIER_PATTERN = Pattern.compile(".*\\b([IV]+)\\b$");

	private final ItemStack stack;
	private final String name;
	private String tooltip = null;
	private String _tooltip = null;
	private FaceType type = null;
	private FaceTool tool = null;

	private int remaining = 0;
	private int max = 0;

	public FaceItem(ItemStack stack) {
		this.stack = stack;
		this.name = stack.getName().getString();
		this.updatePotionValues();
	}

	public static void clearCache() {
		if (CACHE.isEmpty())
			return;
		CACHE.clear();
	}

	public static FaceItem from(ItemStack stack) {
		return CACHE.computeIfAbsent(stack, FaceItem::new);
	}

	public boolean isInvalid() {
		return getFaceType().equals(FaceType.UNKNOWN);
	}

	public boolean isPotion() {
		return max != 0;
	}

	public String getName() {
		return name;
	}

	public String getTooltip() {
		return getTooltip(false);
	}

	public String getTooltip(boolean forceRefresh) {
		return getTooltip(false, forceRefresh);
	}

	private String getTooltip(boolean noParse, boolean forceRefresh) {
		if (forceRefresh || (tooltip == null && !noParse))
			tooltip = parseTooltip(Screen.getTooltipFromItem(INSTANCE.CLIENT, stack), false);
		if (forceRefresh || (_tooltip == null && noParse))
			_tooltip = parseTooltip(Screen.getTooltipFromItem(INSTANCE.CLIENT, stack), true);
		return noParse ? _tooltip : tooltip;
	}

	public FaceType getFaceType() {
		if (type == null) {
			var tooltipStyle = stack.getComponents().get(DataComponentTypes.TOOLTIP_STYLE);
			type = Arrays.stream(FaceType.values())
					.filter(r -> !r.equals(FaceType.UNKNOWN) && getTooltip(true, false).contains(r.getUnicode()))
					.findFirst()
					.orElse(FaceType.UNKNOWN);
			if (getTooltip(true, false).contains(FaceType.UNIQUE.getUnicode()) || type == FaceType.UNKNOWN && tooltipStyle != null) {
				type = Arrays.stream(FaceType.values())
						.filter(r -> tooltipStyle.toString().split("_")[1].equals(r.getText().toLowerCase()))
						.findFirst()
						.orElse(FaceType.UNKNOWN);
			}
		}
		return type;
	}

	public FaceTool getFaceTool() {
		if (tool == FaceTool.BEDROCK || tool == null)
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
			var determinedType = matcher.find() && matcher.groupCount() == 1 ? FaceType.fromTier(matcher.group(1))
					: FaceType.UNIQUE;
			if (determinedType == null)
				return color;
			return determinedType.getColor();
		}
		return color;
	}

	private void updatePotionValues() {
		var pattern = Pattern.compile("Uses: \\((\\d+)/(\\d+)\\)", Pattern.DOTALL);
		var matcher = pattern.matcher(getTooltip(true));
		if (!matcher.find() || matcher.groupCount() != 2)
			return;

		remaining = Integer.parseInt(matcher.group(1));
		max = Integer.parseInt(matcher.group(2));
	}

	private String parseTooltip(List<Text> tooltip, boolean noReplace) {

		if (tooltip.size() < 2)
			return "";

		StringBuilder textBuilder = new StringBuilder(512);
		tooltip.subList(1, tooltip.size() - 1).stream()
				.map(Text::getString)
				.filter(s -> !s.isBlank())
				.forEach(s -> textBuilder.append(s).append("\n"));

		if (noReplace)
			return textBuilder.toString();

		TextUtils.replaceAll(textBuilder, FaceFont.get(FType.ITEM_TOOLTIP));
		TextUtils.replaceAll(textBuilder, FaceType.map());

		StringBuilder gemBuilder = new StringBuilder("\n");
		int totalSlots = 0;
		int size = FFont.GEM_SLOTS.getUnicodes().length;

		for (int i = 0; i < size; i++) {
			String unicode = FFont.GEM_SLOTS.getUnicode(i);
			String slotText = FFont.GEM_SLOTS.getText(i);
			int matches = TextUtils.countMatches(textBuilder, unicode);

			if (matches > 0) {
				TextUtils.replaceAll(textBuilder, unicode, "");
				gemBuilder.append(slotText).append("_Slots=").append(matches).append("\n");
				totalSlots += matches;
			}
		}

		gemBuilder.append("Total_Slots=").append(totalSlots);
		return textBuilder.append(gemBuilder).toString();
	}
}
