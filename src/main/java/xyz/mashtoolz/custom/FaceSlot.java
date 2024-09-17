package xyz.mashtoolz.custom;

import net.minecraft.item.ItemStack;
import xyz.mashtoolz.FaceLift;

public enum FaceSlot {

	HELMET(FaceSlotType.HELMET, 4),
	CHESTPLATE(FaceSlotType.CHESTPLATE, 13),
	LEGGINGS(FaceSlotType.LEGGINGS, 22),
	BOOTS(FaceSlotType.BOOTS, 31),
	NECKLACE(FaceSlotType.NECKLACE, 9),
	EARRING_1(FaceSlotType.EARRING, 0),
	EARRING_2(FaceSlotType.EARRING, 1),
	RING_1(FaceSlotType.RING, 18),
	RING_2(FaceSlotType.RING, 19),
	MAINHAND(FaceSlotType.MAINHAND, null),
	OFFHAND(FaceSlotType.OFFHAND, 14),
	PET(FaceSlotType.PET, 8),
	MOUNT(FaceSlotType.MOUNT, 27),
	PICKAXE(FaceSlotType.PICKAXE, FaceToolType.PICKAXE),
	WOODCUTTINGAXE(FaceSlotType.WOODCUTTINGAXE, FaceToolType.WOODCUTTINGAXE),
	HOE(FaceSlotType.HOE, FaceToolType.HOE);

	private FaceLift INSTANCE = FaceLift.getInstance();

	private final FaceSlotType slotType;
	private int index;
	private ItemStack stack = ItemStack.EMPTY;
	private FaceToolType toolType;

	private FaceSlot(FaceSlotType type, FaceToolType toolType) {
		this.slotType = type;
		this.toolType = toolType;
		updateIndex(true);
	}

	private FaceSlot(FaceSlotType type, int index) {
		this.slotType = type;
		this.index = index;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}

	public ItemStack getStack() {
		return stack;
	}

	public FaceSlotType getFaceSlotType() {
		return slotType;
	}

	public int getIndex() {
		updateIndex(false);
		return index;
	}

	public void updateIndex(boolean init) {
		var inventory = INSTANCE.CLIENT.player.getInventory();
		if (slotType.equals(FaceSlotType.MAINHAND)) {
			var hotbarSlot = inventory.selectedSlot;
			this.index = (init ? (36 + hotbarSlot) : (8 - hotbarSlot));
		} else if (toolType != null) {
			var tool = FaceTool.getByType(toolType);
			this.index = tool != null ? tool.getSlotIndex() : -1;
		}
	}
}