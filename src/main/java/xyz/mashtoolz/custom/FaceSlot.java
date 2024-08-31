package xyz.mashtoolz.custom;

import net.minecraft.item.ItemStack;
import xyz.mashtoolz.FaceLift;

public enum FaceSlot {

	EARRING_1(FaceSlotType.EARRING, 0),
	EARRING_2(FaceSlotType.EARRING, 1),
	NECKLACE(FaceSlotType.NECKLACE, 9),
	RING_1(FaceSlotType.RING, 18),
	RING_2(FaceSlotType.RING, 19),
	HELMET(FaceSlotType.HELMET, 4),
	CHESTPLATE(FaceSlotType.CHESTPLATE, 13),
	LEGGINGS(FaceSlotType.LEGGINGS, 22),
	BOOTS(FaceSlotType.BOOTS, 31),
	MAINHAND(FaceSlotType.MAINHAND, null),
	OFFHAND(FaceSlotType.OFFHAND, 14),
	PICKAXE(FaceSlotType.PICKAXE, FaceToolType.PICKAXE),
	WOODCUTTINGAXE(FaceSlotType.WOODCUTTINGAXE, FaceToolType.WOODCUTTINGAXE),
	HOE(FaceSlotType.HOE, FaceToolType.HOE),
	MOUNT(FaceSlotType.MOUNT, 27),
	PET(FaceSlotType.PET, 8);

	private FaceLift instance = FaceLift.getInstance();

	private final FaceSlotType type;
	private int index;
	private ItemStack stack = ItemStack.EMPTY;
	private FaceToolType toolType;

	private FaceSlot(FaceSlotType type, FaceToolType name) {
		this.type = type;
		this.toolType = name;
		if (type.equals(FaceSlotType.MAINHAND)) {
			var hotbarSlot = instance.client.player.getInventory().selectedSlot;
			this.index = 36 + hotbarSlot;
		} else {
			var tool = instance.config.inventory.autoTool.get(name);
			this.index = tool != null ? tool.getSlotIndex() : -1;
		}
	}

	private FaceSlot(FaceSlotType type, int index) {
		this.type = type;
		this.index = index;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}

	public ItemStack getStack() {
		return stack;
	}

	public FaceSlotType getFaceType() {
		return type;
	}

	public int getIndex() {
		if (type.equals(FaceSlotType.MAINHAND)) {
			var hotbarSlot = FaceLift.getInstance().client.player.getInventory().selectedSlot;
			this.index = 8 - hotbarSlot;
		} else if (toolType != null) {
			var tool = FaceLift.getInstance().config.inventory.autoTool.get(toolType);
			this.index = tool != null ? tool.getSlotIndex() : -1;
		}
		return index;
	}
}