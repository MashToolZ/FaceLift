package xyz.mashtoolz.utils;

import net.minecraft.item.ItemStack;

public class ItemUtils {

	public static boolean compareStacks(ItemStack stack1, ItemStack stack2) {
		return stack1.getNbt().asString().equals(stack2.getNbt().asString());
	}
}
