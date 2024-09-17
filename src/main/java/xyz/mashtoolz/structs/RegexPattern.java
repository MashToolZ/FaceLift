package xyz.mashtoolz.structs;

import java.util.regex.Pattern;

public class RegexPattern {

	private final String key;
	private final Pattern pattern;

	public RegexPattern(String key, String regex) {
		this.key = key;
		this.pattern = Pattern.compile(regex);
	}

	public String getKey() {
		return key;
	}

	public Pattern getPattern() {
		return pattern;
	}
}