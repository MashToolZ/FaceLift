package xyz.mashtoolz.config;

import java.util.Map;

import net.minecraft.util.Identifier;
import xyz.mashtoolz.custom.FaceTexture;
import xyz.mashtoolz.custom.FaceTool;

public class ToolSlots {

	public int pickaxe = 9;
	public int axe = 10;
	public int hoe = 11;

	public Map<Identifier, FaceTool> map() {
		return Map.of(
				FaceTexture.EMPTY_PICKAXE, new FaceTool("pickaxe", this.pickaxe, FaceTexture.EMPTY_PICKAXE),
				FaceTexture.EMPTY_AXE, new FaceTool("woodcuttingaxe", this.axe, FaceTexture.EMPTY_AXE),
				FaceTexture.EMPTY_HOE, new FaceTool("hoe", this.hoe, FaceTexture.EMPTY_HOE));
	}

	public static ToolSlots getDefault() {
		return new ToolSlots();
	}

	public FaceTool getTool(String name) {
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

	public void updateSlot(FaceTool tool, int slot) {
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
		FaceConfig.save();
	}
}
