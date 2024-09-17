package xyz.mashtoolz.handlers;

import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceFont;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.mixins.AdvancementToastAccessor;

public class ToastHandler {

	public static void add(Toast toast) {

		if (!(toast instanceof AdvancementToast))
			return;

		try {

			var advancementToast = (AdvancementToastAccessor) toast;
			var advancement = advancementToast.getAdvancement();
			var id = advancement.id().toString();
			var name = FaceFont.get(FType.TOAST).get(id);

			if (name == null)
				return;

			switch (name) {
				case "Combat Start" -> FaceConfig.General.inCombat = true;
				case "Exited Combat" -> FaceConfig.General.inCombat = false;
			}
		} catch (Exception e) {
		}
	}
}
