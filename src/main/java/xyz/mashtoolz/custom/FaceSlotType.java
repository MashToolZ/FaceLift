package xyz.mashtoolz.custom;

import java.util.Arrays;

import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public enum FaceSlotType {

	EARRING(Items.GOLDEN_HORSE_ARMOR),
	NECKLACE(Items.IRON_HORSE_ARMOR),
	RING(Items.DIAMOND_HORSE_ARMOR),
	HELMET(Items.LEATHER_HELMET),
	CHESTPLATE(Items.LEATHER_CHESTPLATE),
	LEGGINGS(Items.LEATHER_LEGGINGS),
	BOOTS(Items.LEATHER_BOOTS),
	MAINHAND(new Item[] {
			Items.BOW,
			Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
			Items.STONE_SHOVEL, Items.IRON_SHOVEL,
			Items.IRON_AXE,
	}),
	OFFHAND(new Item[] { Items.SHIELD, Items.BOOK, Items.ARROW }),
	PICKAXE(FaceToolType.PICKAXE),
	WOODCUTTINGAXE(FaceToolType.WOODCUTTINGAXE),
	HOE(FaceToolType.HOE),
	MOUNT(Items.SADDLE),
	PET(Items.TNT_MINECART);

	private final String[] names;

	private FaceSlotType(FaceToolType toolType) {
		this.names = new String[] { toolType.getName() };
	}

	private FaceSlotType(Item item) {
		var name = Registries.ITEM.getId(item).toString();
		this.names = new String[] { name };
	}

	private FaceSlotType(Item[] items) {
		this.names = Arrays.stream(items).map(Registries.ITEM::getId).map(Object::toString).collect(Collectors.toList()).toArray(new String[0]);
	}

	public String[] getNames() {
		return names;
	}
}