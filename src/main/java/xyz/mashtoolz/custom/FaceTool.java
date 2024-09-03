package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

public class FaceTool {

	private static FaceLift instance = FaceLift.getInstance();

	public static FaceTool PICKAXE = new FaceTool(FaceToolType.PICKAXE, instance.config.inventory.autoTool.pickaxe, FaceTexture.EMPTY_PICKAXE);
	public static FaceTool WOODCUTTINGAXE = new FaceTool(FaceToolType.WOODCUTTINGAXE, instance.config.inventory.autoTool.woodcuttingaxe, FaceTexture.EMPTY_WOODCUTTINGAXE);
	public static FaceTool HOE = new FaceTool(FaceToolType.HOE, instance.config.inventory.autoTool.hoe, FaceTexture.EMPTY_HOE);
	public static FaceTool BEDROCK = new FaceTool(FaceToolType.BEDROCK, -1, null);

	private FaceToolType type;
	private int slotIndex;
	private Identifier texture;

	private FaceTool(FaceToolType type, int slotIndex, Identifier texture) {
		this.type = type;
		this.slotIndex = slotIndex;
		this.texture = texture;
	}

	public static FaceTool[] values() {
		return new FaceTool[] { PICKAXE, WOODCUTTINGAXE, HOE, BEDROCK };
	}

	public FaceToolType getFaceToolType() {
		return type;
	}

	public void setSlotIndex(int slotIndex) {
		this.slotIndex = slotIndex;
		var autoTool = instance.config.inventory.autoTool;
		switch (type) {
			case PICKAXE:
				autoTool.pickaxe = slotIndex;
				break;
			case WOODCUTTINGAXE:
				autoTool.woodcuttingaxe = slotIndex;
				break;
			case HOE:
				autoTool.hoe = slotIndex;
				break;
			default:
				break;
		}
		FaceConfig.save();
	}

	public int getSlotIndex() {
		return slotIndex;
	}

	public Identifier getTexture() {
		return texture;
	}

	public static FaceTool getByType(FaceToolType type) {
		for (FaceTool tool : FaceTool.values())
			if (tool.type == type)
				return tool;
		return null;
	}
}