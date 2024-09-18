package xyz.mashtoolz.handlers;

import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.text.Text;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.displays.TeleportBar;
import xyz.mashtoolz.displays.XPDisplay;
import xyz.mashtoolz.structs.RegexPattern;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.TextUtils;

public class ChatHandler {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static Pattern CURSE_STACK_PATTERN = Pattern.compile("You have (\\d+) curse stacks");

	public static void addMessage(Text text, CallbackInfo ci) {

		var message = text.getString().replaceAll("[.,]", "");
		if (TextUtils.escapeStringToUnicode(message, false).startsWith("\\uf804"))
			return;

		if (INSTANCE.CONFIG.general.xpDisplay.enabled)
			handleXPMessage(text, message, ci);

		switch (message.trim()) {
			case "RISE AND SHINE! You're well rested and had a pretty good meal!" -> FaceStatus.WELL_RESTED.applyEffect();
			case "Teleport started! You will be teleported in 10 seconds!" -> TeleportBar.start(11_000, "Escaping");
			case "Teleport cancelled!" -> TeleportBar.stop();
			case "Whoosh!" -> FaceStatus.ESCAPE_COOLDOWN.applyEffect();
			case "Your curse has been broken!" -> {
				INSTANCE.CONFIG.general.curseStacks = 0;
				FaceConfig.save();
			}
			default -> {

				var curse_matcher = CURSE_STACK_PATTERN.matcher(message);
				if (curse_matcher.find()) {
					INSTANCE.CONFIG.general.curseStacks = Integer.parseInt(curse_matcher.group(1));
					FaceConfig.save();
					return;
				}
			}
		}
	}

	private static final RegexPattern[] XP_REGEXES = {
			new RegexPattern("fishingXP", "Gained Fishing XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("skillXP", "Gained (\\w+ ?){1,2} XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("combatXP", "\\+(\\d+)XP")
	};

	private static void handleXPMessage(Text text, String message, CallbackInfo ci) {
		for (var regex : XP_REGEXES) {
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
		XPDisplay display = XPDisplay.DISPLAYS.computeIfAbsent(key, k -> new XPDisplay(k, color, 0, System.currentTimeMillis(), false));
		display.setXP(display.getXP() + Integer.parseInt(xpValue));
		display.setTime(System.currentTimeMillis());
		display.setColor(color);
	}
}
