package xyz.mashtoolz.config;

import java.util.Map;

import net.minecraft.util.Identifier;
import xyz.mashtoolz.custom.FaceTexture;

public class ToolSlots {

	public int pickaxe = 9;
	public int axe = 10;
	public int hoe = 11;

	public Map<Identifier, Tool> map() {
		return Map.of(
				FaceTexture.EMPTY_PICKAXE, new Tool("pickaxe", pickaxe, FaceTexture.EMPTY_PICKAXE),
				FaceTexture.EMPTY_AXE, new Tool("woodcuttingaxe", axe, FaceTexture.EMPTY_AXE),
				FaceTexture.EMPTY_HOE, new Tool("hoe", hoe, FaceTexture.EMPTY_HOE));
	}

	public static ToolSlots getDefault() {
		return new ToolSlots();
	}

	public Tool getTool(String name) {
		switch (name) {
			case "pickaxe":
				return map().get(FaceTexture.EMPTY_PICKAXE);
			case "woodcuttingaxe":
				return map().get(FaceTexture.EMPTY_AXE);
			case "hoe":
				return map().get(FaceTexture.EMPTY_HOE);
		}
		return null;
	}

	public void updateSlot(Tool tool, int slot) {
		switch (tool.getName()) {
			case "pickaxe":
				this.pickaxe = slot;
				break;
			case "woodcuttingaxe":
				this.axe = slot;
				break;
			case "hoe":
				this.hoe = slot;
				break;
		}
		Config.save();
	}

	public static class Tool {

		private String name;
		private int slot;
		private Identifier texture;

		public Tool(String name, int slot, Identifier texture) {
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
}
