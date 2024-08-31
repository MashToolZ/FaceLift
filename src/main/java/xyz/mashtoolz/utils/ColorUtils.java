package xyz.mashtoolz.utils;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

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

	public static String getTextColor(Text text) {

		var defaultColor = "#D1D1D1";

		if (text == null || text.getSiblings() == null || text.getSiblings().isEmpty())
			return defaultColor;

		Text sibling = text.getSiblings().get(0);
		if (sibling == null || sibling.getStyle() == null)
			return defaultColor;

		TextColor textColor = sibling.getStyle().getColor();
		if (textColor != null) {
			String color = textColor.toString();
			if (color.matches("#[0-9A-Fa-f]{6}"))
				return "<" + color + ">";
			else {
				var formatting = Formatting.byName(color);
				return formatting != null ? formatting.toString() : defaultColor;
			}
		}

		return defaultColor;
	}
}
