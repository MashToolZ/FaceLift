package xyz.mashtoolz.helpers;

public class NumberFormatter {

	public static String format(int number) {
		if (number >= 100000000)
			return String.format("%.1fm", number / 1000000f);
		else if (number >= 1000000)
			return String.format("%.2fm", number / 1000000f);
		else if (number >= 100000)
			return String.format("%.1fk", number / 1000f);
		else if (number >= 10000)
			return String.format("%.2fk", number / 1000f);
		else if (number >= 1000)
			return String.format("%.1fk", number / 1000f);
		return String.valueOf(number);
	}
}
