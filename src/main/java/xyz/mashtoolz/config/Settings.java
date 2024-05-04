package xyz.mashtoolz.config;

public class Settings {

	Category_General general = Category_General.getDefault();
	Category_CombatTimer combatTimer = Category_CombatTimer.getDefault();
	Category_DPSMeter dpsMeter = Category_DPSMeter.getDefault();
	Category_XPDisplay xpDisplay = Category_XPDisplay.getDefault();
	Category_ArenaTimer arenaTimer = Category_ArenaTimer.getDefault();

	Category_XPCalc xpCalculator = Category_XPCalc.getDefault();

	public static Settings getDefault() {
		return new Settings();
	}
}
