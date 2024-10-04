package xyz.mashtoolz.custom;

import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;

public class FacePotion extends FaceItem {

	private int remaining = 0;
	private int max = 0;

	public FacePotion(ItemStack stack) {
		super(stack);

		var pattern = Pattern.compile("Uses: \\((\\d+)/(\\d+)\\)", Pattern.DOTALL);
		var matcher = pattern.matcher(getTooltip());
		if (!matcher.find() || matcher.groupCount() != 2) {
			return;
		}

		remaining = Integer.parseInt(matcher.group(1));
		max = Integer.parseInt(matcher.group(2));
	}

	public boolean isInvalid() {
		return max == 0;
	}

	public int getRemaining() {
		return remaining;
	}

	public int getMax() {
		return max;
	}
}
