package xyz.mashtoolz;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceSpell;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.displays.ArenaTimer;
import xyz.mashtoolz.displays.CombatTimer;
import xyz.mashtoolz.displays.DPSMeter;
import xyz.mashtoolz.handlers.KeyHandler;
import xyz.mashtoolz.handlers.RenderHandler;
import xyz.mashtoolz.utils.PlayerUtils;

import java.util.Objects;

public class FaceLift implements ClientModInitializer {

    private static FaceLift INSTANCE;

    public MinecraftClient CLIENT;
    public FaceConfig CONFIG;
    public Keybinds KEYBINDS = new Keybinds();

    @Override
    public void onInitializeClient() {

        INSTANCE = this;
        CLIENT = MinecraftClient.getInstance();

        AutoConfig.register(FaceConfig.class, GsonConfigSerializer::new);
        var holder = AutoConfig.getConfigHolder(FaceConfig.class);
        CONFIG = holder.getConfig();
        xyz.mashtoolz.config.FaceConfig.holder = holder;

        FaceStatus.registerEffects();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> {
            ClientConnection connection = Objects.requireNonNull(client.getNetworkHandler()).getConnection();
            if (connection != null && connection.getAddress() != null) {
                String serverAddress = connection.getAddress().toString().toLowerCase();
                xyz.mashtoolz.config.FaceConfig.General.onFaceLand = serverAddress.startsWith("local") || serverAddress.contains("face.land");
            }
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            xyz.mashtoolz.config.FaceConfig.General.onFaceLand = false;
            ArenaTimer.end();
        });

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {

            if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand)
                return false;

            if (CONFIG.inventory.autoTool.enabled)
                FaceTool.update();

            return false;
        });

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {

            if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand)
                return;

            if (entity.getName().getString().equals("Text Display")) {
                DPSMeter.TEXT_DISPLAYS.put(entity.getUuid().toString(), (TextDisplayEntity) entity);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand || client == null || client.player == null)
                return;

            PlayerUtils.update();
            CombatTimer.update();
            DPSMeter.update();
            FaceStatus.update();

            if (Keybinds.INSTANCE == null)
                Keybinds.INSTANCE = this;

            handleKeybinds();

            if (CONFIG.combat.arenaTimer.enabled && ArenaTimer.isActive() && (client.player != null && client.player.getHealth() <= 0))
                ArenaTimer.end();
        });

        ScreenEvents.AFTER_INIT.register(RenderHandler::afterInitScreen);
        HudRenderCallback.EVENT.register(RenderHandler::onHudRender);
        WorldRenderEvents.BEFORE_ENTITIES.register(RenderHandler::beforeEntities);
    }

    public static FaceLift getInstance() {
        return INSTANCE;
    }

    public void sendCommand(String command) {
        Objects.requireNonNull(CLIENT.player).networkHandler.sendChatCommand(command);
    }

    public static void info(boolean console, String message) {
        if (console)
            System.out.println("[FaceLift] " + message);
        else
            Objects.requireNonNull(INSTANCE.CLIENT.player).sendMessage(Text.literal("§7[§cFaceLift§7] " + message));
    }

    public static void handleHotbarScroll(PlayerInventory inventory, double scrollAmount, CallbackInfo ci) {

        if (INSTANCE.CONFIG.inventory.hotbar.scrollFix) {

            int i = (int) Math.signum(scrollAmount);
            inventory.selectedSlot -= i;

            var hotbar = INSTANCE.CONFIG.inventory.hotbar;
            var WRAP = xyz.mashtoolz.config.FaceConfig.Inventory.Hotbar.ScrollFixMethod.WRAP;

            while (inventory.selectedSlot <= 3)
                inventory.selectedSlot += (hotbar.scrollFixMethod == WRAP) ? 5 : 1;

            while (inventory.selectedSlot >= 9)
                inventory.selectedSlot -= (hotbar.scrollFixMethod == WRAP) ? 5 : 1;

            ci.cancel();
        }
    }

    public static void handleHotbarChange(PlayerInventory inventory, int index) {

        var client = INSTANCE.CLIENT;
        var player = client.player;
        if (player == null) {
            inventory.selectedSlot = index;
            return;
        }

        var stack = inventory.getStack(index);
        if (stack.isEmpty()) {
            inventory.selectedSlot = index;
            return;
        }

        var spell = FaceSpell.from(stack);
        if (spell != null) {
            spell.cast();
            return;
        }

        if (!INSTANCE.CONFIG.inventory.hotbar.instantPotion) {
            inventory.selectedSlot = index;
            return;
        }

        var item = FaceItem.from(stack);
        if (item.isInvalid() || !item.isPotion()) {
            inventory.selectedSlot = index;
            return;
        }

        int selectedSlot = inventory.selectedSlot;
        Objects.requireNonNull(client.getNetworkHandler()).sendPacket(new UpdateSelectedSlotC2SPacket(index));
        assert INSTANCE.CLIENT.interactionManager != null;
        INSTANCE.CLIENT.interactionManager.interactItem(player, Hand.MAIN_HAND);
        client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
    }

    private void handleKeybinds() {

        while (Keybinds.MENU.wasPressed())
            KeyHandler.MENU();

        while (Keybinds.MOUNT.wasPressed())
            KeyHandler.MOUNT(PlayerUtils.isMounted());

        while (Keybinds.ESCAPE.wasPressed())
            KeyHandler.ESCAPE();

        if (Keybinds.isPressed(Keybinds.SET_TOOL_SLOT))
            KeyHandler.SET_TOOL_SLOT();
    }
}