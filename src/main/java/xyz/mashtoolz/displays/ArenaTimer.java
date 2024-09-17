package xyz.mashtoolz.displays;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.mixins.InGameHudAccessor;
import xyz.mashtoolz.structs.RegexPattern;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.utils.TimeUtils;

public class ArenaTimer {

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static final List<Wave> WAVES = new ArrayList<>();
	private static final RegexPattern[] REGEXES = {
			new RegexPattern("subtitle.waveStart", "Wave (\\d+) has begun!"),
			new RegexPattern("title.waveEnd", "WAVE VANQUISHED!"),
			new RegexPattern("title.arenaEnd", "ARENA ENDED!"),
			new RegexPattern("title.arenaEnd", "ARENA COMPLETE!")
	};

	private static boolean active = false;
	private static boolean paused = false;

	public static boolean isActive() {
		return active;
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void start() {
		WAVES.clear();
		active = true;
		paused = true;
	}

	public static void end() {
		active = false;
		paused = false;
	}

	public static void startWave() {
		WAVES.add(new Wave(System.currentTimeMillis()));
		paused = false;
	}

	public static void endWave() {
		Wave currentWave = WAVES.get(WAVES.size() - 1);
		currentWave.setEndTime(System.currentTimeMillis());
		paused = true;
	}

	public static long getWaveTime(int index) {
		Wave wave = WAVES.get(index);
		long endTime = wave.getEndTime();
		return endTime > 0 ? endTime - wave.getStartTime() : System.currentTimeMillis() - wave.getStartTime();
	}

	public static long getCurrentWaveTime() {
		return (WAVES.isEmpty() || paused) ? 0 : getWaveTime(WAVES.size() - 1);
	}

	public static long getTotalTime() {
		return System.currentTimeMillis() - WAVES.get(0).getStartTime();
	}

	public static long getTotalTimeWithoutPauses() {
		return WAVES.stream()
				.mapToLong(wave -> wave.getEndTime() > 0 ? wave.getEndTime() - wave.getStartTime() : 0)
				.sum();
	}

	public static void updateTimer(DrawContext context) {
		var inGameHud = (InGameHudAccessor) INSTANCE.CLIENT.inGameHud;
		if (inGameHud == null)
			return;

		Text title = inGameHud.getTitle();
		if (title == null)
			return;

		Text subtitle = inGameHud.getSubtitle() != null ? inGameHud.getSubtitle() : Text.empty();

		for (RegexPattern regex : REGEXES) {
			String[] parts = regex.getKey().split("\\.");
			String type = parts[0];
			String key = parts[1];

			String text = type.equals("title") ? title.getString() : subtitle.getString();
			var match = regex.getPattern().matcher(text);

			if (!match.find())
				continue;

			switch (key) {
				case "waveStart" -> {
					if (!isActive())
						start();
					if (isPaused())
						startWave();
				}
				case "waveEnd" -> {
					if (isActive() && !isPaused())
						endWave();
				}
				case "arenaEnd" -> {
					if (isActive())
						end();
				}
			}
		}
	}

	public static void draw(DrawContext context) {
		if (!isActive())
			return;

		long totalTime = getTotalTime();
		long currentWaveTime = getCurrentWaveTime();

		String totalStr = formatTime(totalTime);
		String waveStr = formatTime(currentWaveTime);

		int x = INSTANCE.CONFIG.combat.arenaTimer.position.x;
		int y = INSTANCE.CONFIG.combat.arenaTimer.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§3Arena Timer", x + 5, y + 5);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + totalStr, x + 107 - INSTANCE.CLIENT.textRenderer.getWidth(totalStr), y + 5);
		RenderUtils.drawTextWithShadow(context, "§bWave Timer", x + 5, y + 15);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + waveStr, x + 107 - INSTANCE.CLIENT.textRenderer.getWidth(waveStr), y + 15);
	}

	private static String formatTime(long time) {
		long[] hms = TimeUtils.timeToHMS(time);
		return String.format("%02d:%02d.%d", hms[1], hms[2], hms[3]);
	}

	private static class Wave {
		private final long start;
		private long end;

		public Wave(long start) {
			this.start = start;
			this.end = 0;
		}

		public long getStartTime() {
			return start;
		}

		public long getEndTime() {
			return end;
		}

		public void setEndTime(long end) {
			this.end = end;
		}
	}
}
