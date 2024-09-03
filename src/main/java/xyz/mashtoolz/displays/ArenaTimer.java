package xyz.mashtoolz.displays;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.mixins.InGameHudAccessor;
import xyz.mashtoolz.utils.RegexPattern;
import xyz.mashtoolz.utils.RenderUtils;
import xyz.mashtoolz.utils.TimeUtils;

public class ArenaTimer {

	private static FaceLift instance = FaceLift.getInstance();

	public static RegexPattern[] regexes = new RegexPattern[] {
			new RegexPattern("subtitle.waveStart", "Wave (\\d+) has begun!"),
			new RegexPattern("title.waveEnd", "WAVE VANQUISHED!"),
			new RegexPattern("title.arenaEnd", "ARENA ENDED!"),
			new RegexPattern("title.arenaEnd", "ARENA COMPLETE!")
	};

	public static List<Wave> waves = new ArrayList<>();

	private static boolean active = false;
	private static boolean paused = false;

	public static boolean isActive() {
		return ArenaTimer.active;
	}

	public static boolean isPaused() {
		return ArenaTimer.paused;
	}

	public static void start() {
		ArenaTimer.waves.clear();
		ArenaTimer.active = true;
		ArenaTimer.paused = true;
	}

	public static void end() {
		ArenaTimer.active = false;
		ArenaTimer.paused = false;
	}

	public static void startWave() {
		Wave wave = new Wave();
		wave.setStartTime(System.currentTimeMillis());
		ArenaTimer.waves.add(wave);
		ArenaTimer.paused = false;
	}

	public static void endWave() {
		Wave wave = waves.get(waves.size() - 1);
		wave.setEndTime(System.currentTimeMillis());
		ArenaTimer.paused = true;
	}

	public static long getWaveTime(int index) {
		long time = 0;
		var wave = ArenaTimer.waves.get(index);
		var endTime = wave.getEndTime();
		time = endTime > 0 ? endTime - wave.getStartTime() : System.currentTimeMillis() - wave.getStartTime();
		return time;
	}

	public static long getCurrentWaveTime() {
		if (ArenaTimer.waves.isEmpty() || ArenaTimer.paused)
			return 0;
		int index = ArenaTimer.waves.size() - 1;
		return getWaveTime(index);
	}

	public static long getTotalTime() {
		return System.currentTimeMillis() - ArenaTimer.waves.get(0).getStartTime();
	}

	public static long getTotalTimeWithoutPauses() {
		long time = 0;
		for (var wave : ArenaTimer.waves) {
			var endTime = wave.getEndTime();
			if (endTime > 0)
				time += endTime - wave.getStartTime();
		}
		return time;
	}

	public static void updateTimer(DrawContext context) {

		var inGameHud = (InGameHudAccessor) instance.client.inGameHud;
		if (inGameHud == null)
			return;

		var title = inGameHud.getTitle();
		if (inGameHud.getTitle() == null)
			return;

		var subtitle = inGameHud.getSubtitle() != null ? inGameHud.getSubtitle() : Text.empty();

		for (var regex : ArenaTimer.regexes) {

			String[] arr = regex.getKey().split("\\.");
			var type = arr[0];
			var key = arr[1];

			var match = regex.getPattern().matcher(type.equals("title") ? title.getString() : subtitle.getString());

			if (!match.find())
				continue;

			switch (key) {
				case "waveStart": {
					if (!isActive())
						start();

					if (isPaused())
						startWave();
					break;
				}

				case "waveEnd": {
					if (isActive() && !isPaused())
						endWave();
					break;
				}

				case "arenaEnd": {
					if (isActive())
						end();
					break;
				}
			}
		}
	}

	public static void draw(DrawContext context) {
		if (!isActive())
			return;

		var totalTime = getTotalTime();

		var totalHMS = TimeUtils.timeToHMS(totalTime);
		var totalStr = String.format("%02d:%02d.%d", totalHMS[1], totalHMS[2], totalHMS[3]);
		var totalStrWidth = instance.client.textRenderer.getWidth(totalStr);

		var waveHMS = TimeUtils.timeToHMS(getCurrentWaveTime());
		var waveStr = String.format("%02d:%02d.%d", waveHMS[1], waveHMS[2], waveHMS[3]);
		var waveStrWidth = instance.client.textRenderer.getWidth(waveStr);

		int x = instance.config.combat.arenaTimer.position.x;
		int y = instance.config.combat.arenaTimer.position.y;

		context.fill(x, y, x + 112, y + RenderUtils.h(2) + 2, 0x80000000);
		RenderUtils.drawTextWithShadow(context, "§3Arena Timer", x + 5, y + 5);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + totalStr, x + 107 - totalStrWidth, y + 5);
		RenderUtils.drawTextWithShadow(context, "§bWave Timer", x + 5, y + 5 + 10);
		RenderUtils.drawTextWithShadow(context, "<#FDFDFD>" + waveStr, x + 107 - waveStrWidth, y + 5 + 10);
	}

	private static class Wave {

		private long start = 0;
		private long end = 0;

		public long getStartTime() {
			return start;
		}

		public void setStartTime(long time) {
			start = time;
		}

		public long getEndTime() {
			return end;
		}

		public void setEndTime(long time) {
			end = time;
		}
	}
}
