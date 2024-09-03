package xyz.mashtoolz.handlers;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.text.Text;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.displays.XPDisplay;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RegexPattern;
import xyz.mashtoolz.utils.TextUtils;

public class ChatHandler {

	private static FaceLift instance = FaceLift.getInstance();

	public static void addMessage(Text text, CallbackInfo ci) {

		var message = text.getString().replaceAll("[.,]", "");
		if (TextUtils.escapeStringToUnicode(message, false).startsWith("\\uf804"))
			return;

		if (instance.config.general.xpDisplay.enabled)
			handleXPMessage(text, message, ci);

		if (message.equals("RISE AND SHINE! You're well rested and had a pretty good meal!"))
			FaceStatus.WELL_RESTED.applyEffect();
	}

	private static RegexPattern[] xpRegexes = new RegexPattern[] {
			new RegexPattern("fishingXP", "Gained Fishing XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("skillXP", "Gained (\\w+ ?){1,2} XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("combatXP", "\\+(\\d+)XP")
	};

	private static void handleXPMessage(Text text, String message, CallbackInfo ci) {

		for (var regex : xpRegexes) {
			var match = regex.getPattern().matcher(message);
			if (!match.find())
				continue;

			ci.cancel();

			String key = regex.getKey().equals("combatXP") ? "Combat" : match.group(1);
			String xpValue = regex.getKey().equals("combatXP") ? match.group(1) : match.group(2);
			String color = regex.getKey().equals("combatXP") ? "<#8AF828>" : ColorUtils.getTextColor(text);

			handleXP(key, xpValue, color);
			return;
		}
	}

	private static void handleXP(String key, String xpValue, String color) {
		XPDisplay display = XPDisplay.displays.computeIfAbsent(key, k -> new XPDisplay(k, color, 0, System.currentTimeMillis(), false));
		display.setXP(display.getXP() + Integer.parseInt(xpValue));
		display.setTime(System.currentTimeMillis());
		display.setColor(color);
	}
}
