package xyz.mashtoolz.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.helpers.XPDisplay;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ChatHud.class)
public class MixinChatHud {

	private FaceLift instance;

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "HEAD"), cancellable = true)
	private void addMessage(Text text, @Nullable MessageSignatureData messageSignatureData, int i,
			@Nullable MessageIndicator messageIndicator, boolean bl, CallbackInfo ci) {
		handleMessage(text, ci);
	}

	private String escapeStringToUnicode(String input) {
		StringBuilder builder = new StringBuilder();
		for (char ch : input.toCharArray()) {
			if (ch < 128) {
				builder.append(ch);
			} else {
				builder.append(String.format("\\u%04x", (int) ch));
			}
		}
		return builder.toString();
	}

	private void handleMessage(Text text, CallbackInfo ci) {

		if (instance == null)
			instance = FaceLift.getInstance();

		if (!instance.config.xpDisplay.enabled)
			return;

		var message = text.getString().replaceAll("[.,]", "");
		if (escapeStringToUnicode(message).startsWith("\\uf804"))
			return;

		for (var regex : instance.config.xpRegexes) {
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
					if (!instance.config.xpDisplays.containsKey(key))
						instance.config.xpDisplays.put(key, new XPDisplay(key, color, 0, System.currentTimeMillis(), false));

					var display = instance.config.xpDisplays.get(key);
					display.setXP(Integer.parseInt(match.group(2)) + display.getXP());
					display.setTime(System.currentTimeMillis());
					display.setColor(color);
					break;
				}

				case "combatXP": {
					var key = "Combat";
					if (!instance.config.xpDisplays.containsKey(key))
						instance.config.xpDisplays.put(key, new XPDisplay(key, "<#8AF828>", 0, System.currentTimeMillis(), false));

					var display = instance.config.xpDisplays.get(key);
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
