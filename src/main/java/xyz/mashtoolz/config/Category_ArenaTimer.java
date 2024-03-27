package xyz.mashtoolz.config;

import org.joml.Vector2i;

public class Category_ArenaTimer {

	public boolean enabled = true;
	public Vector2i position = new Vector2i(122, 20);

	public static Category_ArenaTimer getDefault() {
		return new Category_ArenaTimer();
	}
}
