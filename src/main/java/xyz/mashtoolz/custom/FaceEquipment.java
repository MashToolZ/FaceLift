package xyz.mashtoolz.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.ScreenHandler;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.helpers.HudRenderer;

public class FaceEquipment {

	private static FaceLift instance = FaceLift.getInstance();

	public static boolean updateCache = false;
	public static List<FaceSlot> slots = new ArrayList<>();
	public static List<Integer> indices = new ArrayList<>();

	private static final ArrayList<FaceSlotType> DOUBLE_SLOTS = new ArrayList<>(Arrays.asList(FaceSlotType.EARRING, FaceSlotType.RING));

	public static FaceSlot getSlot(String id, boolean shiftDown) {
		var matches = new ArrayList<FaceSlot>();
		for (var slot : slots) {
			var type = slot.getSlotType();
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
			for (var eqSlot : instance.config.inventory.equipmentSlots) {
				if (eqSlot[0].equals(slot.getSlotType().toString())) {
					try {
						var compound = StringNbtReader.parse(eqSlot[1]);
						slot.setStack(ItemStack.fromNbt(compound));
					} catch (Exception e) {
						FaceLift.info(true, "Failed to parse NBT for cached item: " + eqSlot[0] + "[" + eqSlot[1] + "]");
					}
				}
			}
			indices.add(slot.getIndex());
		}
	}

	public static void clearCache() {
		for (var slot : slots)
			slot.setStack(ItemStack.EMPTY);
		instance.config.inventory.equipmentSlots.clear();
	}

	public static ScreenHandler handler;

	public static void updateCachedEquipment() {

		clearCache();

		var size = handler.slots.size();
		for (var slot : FaceEquipment.slots) {
			var isTool = HudRenderer.TOOL_TYPES.contains(slot.getSlotType());
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

			var compoundTag = new NbtCompound();
			stack.writeNbt(compoundTag);
			var eqSlot = new String[] { slot.getSlotType().toString(), compoundTag.asString() };
			instance.config.inventory.equipmentSlots.add(eqSlot);
		}

		FaceConfig.save();
	}

	private static int getSlotIndex(FaceSlot slot, int size) {
		if (slot.getSlotType().equals(FaceSlotType.MAINHAND))
			return size - slot.getIndex() - 1;
		else if (HudRenderer.TOOL_TYPES.contains(slot.getSlotType()))
			return slot.getIndex() + (size - 45);
		else
			return slot.getIndex();
	}
}
