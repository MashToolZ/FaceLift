package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

public class FaceTool {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	public static final FaceTool PICKAXE = new FaceTool(FaceToolType.PICKAXE, INSTANCE.CONFIG.inventory.autoTool.PICKAXE, FaceTexture.EMPTY_PICKAXE);
	public static final FaceTool WOODCUTTINGAXE = new FaceTool(FaceToolType.WOODCUTTINGAXE, INSTANCE.CONFIG.inventory.autoTool.WOODCUTTINGAXE, FaceTexture.EMPTY_WOODCUTTINGAXE);
	public static final FaceTool HOE = new FaceTool(FaceToolType.HOE, INSTANCE.CONFIG.inventory.autoTool.HOE, FaceTexture.EMPTY_HOE);
	public static final FaceTool BEDROCK = new FaceTool(FaceToolType.BEDROCK, -1, null);

	private final FaceToolType type;
	private int slotIndex;
	private final Identifier texture;

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
		updateConfig();
	}

	private void updateConfig() {
		var autoTool = INSTANCE.CONFIG.inventory.autoTool;
		switch (type) {
			case PICKAXE -> autoTool.PICKAXE = slotIndex;
			case WOODCUTTINGAXE -> autoTool.WOODCUTTINGAXE = slotIndex;
			case HOE -> autoTool.HOE = slotIndex;
			default -> {
			}
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
		for (FaceTool tool : values())
			if (tool.type.equals(type))
				return tool;
		return null;
	}
}