package xyz.mashtoolz.custom;

import java.util.Arrays;
import net.minecraft.text.TextColor;

public enum FaceRarity {

	UNKNOWN(0x000000, "", ""),
	CRAFTED(0xFFD442, "俌", ""),
	COMMON(0xFFFBF3, "丠", "I"),
	UNCOMMON(0x2F88E1, "丿", "II"),
	RARE(0xA63FC9, "乛", "III"),
	EPIC(0xFC3D38, "乜", "IV"),
	DIVINE(0xFFBFCA, "九", ""),
	UNIQUE(0xF8842C, "乞", ""),
	PAWN_1(0xC48800, "俍", ""),
	PAWN_2(0xCFCFCF, "懓", ""),
	TRIBUTE(0x008EB4, "懚", ""),
	IDOL(0xB561C8, "懛", ""),
	SOCKET_GEM(0x2E891C, "丈", ""),
	ITEM(0x008EB4, "婰", ""),
	ESSENCE(0x2A9EC3, "万", ""),
	EXTENDER(0x008970, "三", ""),
	SCROLL(0x6A58BB, "上", ""),
	POTION(0xC86180, "下", ""),
	TOME(0x1060B3, "不", ""),
	PURITY(0xC6C6C6, "与", ""),
	PET(0x51C128, "俈", ""),
	MOUNT(0x773E28, "俉", ""),
	SOUL_GEM(0xB561C8, "俊", ""),
	UNCOMMON_UPGRADE(0x2D59AD, "俋", ""),
	RARE_UPGRADE(0x7A329B, "丏", ""),
	EPIC_UPGRADE(0xA01724, "丌", ""),
	MATERIAL_1(0xE5AB55, "丁", ""),
	MATERIAL_2(0xE5AB55, "丂", ""),
	MATERIAL_3(0xE5AB55, "七", ""),
	MATERIAL_4(0xE5AB55, "丄", ""),
	MATERIAL_5(0xE5AB55, "丅", "");

	private final TextColor color;
	private final String unicode;
	private final String tier;

	private FaceRarity(int color, String unicode, String tier) {
		this.color = TextColor.fromRgb(color);
		this.unicode = unicode;
		this.tier = tier;
	}

	public static FaceRarity fromColor(TextColor color) {
		return Arrays.stream(values())
				.filter(rarity -> rarity.color.equals(color))
				.findFirst()
				.orElse(null);
	}

	public static FaceRarity fromUnicode(String unicode) {
		return Arrays.stream(values())
				.filter(rarity -> rarity.unicode.equals(unicode))
				.findFirst()
				.orElse(null);
	}

	public static FaceRarity fromTier(String tier) {
		return Arrays.stream(values())
				.filter(rarity -> rarity.tier.equals(tier))
				.findFirst()
				.orElse(null);
	}

	public static FaceRarity fromName(String name) {
		return Arrays.stream(values())
				.filter(rarity -> rarity.toString().equals(name))
				.findFirst()
				.orElse(null);
	}

	public TextColor getColor() {
		return color;
	}

	public String getUnicode() {
		return unicode;
	}

	public String getTier() {
		return tier;
	}

	public String getString() {
		return this.toString();
	}
}
