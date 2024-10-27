package xyz.mashtoolz.displays;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.utils.ColorUtils;
import xyz.mashtoolz.utils.RenderUtils;

public class TeleportBar {

    private static final FaceLift INSTANCE = FaceLift.getInstance();

    private static double start = 0;
    private static double duration = 0;
    private static String text = "";

    public static void start(int duration, String text) {
        start = System.currentTimeMillis();
        TeleportBar.duration = duration;
        TeleportBar.text = text;
    }

    public static void stop() {
        start = 0;
        duration = 0;
        text = "";
    }

    public static void draw(DrawContext context) {

        double percent = duration == 0 ? 0 : (duration - (System.currentTimeMillis() - start)) / duration;
        if (percent <= 0)
            return;

        MatrixStack matrices = context.getMatrices();
        Window window = INSTANCE.CLIENT.getWindow();

        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();

        int width = INSTANCE.CONFIG.general.teleportBar.width;
        int height = INSTANCE.CONFIG.general.teleportBar.height;

        var fillWidth = (int) ((1 - percent) * (width));
        var textWidth = INSTANCE.CLIENT.textRenderer.getWidth(text) / 2;

        int xOffset = INSTANCE.CONFIG.general.teleportBar.offset.x;
        int yOffset = INSTANCE.CONFIG.general.teleportBar.offset.y;

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        float centerX = scaledWidth / 2f;
        float centerY = scaledHeight / 2f;

        matrices.translate(centerX - halfWidth + xOffset, centerY - halfHeight + yOffset, 0);

        RenderUtils.drawTextWithShadow(context, text, (int) (halfWidth - textWidth), -10);

        context.fill(0, 0, width, height, ColorUtils.hex2Int("#252320", 0xFF));
        context.drawBorder(0, 0, width, height, ColorUtils.hex2Int("#171717", 0xFF));
        context.fillGradient(1, 1, fillWidth, height - 1, ColorUtils.hex2Int("#A44FE9", 0xFF), ColorUtils.hex2Int("#C42CE3", 0xFF));

        matrices.translate((float) (-scaledWidth / 2 + width / 2), (float) -scaledHeight / 2 - 20, 0);
    }
}
