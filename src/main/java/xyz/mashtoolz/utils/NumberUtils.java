package xyz.mashtoolz.utils;

public class NumberUtils {

	public static String format(int number) {
		if (number >= 100_000_000)
			return String.format("%.1fm", number / 1_000_000f);
		else if (number >= 1_000_000)
			return String.format("%.2fm", number / 1_000_000f);
		else if (number >= 100_000)
			return String.format("%.1fk", number / 1_000f);
		else if (number >= 10_000)
			return String.format("%.2fk", number / 1_000f);
		else if (number >= 1_000)
			return String.format("%.1fk", number / 1_000f);

		return Integer.toString(number);
	}

}
