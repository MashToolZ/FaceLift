package xyz.mashtoolz.custom;

import net.minecraft.text.TextColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum FaceType {

	UNKNOWN("Unknown", "�", 0x000000),

	CRAFTED("Crafted", "俌", 0xFFD442),
	COMMON("Common", "丠", 0xFFFBF3, "I"),
	UNCOMMON("Uncommon", "丿", 0x2F88E1, "II"),
	RARE("Rare", "乛", 0xA63FC9, "III"),
	EPIC("Epic", "乜", 0xFC3D38, "IV"),
	UNIQUE("Unique", "乞", 0xF8842C),
	DIVINE("Divine", "九", 0xFFBFCA),

	PAWN_1("Pawn", "俍", 0xC48800),
	PAWN_2("Pawn", "懓", 0xCFCFCF),

	TRIBUTE("Tribute", "懚", 0x00A4CF),
	IDOL("Idol", "懛", 0xD070E7),

	SOCKET_GEM("Socket Gem", "丈", 0x359E20),
	SOUL_GEM("Soul Gem", "俊", 0xD070E7),

	ITEM("Item", "婰", 0x00A4CF),
	POTION("Potion", "下", 0xE77093),
	FOOD("Food", "Ը", 0x873B0C),

	TOME("Tome", "不", 0x136FCE),
	ESSENCE("Essence", "万", 0x30B6E1),
	EXTENDER("Extender", "三", 0x009E81),
	PURITY("Purity Scroll", "与", 0xE4E4E4),

	PET("Pet", "俈", 0x5DDE2E),
	MOUNT("Mount", "俉", 0x89482E),

	UNCOMMON_UPGRADE("Item Upgrade (Uncommon)", "俋", 0x3466C7),
	RARE_UPGRADE("Item Upgrade (Rare)", "丏", 0x8D3AB3),
	EPIC_UPGRADE("Item Upgrade (Epic)", "丌", 0xB91A2A),

	MATERIAL_1("Material", "丁", 0xECAD56),
	MATERIAL_2("Material", "丂", 0xECAD56),
	MATERIAL_3("Material", "七", 0xECAD56),
	MATERIAL_4("Material", "丄", 0xECAD56),
	MATERIAL_5("Material", "丅", 0xECAD56),

	COSMIC_QUARTER("Cosmic Quarter", "丢", 0x2E1E45);

	private final String text;
	private final String unicode;
	private final TextColor color;
	private final String tier;

	FaceType(String text, String unicode, int color) {
		this(text, unicode, color, "");
	}

	FaceType(String text, String unicode, int color, String tier) {
		this.text = text;
		this.unicode = unicode;
		this.color = TextColor.fromRgb(color);
		this.tier = tier;
	}

	public static FaceType fromUnicode(String unicode) {
		return Arrays.stream(values())
				.filter(type -> type.unicode.equals(unicode))
				.findFirst()
				.orElse(null);
	}

	public static FaceType fromColor(TextColor color) {
		return Arrays.stream(values())
				.filter(type -> type.color.equals(color))
				.findFirst()
				.orElse(null);
	}

	public static FaceType fromTier(String tier) {
		return Arrays.stream(values())
				.filter(type -> type.tier.equals(tier))
				.findFirst()
				.orElse(null);
	}

	public static FaceType fromName(String name) {
		return Arrays.stream(values())
				.filter(type -> type.toString().equals(name))
				.findFirst()
				.orElse(null);
	}

	public String getText() {
		return text;
	}

	public String getUnicode() {
		return unicode;
	}

	public TextColor getColor() {
		return color;
	}

	public String getString() {
		return this.toString();
	}

	public static Map<String, String> map() {
		Map<String, String> map = new HashMap<>();
		for (FaceType type : values())
			if (!type.equals(FaceType.UNKNOWN))
				map.put(type.getUnicode(), type.getText());
		return map;
	}
}
