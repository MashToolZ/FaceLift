package xyz.mashtoolz.utils;

import java.util.Map;

import net.minecraft.text.OrderedText;

public class TextUtils {

	public static String escapeStringToUnicode(String input, boolean removeUnicode) {
		StringBuilder builder = new StringBuilder(input.length());
		for (char ch : input.toCharArray()) {
			if (ch < 128)
				builder.append(ch);
			else if (!removeUnicode)
				builder.append("\\u").append(String.format("%04x", (int) ch));
		}
		return builder.toString();
	}

	public static String toString(OrderedText orderedText) {
		StringBuilder builder = new StringBuilder();

		orderedText.accept((index, style, codePoint) -> {
			builder.appendCodePoint(codePoint);
			return true;
		});

		return builder.toString();
	}

	public static <T> void replaceAll(StringBuilder builder, Map<String, T> replacements) {
		for (var entry : replacements.entrySet()) {
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			int index;
			while ((index = builder.indexOf(key)) != -1)
				builder.replace(index, index + key.length(), value);
		}
	}
}
