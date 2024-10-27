package xyz.mashtoolz.handlers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.config.Keybinds;
import xyz.mashtoolz.custom.*;
import xyz.mashtoolz.custom.FaceFont.FType;
import xyz.mashtoolz.custom.FaceStatus.FaceStatusEffectInstance;
import xyz.mashtoolz.displays.*;
import xyz.mashtoolz.interfaces.EntityInterface;
import xyz.mashtoolz.mixins.HandledScreenAccessor;
import xyz.mashtoolz.mixins.InGameHudAccessor;
import xyz.mashtoolz.mixins.ScreenAccessor;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.widget.DropDownMenu;
import xyz.mashtoolz.widget.SearchFieldWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RenderHandler {

    private static final FaceLift INSTANCE = FaceLift.getInstance();

    private static final MinecraftClient CLIENT = INSTANCE.CLIENT;
    private static final FaceConfig CONFIG = INSTANCE.CONFIG;

    public static SearchFieldWidget SEARCHBAR;
    private static DropDownMenu DROPDOWN;

    private static int GLINT_FRAME = 0;
    private static long GLINT_TIME = System.currentTimeMillis();

    public static final List<Item> ABILITY_ITEMS = Arrays.asList(Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE);
    public static final List<Item> IGNORED_ITEMS = Arrays.asList(Items.BARRIER, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.PLAYER_HEAD, Items.BARRIER);
    public static final List<Item> HIDDEN_ITEMS = Arrays.asList(Items.SHIELD, Items.TRIPWIRE_HOOK, Items.STRING, Items.KELP);

    public static void afterInitScreen(MinecraftClient client, Screen screen, int width, int height) {

        if (!(screen instanceof HandledScreen))
            return;

        FaceItem.clearCache();

        String title = screen.getTitle().getString();
        // Equipment Screen
        if (title.contains("库")) {
            FaceEquipment.updateCache = true;
        }

        // FaceLift.info(true, TextUtils.escapeStringToUnicode(title, false));

        setupSearchBar(client, width, height);
        setupDropdownMenu(screen, width, height);

        ((ScreenAccessor) screen).invokeAddDrawableChild(SEARCHBAR);
        ((ScreenAccessor) screen).invokeAddDrawableChild(DROPDOWN.getButton());
    }

    private static void setupSearchBar(MinecraftClient client, int width, int height) {
        var inventory = CONFIG.inventory;

        SEARCHBAR = new SearchFieldWidget(client.textRenderer, width / 2 - 90, height - 25, 180, 20, SEARCHBAR, Text.literal(inventory.searchbar.query));
        SEARCHBAR.setText(inventory.searchbar.query);
        SEARCHBAR.setMaxLength(255);

        SEARCHBAR.setChangedListener(text -> {
            inventory.searchbar.query = text;
            xyz.mashtoolz.config.FaceConfig.save();
        });

        if (inventory.searchbar.highlight) {
            SEARCHBAR.highlighted = true;
            SEARCHBAR.setEditableColor(0xFFFF78);
        }
    }

    private static void setupDropdownMenu(Screen screen, int width, int height) {
        var inventory = CONFIG.inventory;

        DROPDOWN = new DropDownMenu(screen, "Options", width / 2 + 95, height - 25, 120, 20, true);

        DROPDOWN.addButton("Case-Sensitive: " + inventory.searchbar.caseSensitive, button -> {
            inventory.searchbar.caseSensitive = !inventory.searchbar.caseSensitive;
            button.setMessage(Text.literal(" Case-Sensitive: " + inventory.searchbar.caseSensitive));
            xyz.mashtoolz.config.FaceConfig.save();
        });
    }

    public static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {

        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand)
            return;

        if (!FaceEquipment.updateCache && FaceEquipment.handler != null && (CLIENT.currentScreen == null || !CLIENT.currentScreen.getTitle().getString().contains("库"))) {
            FaceEquipment.updateCachedEquipment();
            FaceEquipment.handler = null;
        }

        if (CLIENT.currentScreen == null)
            RenderHandler.SEARCHBAR = null;

        var matrices = context.getMatrices();
        matrices.push();

        if (CONFIG.combat.combatTimer.enabled)
            CombatTimer.draw(context);

        if (CONFIG.combat.dpsMeter.enabled)
            DPSMeter.draw(context);

        if (CONFIG.combat.arenaTimer.enabled) {
            ArenaTimer.updateTimer();
            ArenaTimer.draw(context);
        }

        if (CONFIG.general.xpDisplay.enabled)
            XPDisplay.draw(context);

        if (CONFIG.general.teleportBar.enabled)
            TeleportBar.draw(context);

        matrices.pop();
    }

    public static void renderOverlayMessage(InGameHud inGameHud) {

        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand || !CONFIG.inventory.hotbar.useCustom)
            return;

        var hud = ((InGameHudAccessor) inGameHud);
        if (hud.getOverlayMessage() != null) {
            var originalText = hud.getOverlayMessage();
            var replacedText = Text.literal("");
            for (Text sibling : originalText.getSiblings()) {
                var text = Text.literal(sibling.getString().replaceAll("☊", "").replaceAll("☋", "")).setStyle(sibling.getStyle());
                replacedText.append(text);
            }
            hud.setOverlayMessage(replacedText);
        }
    }

    public static void drawStatusEffectOverlay(DrawContext context, StatusEffectInstance statusEffectInstance, int x, int y) {

        String duration = FaceStatus.getDuration(statusEffectInstance);
        String color = "<#D1D1D1>";

        if (statusEffectInstance instanceof FaceStatusEffectInstance faceStatusEffect) {
            switch (faceStatusEffect.getFaceStatus()) {
                case CURSE_STACK -> {
                    duration = "" + CONFIG.general.curseStacks;
                    color = "<#FD3434>";
                }
                default -> {
                }
            }
        }

        int durationLength = CLIENT.textRenderer.getWidth(duration);
        RenderUtils.drawTextWithShadow(context, color + duration, x + 13 - (durationLength / 2), y + 14);
    }

    public static void beforeEntities(WorldRenderContext context) {

        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand)
            return;

        var world = context.world();

        world.getEntities().forEach(entity -> {

            if (entity.isRemoved() || !entity.isAlive() || !entity.isLiving() || entity.getType().equals(EntityType.ARMOR_STAND))
                return;

            var text = entity.getCustomName();
            if (text == null || text.equals(Text.EMPTY))
                return;

            var list = text.getSiblings();
            if (list.size() < 3)
                return;

            var flEntity = (EntityInterface) entity;
            var tag = list.getFirst().getString().trim();
            int forceGlowingValue = 2;
            boolean tagFound = false;

            var optionalEntry = FaceFont.entries(FType.MOB_TAG).stream().filter(e -> e.getKey().equals(tag)).findFirst();
            if (optionalEntry.isPresent()) {
                var entry = optionalEntry.get();
                flEntity.FL_setGlowingColor(ColorUtils.hex2Int(entry.getValue(), 0xFF));
                tagFound = true;
            }

            if (!tagFound) {
                var color = list.getFirst().getStyle().getColor();
                if (color != null)
                    flEntity.FL_setGlowingColor(ColorUtils.hex2Int(color.getHexCode(), 0xFF));
                else
                    forceGlowingValue = 1;
            }

            flEntity.FL_setForceGlowing(forceGlowingValue);
        });
    }

    public static void onHandledScreenRender(DrawContext context, int mouseX, int mouseY) {

        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand || !(CLIENT.currentScreen instanceof HandledScreen))
            return;

        var screen = (HandledScreenAccessor) CLIENT.currentScreen;
        var handler = screen.getHandler();
        if (handler == null)
            return;

        if (FaceEquipment.updateCache && CLIENT.currentScreen.getTitle().getString().contains("库")) {
            FaceEquipment.updateCache = false;
            FaceEquipment.handler = handler;
        }

        if (Keybinds.isPressed(Keybinds.COMPARE_TOOLTIP))
            RenderUtils.compareAndRenderTooltip(screen, context, mouseX, mouseY);
    }

    public static void onHandledScreenKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (RenderHandler.SEARCHBAR != null && RenderHandler.SEARCHBAR.isFocused()) {
            var options = INSTANCE.CLIENT.options;
            List<KeyBinding> keys = new ArrayList<>(List.of(options.inventoryKey, options.dropKey, options.pickItemKey));
            keys.addAll(Arrays.asList(options.hotbarKeys));
            if (keys.stream().anyMatch(key -> key.matchesKey(keyCode, scanCode)))
                cir.setReturnValue(true);
        }
    }

    public static void drawItem(DrawContext context, @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        if (!stack.isEmpty()) {

            var matrices = context.getMatrices();
            BakedModel bakedModel = CLIENT.getItemRenderer().getModel(stack, world, entity, seed);
            matrices.push();
            matrices.translate((float) (x + 8), (float) (y + 8), (float) (150 + (bakedModel.hasDepth() ? z : 0)));

            try {
                var isSpell = ABILITY_ITEMS.contains(stack.getItem());
                if (isSpell && !(CLIENT.currentScreen instanceof HandledScreen)) {
                    var spell = FaceSpell.from(stack);
                    if (spell == null)
                        return;
                    spell.animate(context, stack);
                } else
                    matrices.scale(16.0F, -16.0F, 16.0F);

                boolean bl = !bakedModel.isSideLit();
                if (bl) {
                    DiffuseLighting.disableGuiDepthLighting();
                }

                if (isSpell)
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, FaceSpell.GLOBALCD != 0.0F ? 0.35F : 1.0F);
                CLIENT.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, matrices, context.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
                context.draw();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                if (bl) {
                    DiffuseLighting.enableGuiDepthLighting();
                }
            } catch (Throwable var12) {
                CrashReport crashReport = CrashReport.create(var12, "Rendering item");
                CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
                crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                throw new CrashException(crashReport);
            }

            matrices.pop();
        }

        ci.cancel();
    }

    public static void renderHotbar(InGameHud inGameHud, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand)
            return;

        boolean customHotbar = CONFIG.inventory.hotbar.useCustom;

        var hud = ((InGameHudAccessor) inGameHud);
        var matrices = context.getMatrices();
        var player = hud.invokeGetCameraPlayer();

        if (player != null) {

            ItemStack itemStack = player.getOffHandStack();
            Arm arm = player.getMainArm().getOpposite();
            int i = context.getScaledWindowWidth() / 2;
            int j = context.getScaledWindowHeight();
            RenderSystem.enableBlend();
            matrices.push();
            matrices.translate(0.0F, 0.0F, -90.0F);

            matrices.scale(1.0F, 1.0F, 1.0F);
            context.drawTexture(customHotbar ? FaceTexture.CUSTOM_HOTBAR : FaceTexture.HOTBAR, i - 91, j - 22, 0, 0, 182, 22, 182, 22);

            var inventory = player.getInventory();
            int selectedSlot = inventory.selectedSlot;
            if (!customHotbar)
                context.drawGuiTexture(hud.hotbarSelectionTexture(), i - 91 - 1 + selectedSlot * 20, j - 23, 24, 23);
            else if (selectedSlot >= 4 && selectedSlot <= 8) {
                int visualIndex = selectedSlot - 4;
                context.drawGuiTexture(hud.hotbarSelectionTexture(), i - 91 - 1 + 30 + visualIndex * 25, j - 23, 24, 23);
            }

            if (!itemStack.isEmpty())
                if (arm == Arm.LEFT)
                    context.drawGuiTexture(hud.hotbarOffhandLeftTexture(), i - 91 - 29, j - 23, 29, 24);
                else
                    context.drawGuiTexture(hud.hotbarOffhandRightTexture(), i + 91, j - 23, 29, 24);

            matrices.pop();
            RenderSystem.disableBlend();
            int l = 1;

            for (int m = 0; m < 9; m++) {
                ItemStack mStack = player.getInventory().main.get(m);

                if (customHotbar) {
                    boolean isSpellSlot = m < 4;
                    int x = isSpellSlot ? (i - 90 + 42 + m * 25 + 2) + CONFIG.inventory.hotbar.offset.x : (i - 90 + 30 + (m - 4) * 25 + 2);
                    int y = isSpellSlot ? (j / 2) + CONFIG.inventory.hotbar.offset.y : (j - 16 - 3);
                    hud.invokeRenderHotbarItem(context, x, y, tickCounter, player, mStack, l++);
                } else {
                    int n = i - 90 + m * 20 + 2;
                    int o = context.getScaledWindowHeight() - 16 - 3;
                    hud.invokeRenderHotbarItem(context, n, o, tickCounter, player, mStack, l++);
                }
            }

            if (!itemStack.isEmpty()) {
                int m = j - 16 - 3;
                if (arm == Arm.LEFT) {
                    hud.invokeRenderHotbarItem(context, i - 91 - 26, m, tickCounter, player, itemStack, l++);
                } else {
                    hud.invokeRenderHotbarItem(context, i + 91 + 10, m, tickCounter, player, itemStack, l++);
                }
            }

            if (INSTANCE.CLIENT.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
                RenderSystem.enableBlend();
                assert INSTANCE.CLIENT.player != null;
                float f = INSTANCE.CLIENT.player.getAttackCooldownProgress(0.0F);
                if (f < 1.0F) {
                    int n = j - 20;
                    int o = i + 91 + 6;
                    if (arm == Arm.RIGHT) {
                        o = i - 91 - 22;
                    }

                    int p = (int) (f * 19.0F);
                    context.drawGuiTexture(hud.hotbarAttackIndicatorBackgroundTexture(), o, n, 18, 18);
                    context.drawGuiTexture(hud.hotbarAttackIndicatorProgressTexture(), 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
                }

                RenderSystem.disableBlend();
            }
        }

        ci.cancel();
    }

    public static void drawSlot_start(DrawContext context, Slot slot, CallbackInfo ci) {
        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand)
            return;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        RenderUtils.enableBlend();

        int x = slot.x, y = slot.y;
        var stack = slot.getStack();

        FaceItem item;
        boolean hideItem = false;

        if (stack.isEmpty() || IGNORED_ITEMS.contains(stack.getItem()))
            renderToolSlot(context, slot, x, y);
        else {
            item = FaceItem.from(stack);
            hideItem = searchbarCheck(item);
            renderNormalItem(context, matrices, item, x, y, hideItem);
        }

        RenderUtils.disableBlend();
        matrices.pop();

        if (hideItem && HIDDEN_ITEMS.contains(stack.getItem()))
            ci.cancel();
    }

    public static void drawItemInSlot(DrawContext context, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {

        if (!xyz.mashtoolz.config.FaceConfig.General.onFaceLand || !ABILITY_ITEMS.contains(stack.getItem()))
            return;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        RenderUtils.enableBlend();

        var spell = FaceSpell.from(stack);
        boolean hasEnchantment = !stack.getEnchantments().isEmpty();

        if (spell != null) {

            if ((spell.isToggled() && !hasEnchantment) || (!spell.isToggled() && hasEnchantment))
                spell.setToggled(hasEnchantment);
            spell.update(context, stack, x, y);
            int size = (int) spell.getSize() + 2;
            int offset = (size - 16) / 2;
            context.drawTexture(spell.getTexture(), x - offset, y - offset, 0, 0, size, size, size, size);
        }

        if (hasEnchantment)
            drawGlintAnimation(context, stack, x, y);

        RenderUtils.disableBlend();
        matrices.pop();

        if (!stack.isEmpty()) {
            matrices.push();
            if (stack.getCount() != 1 || countOverride != null) {
                String string = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
                matrices.translate(0.0F, 0.0F, 200.0F);
                context.drawText(CLIENT.textRenderer, string, x + 19 - 2 - CLIENT.textRenderer.getWidth(string), y + 6 + 3, 16777215, true);
                matrices.translate(0.0F, 0.0F, -200.0F);
            }

            if (stack.isItemBarVisible()) {
                int i = stack.getItemBarStep();
                int j = stack.getItemBarColor();
                int k = x + 2;
                int l = y + 13;
                context.fill(RenderLayer.getGuiOverlay(), k, l, k + 13, l + 2, Colors.BLACK);
                context.fill(RenderLayer.getGuiOverlay(), k, l, k + i, l + 1, j | Colors.BLACK);
            }
            matrices.pop();
        }
        ci.cancel();
    }

    private static boolean searchbarCheck(FaceItem item) {
        if (SEARCHBAR == null || !SEARCHBAR.highlighted)
            return false;

        var query = SEARCHBAR.getText();
        if (query.isEmpty())
            return true;

        var name = item.getName();
        if (name.equalsIgnoreCase("air"))
            return true;

        var tooltip = item.getTooltip();

        try {
            Pattern pattern = Pattern.compile(query, Pattern.DOTALL | (CONFIG.inventory.searchbar.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE));
            if (!pattern.matcher(tooltip).find() && !pattern.matcher(name).find())
                return true;
            SEARCHBAR.setEditableColor(0xFFFF78);
        } catch (PatternSyntaxException e) {
            SEARCHBAR.setEditableColor(0xFF7878);
        }

        return false;
    }

    private static void drawGlintAnimation(DrawContext context, ItemStack stack, int x, int y) {

        var spell = FaceSpell.from(stack);
        var matrices = context.getMatrices();
        int size = 16;
        float f = CLIENT.player == null ? 0.0F : CLIENT.player.getItemCooldownManager().getCooldownProgress(stack.getItem(), CLIENT.getRenderTickCounter().getTickDelta(true));
        boolean onCooldown = f != 0.0F || (1.0F - (float) stack.getDamage() / stack.getMaxDamage()) != 1.0F;
        int iSize = CLIENT.currentScreen instanceof HandledScreen || !CONFIG.inventory.hotbar.useCustom ? 16 : (spell != null && spell.isToggled()) ? 20 : onCooldown ? 16 : 20;

        int iOffset = (iSize - size) / 2;
        int maxFrames = 960 / size;
        int stepTime = 1200 / maxFrames;

        matrices.translate(0.0f, 0.0f, 400.0f);
        context.fill(x - iOffset, y - iOffset, x + iSize - iOffset, y + iSize - iOffset, ColorUtils.hex2Int("#000000", 0x78));

        RenderSystem.setShaderTexture(0, FaceTexture.ABILITY_GLINT);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
        context.drawTexture(FaceTexture.ABILITY_GLINT, x - iOffset + 1, y - iOffset + 1, iSize - 2, iSize - 2, (GLINT_FRAME * size), 0, size, size, maxFrames * size, size);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.drawTexture(FaceTexture.ABILITY_GLINT, x - iOffset, y - iOffset, iSize, iSize, (GLINT_FRAME * size), 0, size, size, maxFrames * size, size);
        matrices.translate(0.0f, 0.0f, -400.0f);

        if (System.currentTimeMillis() - GLINT_TIME >= stepTime) {
            GLINT_TIME = System.currentTimeMillis();
            GLINT_FRAME = (GLINT_FRAME + 1) % maxFrames;
        }
    }

    private static void renderToolSlot(DrawContext context, Slot slot, int x, int y) {
        if (!CONFIG.inventory.autoTool.enabled)
            return;

        var screen = (HandledScreenAccessor) CLIENT.currentScreen;
        assert screen != null;
        var handler = screen.getHandler();
        if (handler.slots.size() == 46 && !CLIENT.currentScreen.getTitle().getString().isEmpty()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
            for (var tool : FaceTool.values()) {
                if (slot.id == tool.getSlotIndex()) {
                    RenderSystem.setShaderTexture(0, tool.getTexture());
                    context.drawTexture(tool.getTexture(), x, y, 0, 0, 16, 16, 16, 16);
                    break;
                }
            }
        }
    }

    private static void renderNormalItem(DrawContext context, MatrixStack matrices, FaceItem item, int x, int y, boolean hideItem) {
        if (!CONFIG.inventory.itemColors.enabled)
            return;

        var type = item.getFaceType();
        var color = item.getColor();

        if (color == null || type.equals(FaceType.UNKNOWN)) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
            return;
        }

        float[] rgb = ColorUtils.getRGB(color);
        float opacity = hideItem ? 0.25F : CONFIG.inventory.itemColors.opacity;

        matrices.translate(0.0f, 0.0f, 100.0f);
        RenderSystem.setShaderTexture(0, FaceTexture.ITEM_GLOW);
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], opacity);

        if (CONFIG.inventory.itemColors.useTexture)
            context.drawTexture(FaceTexture.ITEM_GLOW, x, y, 0, 0, 16, 16, 16, 16);
        else
            context.drawBorder(x, y, 16, 16, ColorUtils.hex2Int("#FFFFFF", 0xFF));

        matrices.translate(0.0f, 0.0f, -100.0f);

        if (type.getString().startsWith("MATERIAL_")) {
            int stars = Integer.parseInt(type.getString().split("_")[1]);
            matrices.translate(0.0f, 0.0f, 300.0f);
            RenderSystem.setShaderTexture(0, FaceTexture.ITEM_STAR);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            context.drawTexture(FaceTexture.ITEM_STAR, x, y, 0, 0, 3 * stars, 3, 3, 3);
            matrices.translate(0.0f, 0.0f, -300.0f);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hideItem ? 0.25F : 1.0F);
    }

}
