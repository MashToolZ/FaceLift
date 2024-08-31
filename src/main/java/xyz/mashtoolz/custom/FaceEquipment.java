package xyz.mashtoolz.custom;

import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import xyz.mashtoolz.helpers.HudRenderer;

public class FaceEquipment {

	public static boolean updateCache = false;
	public static List<FaceSlot> slots = new ArrayList<>();
	public static List<Integer> indices = new ArrayList<>();

	private static final ArrayList<FaceSlotType> DOUBLE_SLOTS = new ArrayList<>(Arrays.asList(FaceSlotType.EARRING, FaceSlotType.RING));

	public static FaceSlot getSlot(String id, boolean shiftDown) {
		var matches = new ArrayList<FaceSlot>();
		for (var slot : slots) {
			var type = slot.getFaceType();
			if (Arrays.stream(type.getNames()).anyMatch(name -> name.equals(id))) {
				if (DOUBLE_SLOTS.contains(type)) {
					matches.add(slot);
					if (matches.size() == 2)
						return matches.get(shiftDown ? 1 : 0);
				} else
					return slot;
			}
		}
		return null;
	}

	static {
		for (FaceSlot slot : FaceSlot.values()) {
			slots.add(slot);
			indices.add(slot.getIndex());
		}
	}

	public static void clearCache() {
		for (var slot : slots)
			slot.setStack(ItemStack.EMPTY);
	}

	public static ScreenHandler handler;

	public static void updateCachedEquipment() {

		var size = handler.slots.size();
		for (var slot : FaceEquipment.slots) {
			var isTool = HudRenderer.TOOL_TYPES.contains(slot.getFaceType());
			var index = getSlotIndex(slot, size);
			var stack = handler.getSlot(index).getStack();
			if (stack.isEmpty())
				continue;

			var item = new FaceItem(stack);
			if (isTool && item.getFaceTool() == null)
				continue;

			if (item.invalid)
				continue;

			slot.setStack(stack);
		}
	}

	private static int getSlotIndex(FaceSlot slot, int size) {
		if (slot.getFaceType().equals(FaceSlotType.MAINHAND))
			return size - slot.getIndex() - 1;
		else if (HudRenderer.TOOL_TYPES.contains(slot.getFaceType()))
			return slot.getIndex() + (size - 45);
		else
			return slot.getIndex();
	}
}
