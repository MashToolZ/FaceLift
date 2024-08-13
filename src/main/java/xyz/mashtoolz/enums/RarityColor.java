package xyz.mashtoolz.enums;

import net.minecraft.text.TextColor;

public enum RarityColor {

	UNKNOWN(TextColor.fromRgb(0x8C8C8C), ""),
	COMMON(TextColor.fromRgb(0xEEECE9), "I"),
	UNCOMMON(TextColor.fromRgb(0x2F88E1), "II"),
	RARE(TextColor.fromRgb(0xA63FC9), "III"),
	EPIC(TextColor.fromRgb(0xFC3D38), "IV"),
	UNIQUE(TextColor.fromRgb(0xF8842C), "U");

	private final TextColor color;
	private final String tier;

	RarityColor(TextColor color, String tier) {
		this.color = color;
		this.tier = tier;
	}

	public static RarityColor fromTier(String tier) {
		for (RarityColor rarity : RarityColor.values())
			if (rarity.getTier().equals(tier))
				return rarity;
		return RarityColor.UNKNOWN;
	}

	public static RarityColor fromRarity(String rarity) {
		for (RarityColor color : RarityColor.values())
			if (color.getRarity().equals(rarity))
				return color;
		return RarityColor.UNKNOWN;
	}

	public String getRarity() {
		return this.name();
	}

	public TextColor getColor() {
		return this.color;
	}

	public String getTier() {
		return this.tier;
	}
}
