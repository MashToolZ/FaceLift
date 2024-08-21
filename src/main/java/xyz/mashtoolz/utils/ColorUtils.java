package xyz.mashtoolz.utils;

import net.minecraft.text.TextColor;

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
}
