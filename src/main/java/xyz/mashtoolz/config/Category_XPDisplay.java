package xyz.mashtoolz.config;

import org.joml.Vector2i;

public class Category_XPDisplay {

	public boolean enabled = true;
	public int duration = 5000;
	public Vector2i position = new Vector2i(5, 99);

	public static Category_XPDisplay getDefault() {
		return new Category_XPDisplay();
	}
}
