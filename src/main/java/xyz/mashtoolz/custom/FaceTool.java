package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;

public class FaceTool {

	private String name;
	private int slot;
	private Identifier texture;

	public FaceTool(String name, int slot, Identifier texture) {
		this.name = name;
		this.slot = slot;
		this.texture = texture;
	}

	public Identifier getTexture() {
		return texture;
	}

	public String getName() {
		return name;
	}

	public int getSlot() {
		return slot;
	}
}