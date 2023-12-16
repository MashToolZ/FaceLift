package com.mashtoolz.config;

public class Settings {

	public boolean mountThirdPerson = true;

	public boolean combatTimerEnabled = true;
	public int combatTimerPosX = 5;
	public int combatTimerPosY = 5;

	public boolean dpsMeterEnabled = true;
	public int dpsMeterTime = 5000;
	public int dpsMeterPosX = 5;
	public int dpsMeterPosY = 37;

	public boolean xpDisplayEnabled = true;
	public int xpDisplayTime = 5000;
	public int xpDisplayPosX = 5;
	public int xpDisplayPosY = 99;

	public Settings() {
	}

	public static Settings getDefault() {
		return new Settings();
	}
}
