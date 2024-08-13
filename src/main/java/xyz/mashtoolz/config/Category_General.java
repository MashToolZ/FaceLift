package xyz.mashtoolz.config;

public class Category_General {

	public boolean mountThirdPerson = true;
	public int tabHeightOffset = 25;
	public float rarityOpacity = 0.75F;

	public static Category_General getDefault() {
		return new Category_General();
	}
}
