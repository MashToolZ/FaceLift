package xyz.mashtoolz.utils;

public class TimeUtils {

	public static long[] timeToHMS(long time) {
		var ms = time % 1000;
		time /= 1000;
		var s = time % 60;
		time /= 60;
		var m = time % 60;
		time /= 60;
		var h = time % 24;
		return new long[] { h, m, s, ms / 100 };
	}
}
