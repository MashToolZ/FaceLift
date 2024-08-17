package xyz.mashtoolz.utils;

import java.util.regex.Pattern;

import net.minecraft.client.gui.DrawContext;
import xyz.mashtoolz.FaceLift;

public class RenderUtils {

	private static FaceLift instance = FaceLift.getInstance();

	public static void drawTextWithShadow(DrawContext context, String text, int x, int y) {

		int color = 0xD1D1D1;
		var pattern = Pattern.compile("<#([A-Fa-f0-9]{6,8})>");
		var matcher = pattern.matcher(text);
		var segments = text.split(pattern.pattern());

		for (var segment : segments) {
			context.drawTextWithShadow(instance.client.textRenderer, segment, x, y, color);
			x += instance.client.textRenderer.getWidth(segment);

			if (matcher.find()) {
				var group = matcher.group(1);
				var hex = group.substring(0, 6);
				int opacity = 0xFF;
				if (group.length() == 8) {
					hex = group.substring(2, 8);
					opacity = Math.max(4, Math.min(255, Integer.parseInt(group.substring(0, 2), 16)));
				}

				color = ColorUtils.hex2Int(hex, opacity);
			} else {
				color = 0xD1D1D1;
			}
		}
	}

	public static void drawTimeBar(DrawContext context, int x, int y, int remaining, int max, int color) {
		int barWidth = x + 6 + Math.round(remaining * 101 / max);
		context.fill(x + 5, y + 15, x + 107, y + 22, ColorUtils.hex2Int("D1D1D1", 0x40));
		context.fill(x + 6, y + 16, x + 106, y + 21, ColorUtils.hex2Int("D1D1D1", 0x40));
		context.fill(x + 6, y + 16, barWidth, y + 21, color);
	}

	public static int w(int n) {
		return 107 + n;
	}

	public static int h(int n) {
		return 10 * n + 5;
	}

	public static int lh(int n) {
		return 10 * n;
	}

	public static int tbh(int n) {
		return 22 + n;
	}

	public static void drawLine(DrawContext context, int x0, int y0, int x1, int y1, int color, int lineWidth) {
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;

		int err = dx - dy;

		while (true) {
			if (dx > dy) {
				for (int i = -lineWidth / 2; i <= lineWidth / 2; i++) {
					context.fill(x0, y0 + i, x0 + 1, y0 + i + 1, color);
				}
			} else {
				for (int i = -lineWidth / 2; i <= lineWidth / 2; i++) {
					context.fill(x0 + i, y0, x0 + i + 1, y0 + 1, color);
				}
			}

			if (x0 == x1 && y0 == y1)
				break;

			int e2 = err << 1;

			if (e2 > -dy) {
				err -= dy;
				x0 += sx;
			}
			if (e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}
}
