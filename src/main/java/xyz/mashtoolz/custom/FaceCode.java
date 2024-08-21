package xyz.mashtoolz.custom;

public enum FaceCode {

	DISTORTED("俾", "Distorted"),
	ENCHANTABLE("严", "Enchantable");

	public static final String[] list1 = new String[] { "倀", "倁", "倂", "倃", "倄", "倅", "倆", "倇", "倈", "倉", "倊", "個", "倌", "倍", "倎", "倏", "倖", "倗", "倘", "候", "倚", "倛", "倜", "倝", "倞", "借" };
	public static final String[] list2 = new String[] { "俐", "俑", "俒", "俓", "俔", "俕", "俖", "俗", "俘", "俙", "俚", "俛", "俜", "保", "俞", "俟", "俠", "信", "俢", "俣", "俤", "俥", "俦", "俧", "俨", "俩" };
	public static final String[] list3 = new String[] { "乀", "乁", "乂", "乃", "乄", "久", "乆", "乇", "么", "义", "乊", "之", "乌", "乍", "乎", "乏", "乐", "乑", "乒", "乓", "乔", "乕", "乖", "乗", "乘", "乙" };

	private final String unicode;
	private final String text;

	private FaceCode(String unicode, String text) {
		this.unicode = unicode;
		this.text = text;
	}

	public String getUnicode() {
		return unicode;
	}

	public String getText() {
		return text;
	}

}
