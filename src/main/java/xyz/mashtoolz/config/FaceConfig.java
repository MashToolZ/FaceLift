package xyz.mashtoolz.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import xyz.mashtoolz.custom.FaceStatus;
import xyz.mashtoolz.structs.Pos2D;

@Config(name = "facelift")
public class FaceConfig implements ConfigData {

	@ConfigEntry.Category("general")
	@ConfigEntry.Gui.Excluded
	public static ConfigHolder<FaceConfig> holder;

	@ConfigEntry.Category("general")
	@ConfigEntry.Gui.TransitiveObject
	public General general = new General();

	public static class General {

		//

		@ConfigEntry.Gui.Excluded
		public static boolean onFaceLand = false;

		@ConfigEntry.Gui.Excluded
		public static boolean isMounted = false;

		@ConfigEntry.Gui.Excluded
		public static boolean inCombat = false;

		@ConfigEntry.Gui.Excluded
		public static float hurtTime = 0;

		@ConfigEntry.Gui.Excluded
		public static long lastHurtTime = 0;

		@ConfigEntry.Gui.Excluded
		public int curseStacks = 0;

		@ConfigEntry.Gui.Excluded
		public Map<FaceStatus, Long> statusEffects = Map.of();

		//

		@ConfigEntry.Gui.Tooltip
		public boolean mountThirdPerson = true;

		@ConfigEntry.Gui.Tooltip
		public boolean instantBowZoom = true;

		@ConfigEntry.Gui.Tooltip
		public int playerListHeightOffset = 25;

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public TeleportBar teleportBar = new TeleportBar();

		public static class TeleportBar {
			public boolean enabled = true;
		}

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public XPDisplay xpDisplay = new XPDisplay();

		public static class XPDisplay {
			public boolean enabled = true;
			public boolean showTimebar = true;
			public int duration = 5000;

			@ConfigEntry.Gui.Tooltip
			public boolean showLastGain = true;

			@ConfigEntry.Gui.TransitiveObject
			public Pos2D position = new Pos2D(5, 99);

			@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
			public DisplayType displayType = DisplayType.DEFAULT;

			public enum DisplayType {
				DEFAULT, PER_MINUTE, PER_HOUR
			}
		}
	}

	@ConfigEntry.Category("inventory")
	@ConfigEntry.Gui.TransitiveObject
	public Inventory inventory = new Inventory();

	public static class Inventory {

		@ConfigEntry.Gui.Excluded
		public List<String[]> equipmentSlots = new ArrayList<>();

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public ItemColors itemColors = new ItemColors();

		public static class ItemColors {
			public boolean enabled = true;

			@ConfigEntry.Gui.Tooltip
			public float opacity = 0.75F;

			@ConfigEntry.Gui.Tooltip
			public boolean useTexture = true;
		}

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public AutoTool autoTool = new AutoTool();

		public static class AutoTool {
			public boolean enabled = true;

			@ConfigEntry.Gui.Excluded
			public int PICKAXE = 15;
			@ConfigEntry.Gui.Excluded
			public int WOODCUTTINGAXE = 16;
			@ConfigEntry.Gui.Excluded
			public int HOE = 17;
		}

		@ConfigEntry.Gui.Excluded
		public Searchbar searchbar = new Searchbar();

		public static class Searchbar {
			public boolean caseSensitive = false;
			public boolean highlight = false;
			public String query = "";
		}
	}

	@ConfigEntry.Category("combat")
	@ConfigEntry.Gui.TransitiveObject
	public Combat combat = new Combat();

	public static class Combat {

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public CombatTimer combatTimer = new CombatTimer();

		public static class CombatTimer {
			public boolean enabled = true;

			@ConfigEntry.Gui.TransitiveObject
			public Pos2D position = new Pos2D(5, 5);
		}

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public DPSMeter dpsMeter = new DPSMeter();

		public static class DPSMeter {
			public boolean enabled = true;
			public boolean showTimebar = true;
			public int duration = 3000;

			@ConfigEntry.Gui.TransitiveObject
			public Pos2D position = new Pos2D(5, 37);
		}

		@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
		public ArenaTimer arenaTimer = new ArenaTimer();

		public static class ArenaTimer {
			public boolean enabled = true;

			@ConfigEntry.Gui.TransitiveObject
			public Pos2D position = new Pos2D(122, 20);
		}
	}

	public static void save() {
		holder.save();
	}
}