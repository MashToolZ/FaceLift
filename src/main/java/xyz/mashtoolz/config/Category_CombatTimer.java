package xyz.mashtoolz.config;

import org.joml.Vector2i;

public class Category_CombatTimer {

	public boolean enabled = true;
	public boolean showTimebar = true;
	public Vector2i position = new Vector2i(5, 5);

	public static Category_CombatTimer getDefault() {
		return new Category_CombatTimer();
	}
}
