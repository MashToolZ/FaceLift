package xyz.mashtoolz.helpers;

import java.util.ArrayList;
import java.util.Arrays;

public class DPSMeter {

	public ArrayList<String> damageNumbers = new ArrayList<>(
			Arrays.asList("０", "１", "２", "３", "４", "５", "６", "７", "８", "９"));

	private long startTime;
	private long lastHitTime;
	private int damage;
	private int hits;

	public DPSMeter() {
		this.startTime = 0;
		this.lastHitTime = 0;
		this.damage = 0;
		this.hits = 0;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getLastHitTime() {
		return this.lastHitTime;
	}

	public void addDamage(int damage) {
		if (this.startTime == 0)
			this.startTime = System.currentTimeMillis();
		this.lastHitTime = System.currentTimeMillis();
		this.damage += damage;
		this.hits++;
	}

	public int getDamage() {
		return this.damage;
	}

	public int getHits() {
		return this.hits;
	}

	public void reset() {
		this.startTime = 0;
		this.damage = 0;
		this.hits = 0;
	}

	public int getDPS() {
		if (this.startTime == 0)
			return 0;
		var time = System.currentTimeMillis() - this.startTime;
		if (time == 0)
			return 0;
		return Math.round(this.damage / (time / 1000f));
	}

	public Integer parseDamage(String text) {
		StringBuilder damage = new StringBuilder();
		Arrays.stream(text.split("")).filter(segment -> damageNumbers.contains(segment))
				.forEach(segment -> damage.append(segment));

		if (damage.length() == 0) {
			return 0;
		}
		return Integer.parseInt(damage.toString());
	}
}
