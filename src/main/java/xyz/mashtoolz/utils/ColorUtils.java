package xyz.mashtoolz.utils;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Optional;

public class ColorUtils {

	public static int hex2Int(String colorHex, int opacity) {
		colorHex = colorHex.replace("#", "");
		if (colorHex.length() != 6)
			throw new IllegalArgumentException("Invalid hex color format.");
		return (opacity << 24) | Integer.parseInt(colorHex, 16);
	}

	public static float[] getRGB(TextColor color) {
		if (color == null)
			return new float[] { 0, 0, 0 };

		int rgb = color.getRgb();
		return new float[] {
				(rgb >> 16 & 255) / 255.0F,
				(rgb >> 8 & 255) / 255.0F,
				(rgb & 255) / 255.0F
		};
	}

	public static int getARGB(TextColor color, int opacity) {
		int rgb = color.getRgb(); // Gets the RGB value (0xRRGGBB)
		int alpha = opacity << 24;
		return alpha | rgb; // Combine alpha and RGB
	}

	public static String getTextColor(Text text) {
		String defaultColor = "#D1D1D1";

		return Optional.ofNullable(text)
				.flatMap(t -> t.getSiblings().stream().findFirst())
				.map(Text::getStyle)
				.map(Style::getColor)
				.map(TextColor::toString)
				.map(color -> {

					if (color.matches("#[0-9A-Fa-f]{6}"))
						return "<" + color + ">";

					Formatting formatting = Formatting.byName(color);
					return formatting != null ? formatting.toString() : defaultColor;
				})
				.orElse(defaultColor);
	}
}
