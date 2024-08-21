package xyz.mashtoolz.utils;

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
}
