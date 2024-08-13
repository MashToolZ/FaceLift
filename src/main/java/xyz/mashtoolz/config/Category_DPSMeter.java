package xyz.mashtoolz.config;

import org.joml.Vector2i;

public class Category_DPSMeter {

	public boolean enabled = true;
	public boolean showTimebar = true;
	public int duration = 5000;
	public Vector2i position = new Vector2i(5, 37);

	public static Category_DPSMeter getDefault() {
		return new Category_DPSMeter();
	}
}
