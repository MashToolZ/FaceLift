package xyz.mashtoolz.mixins;

import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.helpers.AdvancementInfo;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

	private static FaceLift instance = FaceLift.getInstance();

	public HashMap<String, AdvancementInfo> advancementInfos = new HashMap<String, AdvancementInfo>() {
		{
			put("侠", new AdvancementInfo("侠", "OutpostUnderAttack"));
			put("侩", new AdvancementInfo("侩", "ItemLowEnchantment"));
			put("価", new AdvancementInfo("価", "CombatStart"));
			put("侫", new AdvancementInfo("侫", "ItemLowDurability"));
			put("侢", new AdvancementInfo("侢", "CombatEnd"));
			put("侬", new AdvancementInfo("侬", "FriendRequest"));
			put("侣", new AdvancementInfo("侣", "NewKnowledge"));
			put("侭", new AdvancementInfo("侭", "FaithRestored"));
			put("侤", new AdvancementInfo("侤", "QuestPoints"));
			put("侮", new AdvancementInfo("侮", "UNKNOWN"));
			put("侥", new AdvancementInfo("侥", "UnspentPoints"));
			put("侯", new AdvancementInfo("侯", "UNKNOWN"));
			put("侦", new AdvancementInfo("侦", "KarmaIncreased"));
			put("侰", new AdvancementInfo("侰", "UNKNOWN"));
			put("侧", new AdvancementInfo("侧", "KarmaDecreased"));
			put("侱", new AdvancementInfo("侱", "UNKNOWN"));
		}
	};

	@Inject(method = "add", at = @At("HEAD"))
	public void add(Toast toast, CallbackInfo ci) {

		if (!(toast instanceof AdvancementToast) || instance.config == null)
			return;

		try {

			var advancementToast = (AdvancementToastInterface) toast;
			AdvancementEntry advancement = advancementToast.getAdvancement();

			var id = advancement.id().toString();
			var advancementInfo = advancementInfos.get(id);
			if (advancementInfo == null)
				return;

			switch (advancementInfo.getName()) {
				case "CombatStart": {
					instance.config.general.inCombat = true;
					break;
				}

				case "CombatEnd": {
					instance.config.general.inCombat = false;
					break;
				}
			}
		} catch (Exception e) {
		}
	}
}