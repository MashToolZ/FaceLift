package xyz.mashtoolz.custom;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class FaceFont {

	private static final Map<FType, Map<String, String>> MAPS = new HashMap<>();

	public enum FType {
		DAMAGE_NUMBERS,
		ITEM_TOOLTIP,
		ACTION,
		HURT_TIME,
		TOAST,
		MOB_TAG,
		CHAT_TAG
	}

	static {
		for (var type : FType.values()) {
			var map = new HashMap<String, String>();
			for (var font : FFont.values()) {
				if (font.getType() != type)
					continue;

				var name = font.name();
				for (int i = 0; i < font.getUnicodes().length; i++) {
					var unicode = font.getUnicode(i);
					var text = font.getText(i);
					switch (name) {
						case "UPGRADE_NUMBERS" -> map.put(unicode, String.format("+%d", i + 1));
						case "GEM_SLOTS" -> {
						}
						default -> map.put(unicode, text);
					}
				}
			}
			MAPS.put(type, map);
		}
	}

	private static final String TT_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ";
	private static final String NAME_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ'";

	public enum FFont {

		DAMAGE_NUMBERS(FType.DAMAGE_NUMBERS, "１２３４５６７８９０", "1234567890", true),

		CHARS_1(FType.ITEM_TOOLTIP, "乀乁乂乃乄久乆乇么义乊之乌乍乎乏乐乑乒乓乔乕乖乗乘乙乚", TT_LETTERS, true),
		CHARS_2(FType.ITEM_TOOLTIP, "倀倁倂倃倄倅倆倇倈倉倊個倌倍倎倏倖倗倘候倚倛倜倝倞借俿", TT_LETTERS, true),
		CHARS_3(FType.ITEM_TOOLTIP, "俐俑俒俓俔俕俖俗俘俙俚俛俜保俞俟俠信俢俣俤俥俦俧俨俩俏", NAME_LETTERS, true),
		CHARS_4(FType.ITEM_TOOLTIP, "加务劢劣劤劥劦劧动助努劫劬劭劮劯劰励劲劳労劵劶劷劸効婱", NAME_LETTERS, true),
		UPGRADE_NUMBERS(FType.ITEM_TOOLTIP, "丰丱串丳临丵丶丷丸丹为主丼丽举", null, true),
		SPACERS(FType.ITEM_TOOLTIP, "", "", true),

		DISTORTED(FType.ITEM_TOOLTIP, "俾", "Distorted"),
		ENCHANTABLE(FType.ITEM_TOOLTIP, "严", "Enchantable"),
		SOULBOUND(FType.ITEM_TOOLTIP, "俎", "Soulbound"),
		GEM_SLOTS(FType.ITEM_TOOLTIP, "並丧丨丩个丫丬中丮丯", new String[] { "Open", "Locked", "Filled" }),

		REQUIREMENT_NOT_MET(FType.ACTION, "᳤", "Requirement not met"),
		NO_TARGET(FType.ACTION, "᳢", "No Target"),
		LOW_ENERGY(FType.ACTION, "᳣", "Low Energy"),
		LOW_ENERGY_JEB(FType.ACTION, "砣", "Low Energy"),
		ON_COOLDOWN(FType.ACTION, "᳥", "On Cooldown"),
		DODGED(FType.ACTION, "丞", "Dodged"),
		BLOCKED(FType.ACTION, "丟", "Blocked"),

		HURT_TIME_1(FType.HURT_TIME, "丞", "Dodged"),
		HURT_TIME_2(FType.HURT_TIME, "丟", "Blocked"),

		OUTPOST_UNDER_ATTACK(FType.TOAST, "侠", "Outpost Under Attack"),
		COMBAT_START(FType.TOAST, "価", "Combat Start"),
		COMBAT_END(FType.TOAST, "侢", "Exited Combat"),
		ITEM_LOW_ENCHANTMENT(FType.TOAST, "侩", "Item Low Enchantment"),
		ITEM_LOW_DURABILITY(FType.TOAST, "侫", "Item Low Durability"),
		NEW_KNOWLEDGE(FType.TOAST, "侣", "New Knowledge"),
		GAINED_QUEST_POINTS(FType.TOAST, "侤", "Gained Quest Points"),
		UNSPENT_LEVEL_POINTS(FType.TOAST, "侥", "Unspent Level Points"),
		KARMA_INCREASED(FType.TOAST, "侦", "Karma Increased"),
		KARMA_DECREASED(FType.TOAST, "侧", "Karma Decreased"),
		FRIEND_REQUEST(FType.TOAST, "侬", "Friend Request"),
		FAITH_RESTORED(FType.TOAST, "侭", "Faith Restored"),

		ELITE(FType.MOB_TAG, "✜", "#B55207"),
		BOSS(FType.MOB_TAG, "✮", "#FC0000"),

		DANGSON(FType.CHAT_TAG, "財", "DANGSON"),
		DANGSON_BAD(FType.CHAT_TAG, "㙳", "DANGSON_BAD");

		private final FType type;
		private final String[] unicode;
		private final String[] text;

		FFont(FType type, String unicode, String text) {
			this(type, unicode, text, false);
		}

		FFont(FType type, String unicode, String[] text) {
			this.type = type;
			this.unicode = unicode.split("");
			this.text = text;
		}

		FFont(FType type, String unicode, String text, boolean isSplit) {
			this.type = type;
			this.unicode = isSplit ? unicode.split("") : new String[] { unicode };
			this.text = text != null && isSplit ? text.split("") : new String[] { text };
		}

		public FType getType() {
			return type;
		}

		public String[] getUnicodes() {
			return unicode;
		}

		public String getUnicode(int index) {
			return unicode[Math.min(index, unicode.length - 1)];
		}

		public String getText(int index) {
			return text[Math.min(index, text.length - 1)];
		}
	}

	public static Map<String, String> get(FType type) {
		return MAPS.get(type);
	}

	public static Set<String> keys(FType type) {
		return MAPS.get(type).keySet();
	}

	public static Set<Map.Entry<String, String>> entries(FType type) {
		return MAPS.get(type).entrySet();
	}
}
