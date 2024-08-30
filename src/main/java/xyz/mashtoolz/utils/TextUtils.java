package xyz.mashtoolz.utils;

import net.minecraft.text.OrderedText;

public class TextUtils {

	public static String escapeStringToUnicode(String input, boolean removeUnicode) {
		StringBuilder builder = new StringBuilder();
		for (char ch : input.toCharArray()) {
			if (ch < 128) {
				builder.append(ch);
			} else if (!removeUnicode) {
				builder.append(String.format("\\u%04x", (int) ch));
			}
		}
		return builder.toString();
	}

	public static String toString(OrderedText orderedText) {
		StringBuilder builder = new StringBuilder();

		orderedText.accept((index, style, codePoint) -> {
			builder.append(Character.toChars(codePoint));
			return true;
		});

		return builder.toString();
	}
}
