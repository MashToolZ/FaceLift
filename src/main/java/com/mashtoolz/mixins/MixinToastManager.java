package com.mashtoolz.mixins;

import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mashtoolz.FaceLift;
import com.mashtoolz.helpers.AdvancementInfo;

import net.minecraft.advancement.Advancement;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

@Mixin(ToastManager.class)
public class MixinToastManager {

	private FaceLift instance;

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

		if (instance == null)
			instance = FaceLift.getInstance();

		if (!(toast instanceof AdvancementToast))
			return;

		try {
			var advancementToast = (IAdvancementToast) toast;
			Advancement advancement = advancementToast.getAdvancement();

			var id = advancement.getDisplay().getTitle().getString();
			var advancementInfo = advancementInfos.get(id);
			if (advancementInfo == null)
				return;

			switch (advancementInfo.getName()) {
				case "CombatStart": {
					instance.config.inCombat = true;
					break;
				}

				case "CombatEnd": {
					instance.config.inCombat = false;
					break;
				}
			}
		} catch (Exception e) {
		}
	}
}