package xyz.mashtoolz.custom;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.world.RaycastContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;
import xyz.mashtoolz.utils.PlayerUtils;

import java.util.Objects;

public class FaceTool {

    private static final FaceLift INSTANCE = FaceLift.getInstance();

    public static final FaceTool PICKAXE = new FaceTool(FaceToolType.PICKAXE, INSTANCE.CONFIG.inventory.autoTool.PICKAXE, FaceTexture.EMPTY_PICKAXE);
    public static final FaceTool WOODCUTTINGAXE = new FaceTool(FaceToolType.WOODCUTTINGAXE, INSTANCE.CONFIG.inventory.autoTool.WOODCUTTINGAXE, FaceTexture.EMPTY_WOODCUTTINGAXE);
    public static final FaceTool HOE = new FaceTool(FaceToolType.HOE, INSTANCE.CONFIG.inventory.autoTool.HOE, FaceTexture.EMPTY_HOE);
    public static final FaceTool BEDROCK = new FaceTool(FaceToolType.BEDROCK, -1, null);

    private final FaceToolType type;
    private int slotIndex;
    private final Identifier texture;

    private FaceTool(FaceToolType type, int slotIndex, Identifier texture) {
        this.type = type;
        this.slotIndex = slotIndex;
        this.texture = texture;
    }

    public FaceToolType getFaceToolType() {
        return type;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
        updateConfig();
    }

    private void updateConfig() {
        var autoTool = INSTANCE.CONFIG.inventory.autoTool;
        switch (type) {
            case PICKAXE -> autoTool.PICKAXE = slotIndex;
            case WOODCUTTINGAXE -> autoTool.WOODCUTTINGAXE = slotIndex;
            case HOE -> autoTool.HOE = slotIndex;
            default -> {
            }
        }
        FaceConfig.save();
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public Identifier getTexture() {
        return texture;
    }

    public static FaceTool[] values() {
        return new FaceTool[]{PICKAXE, WOODCUTTINGAXE, HOE, BEDROCK};
    }

    public static FaceTool getByType(FaceToolType type) {
        for (FaceTool tool : values())
            if (tool.type.equals(type))
                return tool;
        return null;
    }

    public static void update() {

        var client = INSTANCE.CLIENT;
        var player = client.player;
        assert player != null;
        var inventory = player.getInventory();
        var hotbarSlot = inventory.selectedSlot;
        var mainHandStack = player.getMainHandStack();

        var eyePos = player.getEyePos();
        var reach = player.getBlockInteractionRange();
        var rayEnd = eyePos.add(player.getRotationVector().multiply(reach));
        assert client.world != null;
        var blockHitResult = client.world.raycast(new RaycastContext(eyePos, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

        var faceItem = FaceItem.from(mainHandStack);
        var tooltip = faceItem.isInvalid() ? null : faceItem.getTooltip();
        var currentTool = getCurrentToolFromTooltip(tooltip);
        var targetTool = PlayerUtils.getTargetTool(blockHitResult, mainHandStack);

        if (targetTool != null && targetTool.getFaceToolType().equals(FaceToolType.BEDROCK))
            return;

        ClientPlayNetworkHandler networkHandler = Objects.requireNonNull(INSTANCE.CLIENT.getNetworkHandler());
        networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot != 8 ? 8 : 5));
        if (tooltip == null || currentTool == null) {
            handleNullTool(targetTool, hotbarSlot, inventory);
            networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
            return;
        }

        handleToolSwap(targetTool, currentTool, hotbarSlot, inventory);
        networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
    }

    private static FaceTool getCurrentToolFromTooltip(String tooltip) {
        if (tooltip == null)
            return null;

        for (var tool : FaceTool.values())
            if (tooltip.contains(tool.getFaceToolType().getName()))
                return tool;

        return null;
    }

    private static void handleNullTool(FaceTool targetTool, int hotbarSlot, PlayerInventory inventory) {
        if (targetTool != null && !inventory.getStack(targetTool.getSlotIndex()).isEmpty())
            PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
    }

    private static void handleToolSwap(FaceTool targetTool, FaceTool currentTool, int hotbarSlot, PlayerInventory inventory) {
        boolean isCurrentToolSlotEmpty = inventory.getStack(currentTool.getSlotIndex()).isEmpty();

        if (targetTool == null) {
            if (isCurrentToolSlotEmpty)
                swap(hotbarSlot, currentTool);
            else
                PlayerUtils.clickSlot(currentTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
            return;
        }

        if (currentTool.getFaceToolType().equals(targetTool.getFaceToolType()))
            return;

        if (isCurrentToolSlotEmpty) {
            swap(hotbarSlot, currentTool);
            PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
        } else {
            PlayerUtils.clickSlot(currentTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
            PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
        }
    }

    private static void swap(int hotbarSlot, FaceTool tool) {
        PlayerUtils.clickSlot(36 + hotbarSlot, 0, SlotActionType.PICKUP);
        PlayerUtils.clickSlot(tool.getSlotIndex(), 0, SlotActionType.PICKUP);
    }
}