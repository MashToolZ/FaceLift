package xyz.mashtoolz.utils;

import java.util.Map;

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

	public static void replaceAll(StringBuilder builder, String key, String value) {
		int index;
		while ((index = builder.indexOf(key)) != -1)
			builder.replace(index, index + key.length(), value);
	}

	public static <T> void replaceAll(StringBuilder builder, Map<String, T> replacements) {
		replaceAll(builder, replacements, "", "");
	}

	public static <T> void replaceAll(StringBuilder builder, Map<String, T> replacements, String prefix, String suffix) {
		for (var entry : replacements.entrySet()) {
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			int index;
			while ((index = builder.indexOf(key)) != -1)
				builder.replace(index, index + key.length(), prefix + value + suffix);
		}
	}

	public static int countMatches(StringBuilder builder, String key) {
		int matches = 0;
		int index = 0;
		while ((index = builder.indexOf(key, index)) != -1) {
			matches++;
			index += key.length();
		}
		return matches;
	}

}
