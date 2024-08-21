package xyz.mashtoolz.config;

public class Category_Inventory {

	public float rarityOpacity = 0.75F;

	public Searchbar searchbar = new Searchbar();

	public static Category_Inventory getDefault() {
		return new Category_Inventory();
	}
}
