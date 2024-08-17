package xyz.mashtoolz.utils;

import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import xyz.mashtoolz.enums.RarityColor;

public class ColorUtils {

	public static int hex2Int(String colorHex, int opacity) {
		if (colorHex.startsWith("#"))
			colorHex = colorHex.substring(1);
		return (opacity << 24) | Integer.parseInt(colorHex, 16);
	}

	public static float[] getRGB(TextColor color) {
		return new float[] {
				(color.getRgb() >> 16 & 255) / 255.0F,
				(color.getRgb() >> 8 & 255) / 255.0F,
				(color.getRgb() & 255) / 255.0F
		};
	}

	public static TextColor getItemColor(ItemStack stack) {
		if (stack.getItem().equals(Items.EMERALD)) {
			var pattern = Pattern.compile("\\b[IVXLCDM]+\\b");
			var matcher = pattern.matcher(stack.getName().getString());
			var tier = matcher.find() ? matcher.group() : "U";
			return RarityColor.fromTier(tier).getColor();
		}
		var siblings = stack.getName().getSiblings();
		return siblings.size() > 0 ? siblings.get(0).getStyle().getColor() : null;
	}
}
