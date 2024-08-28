package xyz.mashtoolz.helpers;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.utils.TextUtils;

public class ChatHandler {

	public static void addMessage(Text text, CallbackInfo ci) {

		var message = text.getString().replaceAll("[.,]", "");
		if (TextUtils.escapeStringToUnicode(message, false).startsWith("\\uf804"))
			return;

		if (FaceConfig.xpDisplay.enabled)
			handleXPMessage(text, message, ci);

		try {

			if (message.equals("RISE AND SHINE! You're well rested and had a pretty good meal!"))
				FaceStatus.WELL_RESTED.applyEffect();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void handleXPMessage(Text text, String message, CallbackInfo ci) {

		for (var regex : FaceConfig.xpRegexes) {
			var match = regex.getPattern().matcher(message);

			if (!match.find())
				continue;

			ci.cancel();

			switch (regex.getKey()) {
				case "skillXP": {

					var color = "#D1D1D1";

					try {
						color = text.getSiblings().get(0).getStyle().getColor().toString();
					} catch (Exception e) {
					}

					if (color.matches("#[0-9A-Fa-f]{6}"))
						color = "<" + color + ">";
					else
						color = Formatting.byName(color).toString();

					var key = match.group(1);
					if (!FaceConfig.xpDisplays.containsKey(key))
						FaceConfig.xpDisplays.put(key, new XPDisplay(key, color, 0, System.currentTimeMillis(), false));

					var display = FaceConfig.xpDisplays.get(key);
					display.setXP(Integer.parseInt(match.group(2)) + display.getXP());
					display.setTime(System.currentTimeMillis());
					display.setColor(color);
					break;
				}

				case "combatXP": {
					var key = "Combat";
					if (!FaceConfig.xpDisplays.containsKey(key))
						FaceConfig.xpDisplays.put(key, new XPDisplay(key, "<#8AF828>", 0, System.currentTimeMillis(), false));

					var display = FaceConfig.xpDisplays.get(key);
					display.setXP(Integer.parseInt(match.group(1)) + display.getXP());
					display.setTime(System.currentTimeMillis());
					display.setColor("<#8AF828>");
					break;
				}
			}
			return;
		}
	}
}
