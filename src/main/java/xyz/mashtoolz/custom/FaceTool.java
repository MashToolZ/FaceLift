package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

public enum FaceTool {

	PICKAXE(FaceToolType.PICKAXE, 15, FaceTexture.EMPTY_PICKAXE),
	WOODCUTTINGAXE(FaceToolType.WOODCUTTINGAXE, 16, FaceTexture.EMPTY_WOODCUTTINGAXE),
	HOE(FaceToolType.HOE, 17, FaceTexture.EMPTY_HOE),
	BEDROCK(FaceToolType.BEDROCK, -1, null);

	private static FaceLift instance = FaceLift.getInstance();

	private FaceToolType type;
	private int slotIndex;
	private Identifier texture;

	private FaceTool(FaceToolType type, int slotIndex, Identifier texture) {
		this.type = type;
		this.slotIndex = slotIndex;
		this.texture = texture;
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