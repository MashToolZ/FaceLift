package xyz.mashtoolz.config;

import java.util.ArrayList;
import java.util.Arrays;

import org.joml.Vector2i;

public class Category_CombatTimer {

	public boolean enabled = true;
	public boolean showTimebar = true;
	public Vector2i position = new Vector2i(5, 5);
	public ArrayList<String> unicodes = new ArrayList<>(Arrays.asList("丞", "丟"));

	public static Category_CombatTimer getDefault() {
		return new Category_CombatTimer();
	}
}
