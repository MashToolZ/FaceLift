package xyz.mashtoolz.helpers;

import java.util.ArrayList;
import java.util.List;

public class ArenaTimer {

	public RegexPattern[] regexes = new RegexPattern[] {
			new RegexPattern("subtitle.waveStart", "Wave (\\d+) has begun!"),
			new RegexPattern("title.waveEnd", "WAVE VANQUISHED!"),
			new RegexPattern("title.arenaEnd", "ARENA ENDED!")
	};

	public List<Wave> waves = new ArrayList<>();

	private boolean active = false;
	private boolean paused = false;

	public boolean isActive() {
		return active;
	}

	public boolean isPaused() {
		return paused;
	}

	public void start() {
		System.out.println("ArenaTimer.start()");
		waves.clear();
		active = true;
		paused = true;
	}

	public void end() {
		System.out.println("ArenaTimer.end()");
		active = false;
		paused = false;
	}

	public void startWave() {
		System.out.println("ArenaTimer.startWave()");
		Wave wave = new Wave();
		wave.setStartTime(System.currentTimeMillis());
		waves.add(wave);
		paused = false;
	}

	public void endWave() {
		System.out.println("ArenaTimer.endWave()");
		Wave wave = waves.get(waves.size() - 1);
		wave.setEndTime(System.currentTimeMillis());
		paused = true;
	}

	public long getWaveTime(int index) {
		long time = 0;
		var wave = waves.get(index);
		var endTime = wave.getEndTime();

		time = endTime > 0 ? endTime - wave.getStartTime() : System.currentTimeMillis() - wave.getStartTime();

		return time;
	}

	public long getCurrentWaveTime() {
		if (waves.isEmpty() || paused)
			return 0;
		int index = waves.size() - 1;
		return getWaveTime(index);
	}

	public long getTotalTime() {
		return System.currentTimeMillis() - waves.get(0).getStartTime();
	}

	public long getTotalTimeWithoutPauses() {
		long time = 0;
		for (var wave : waves) {
			var endTime = wave.getEndTime();
			if (endTime > 0)
				time += endTime - wave.getStartTime();
		}
		return time;
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
