package xyz.mashtoolz.custom;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.ScreenHandler;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceEquipment {

	private static final FaceLift INSTANCE = FaceLift.getInstance();

	public static boolean updateCache = false;
	public static final List<FaceSlot> SLOTS = new ArrayList<>();
	public static final List<Integer> INDICES = new ArrayList<>();

	private static final List<FaceSlotType> DUALWIELD_SLOTS = List.of(FaceSlotType.MAINHAND, FaceSlotType.OFFHAND);
	private static final List<FaceSlotType> DOUBLE_SLOTS = List.of(FaceSlotType.EARRING, FaceSlotType.RING);
	public static final List<FaceSlotType> TOOL_TYPES = List.of(FaceSlotType.PICKAXE, FaceSlotType.WOODCUTTINGAXE, FaceSlotType.HOE);

	public static ScreenHandler handler;

	static {
		for (FaceSlot slot : FaceSlot.values()) {
			SLOTS.add(slot);
			for (var eqSlot : INSTANCE.CONFIG.inventory.equipmentSlots) {
				if (eqSlot[0].equals(slot.getFaceSlotType().toString())) {
					try {
						var compound = StringNbtReader.parse(eqSlot[1]);
                        assert INSTANCE.CLIENT.world != null;
                        var wrapper = INSTANCE.CLIENT.world.getRegistryManager();
						slot.setStack(ItemStack.fromNbtOrEmpty(wrapper, compound));
					} catch (Exception e) {
						FaceLift.info(true, "Failed to parse NBT for cached item: " + eqSlot[0] + "[" + eqSlot[1] + "]");
					}
				}
			}
			INDICES.add(slot.getIndex());
		}
	}

	public static FaceSlot getSlot(String id, boolean shiftDown) {
		var matches = SLOTS.stream()
				.filter(slot -> Arrays.asList(slot.getFaceSlotType().getNames()).contains(id))
				.toList();

		if (matches.isEmpty())
			return null;

		var slotType = matches.getFirst().getFaceSlotType();
		if (DUALWIELD_SLOTS.contains(slotType) || DOUBLE_SLOTS.contains(slotType))
			return matches.size() == 2 ? matches.get(shiftDown ? 1 : 0) : matches.getFirst();
		else
			return matches.getFirst();

	}

	public static void clearCache() {
		SLOTS.forEach(slot -> slot.setStack(ItemStack.EMPTY));
		INSTANCE.CONFIG.inventory.equipmentSlots.clear();
	}

	public static void updateCachedEquipment() {

		clearCache();

		int size = handler.slots.size();
		for (FaceSlot slot : FaceEquipment.SLOTS) {
			boolean isTool = TOOL_TYPES.contains(slot.getFaceSlotType());
			int index = getSlotIndex(slot, size);
			var stack = handler.getSlot(index).getStack();
			if (stack.isEmpty())
				continue;

			var item = FaceItem.from(stack);
			if (isTool && item.getFaceTool() == null)
				continue;

			if (item.isInvalid())
				continue;

			slot.setStack(stack);

            assert INSTANCE.CLIENT.world != null;
            var wrapper =INSTANCE.CLIENT.world.getRegistryManager();
			var compoundTag = stack.encode(wrapper);
			var eqSlot = new String[] { slot.getFaceSlotType().toString(), compoundTag.asString() };
			INSTANCE.CONFIG.inventory.equipmentSlots.add(eqSlot);
		}

		FaceConfig.save();
	}

	private static int getSlotIndex(FaceSlot slot, int size) {
		FaceSlotType slotType = slot.getFaceSlotType();

		if (slotType.equals(FaceSlotType.MAINHAND))
			return size - slot.getIndex() - 1;
		else if (TOOL_TYPES.contains(slotType))
			return slot.getIndex() + (size - 45);
		else
			return slot.getIndex();
	}
}
