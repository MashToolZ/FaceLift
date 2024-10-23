package xyz.mashtoolz.displays;

import net.minecraft.client.gui.DrawContext;
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
		var percent = duration == 0 ? 0 : (duration - (System.currentTimeMillis() - start)) / duration;
		if (percent <= 0)
			return;

		var matrices = context.getMatrices();
		var window = INSTANCE.CLIENT.getWindow();

		var width = window.getScaledWidth();
		var height = window.getScaledHeight();

		var barWidth = (int) ((1 - percent) * 100);
		var textWidth = INSTANCE.CLIENT.textRenderer.getWidth(text);

		matrices.translate((float) width / 2 - 50, (float) height / 2 + 14, 0);

		RenderUtils.drawTextWithShadow(context, text, 50 - textWidth / 2, -10);
		context.fill(0, 0, 100, 10, ColorUtils.hex2Int("#252320", 0xFF));
		context.drawBorder(0, 0, 100, 10, ColorUtils.hex2Int("#171717", 0xFF));
		context.fillGradient(1, 1, barWidth, 9, ColorUtils.hex2Int("#A44FE9", 0xFF), ColorUtils.hex2Int("#C42CE3", 0xFF));

		matrices.translate((float) -width / 2 + 50, (float) -height / 2 - 20, 0);
	}
}
