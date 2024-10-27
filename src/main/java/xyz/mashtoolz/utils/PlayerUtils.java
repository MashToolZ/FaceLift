package xyz.mashtoolz.utils;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.custom.FaceEquipment;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceTool;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils {

    public static FaceLift INSTANCE = FaceLift.getInstance();

    private static void checkMount(MinecraftClient client) {
        if (INSTANCE.CONFIG.general.mountThirdPerson) {
            if (PlayerUtils.isMounted() && !FaceConfig.General.isMounted && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
                client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                xyz.mashtoolz.config.FaceConfig.General.isMounted = true;
            } else if (!PlayerUtils.isMounted() && xyz.mashtoolz.config.FaceConfig.General.isMounted && client.options.getPerspective() != Perspective.FIRST_PERSON) {
                client.options.setPerspective(Perspective.FIRST_PERSON);
                xyz.mashtoolz.config.FaceConfig.General.isMounted = false;
            }
        }
    }

    public static void update() {
        checkMount(INSTANCE.CLIENT);
    }

    public static boolean isMounted() {
        Entity player = INSTANCE.CLIENT.player;
        assert player != null;
        Entity ridingEntity = player.getVehicle();
        return ridingEntity != null && ridingEntity != player;
    }

    public static void clickSlot(int slotId, int button, SlotActionType actionType) {
        assert INSTANCE.CLIENT.player != null;
        int syncId = INSTANCE.CLIENT.player.currentScreenHandler.syncId;
        assert INSTANCE.CLIENT.interactionManager != null;
        INSTANCE.CLIENT.interactionManager.clickSlot(syncId, slotId, button, actionType, INSTANCE.CLIENT.player);
    }

    public static FaceTool getTargetTool(BlockHitResult blockHitResult, ItemStack currentStack) {
        var world = INSTANCE.CLIENT.world;
        assert INSTANCE.CLIENT.player != null;
        var blockView = INSTANCE.CLIENT.player.getWorld();
        var blockPos = blockHitResult.getBlockPos();
        assert world != null;
        var blockState = world.getBlockState(blockPos);
        var block = blockState.getBlock();

        if (block instanceof CropBlock cropBlock) {
            if (cropBlock.getAge(blockState) != cropBlock.getMaxAge())
                return null;
        }

        if (block.equals(Blocks.BEDROCK))
            return FaceTool.BEDROCK;

        var inventory = INSTANCE.CLIENT.player.getInventory();
        var cachedBlockPos = new CachedBlockPosition(blockView, blockPos, false);

        if (currentStack.canBreak(cachedBlockPos))
            return FaceTool.BEDROCK;

        List<FaceTool> possibleTools = new ArrayList<>();
        for (var slot : FaceEquipment.SLOTS) {
            if (!FaceEquipment.TOOL_TYPES.contains(slot.getFaceSlotType()))
                continue;

            var stack = inventory.getStack(slot.getIndex());
            if (stack.isEmpty() || !stack.canBreak(cachedBlockPos))
                continue;

            var item = FaceItem.from(stack);
            if (item.isInvalid())
                continue;

            var tool = item.getFaceTool();
            if (tool != null)
                possibleTools.add(tool);

        }

        if (possibleTools.size() == 1)
            return possibleTools.getFirst();

        if (possibleTools.contains(FaceTool.HOE) && possibleTools.contains(FaceTool.WOODCUTTINGAXE))
            return FaceTool.HOE;

        return null;
    }
}
