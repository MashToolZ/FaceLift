package xyz.mashtoolz.handlers;

import java.util.HashMap;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.mixins.AdvancementToastInterface;

public class ToastHandler {

	public static HashMap<String, String> advancements = new HashMap<String, String>() {
		{
			put("侠", "OutpostUnderAttack");
			put("侩", "ItemLowEnchantment");
			put("価", "CombatStart");
			put("侫", "ItemLowDurability");
			put("侢", "CombatEnd");
			put("侬", "FriendRequest");
			put("侣", "NewKnowledge");
			put("侭", "FaithRestored");
			put("侤", "QuestPoints");
			put("侮", "UNKNOWN");
			put("侥", "UnspentPoints");
			put("侯", "UNKNOWN");
			put("侦", "KarmaIncreased");
			put("侰", "UNKNOWN");
			put("侧", "KarmaDecreased");
			put("侱", "UNKNOWN");
		}
	};

	public static void add(Toast toast) {

		if (!(toast instanceof AdvancementToast))
			return;

		try {

			var advancementToast = (AdvancementToastInterface) toast;
			AdvancementEntry advancement = advancementToast.getAdvancement();

			var id = advancement.id().toString();
			var name = advancements.get(id);
			if (name == null)
				return;

			switch (name) {
				case "CombatStart": {
					FaceConfig.General.inCombat = true;
					break;
				}

				case "CombatEnd": {
					FaceConfig.General.inCombat = false;
					break;
				}
			}
		} catch (Exception e) {
		}
	}
}
