package xyz.mashtoolz.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import xyz.mashtoolz.handlers.ChatHandler;

@Mixin(ChatHud.class)
public class ChatHudMixin {

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "HEAD"), cancellable = true)
	private void FL_addMessage(Text text, @Nullable MessageSignatureData messageSignatureData, @Nullable MessageIndicator messageIndicator, CallbackInfo ci) {
		ChatHandler.addMessage(text, ci);
	}
}
