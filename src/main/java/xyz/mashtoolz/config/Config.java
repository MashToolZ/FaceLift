package xyz.mashtoolz.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.helpers.RegexPattern;
import xyz.mashtoolz.helpers.XPDisplay;
import xyz.mashtoolz.mixins.KeyBindingInterface;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class Config {

	private static final FaceLift instance = FaceLift.getInstance();

	private static MinecraftClient client;

	private static ConfigEntryBuilder entryBuilder;

	public static boolean onFaceLand = false;

	public static boolean isMounted = false;
	public static boolean inCombat = false;
	public static float hurtTime = 0;
	public static long lastHurtTime = 0;

	public static RegexPattern[] xpRegexes = new RegexPattern[] {
			new RegexPattern("fishingXP", "Gained Fishing XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("skillXP", "Gained (\\w+ ?){1,2} XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("combatXP", "\\+(\\d+)XP")
	};

	public static ArrayList<String> combatUnicodes = new ArrayList<>(Arrays.asList("丞", "丟"));

	public static Map<String, XPDisplay> xpDisplays = new HashMap<String, XPDisplay>();
	public static XPDisplay lastXPDisplay;

	public static Settings settings = Settings.getDefault();

	public static Category_General general = settings.general;
	public static Category_Inventory inventory = settings.inventory;
	public static Category_DPSMeter dpsMeter = settings.dpsMeter;
	public static Category_CombatTimer combatTimer = settings.combatTimer;
	public static Category_XPDisplay xpDisplay = settings.xpDisplay;
	public static Category_ArenaTimer arenaTimer = settings.arenaTimer;

	public static KeyBinding configKey = addKeybind("facelift.key.config");
	public static KeyBinding mountKey = addKeybind("facelift.key.mount");

	public static KeyBinding spell1Key = addKeybind("facelift.key.spell1");
	public static KeyBinding spell2Key = addKeybind("facelift.key.spell2");
	public static KeyBinding spell3Key = addKeybind("facelift.key.spell3");
	public static KeyBinding spell4Key = addKeybind("facelift.key.spell4");

	public static KeyBinding addKeybind(String key) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "facelift.title"));
	}

	public static boolean isPressed(KeyBinding key) {
		return InputUtil.isKeyPressed(client.getWindow().getHandle(), ((KeyBindingInterface) key).getBoundKey().getCode());
	}

	public static void load() {

		client = instance.client;

		try (FileReader reader = new FileReader("config/facelift.json")) {

			Gson gson = new Gson();
			Settings settings = gson.fromJson(reader, Settings.class);

			general.mountThirdPerson = settings.general.mountThirdPerson;
			general.tabHeightOffset = settings.general.tabHeightOffset;

			inventory.rarityOpacity = settings.inventory.rarityOpacity;
			inventory.searchbar = settings.inventory.searchbar;

			combatTimer.enabled = settings.combatTimer.enabled;
			combatTimer.position = settings.combatTimer.position;

			dpsMeter.enabled = settings.dpsMeter.enabled;
			dpsMeter.duration = settings.dpsMeter.duration;
			dpsMeter.position = settings.dpsMeter.position;

			xpDisplay.enabled = settings.xpDisplay.enabled;
			xpDisplay.duration = settings.xpDisplay.duration;
			xpDisplay.position = settings.xpDisplay.position;
			xpDisplay.showLastGain = settings.xpDisplay.showLastGain;
			xpDisplay.displayType = settings.xpDisplay.displayType;

			arenaTimer.enabled = settings.arenaTimer.enabled;
			arenaTimer.position = settings.arenaTimer.position;

		} catch (IOException | JsonSyntaxException e) {
			save();
		}
	}

	public static void save() {
		try (FileWriter writer = new FileWriter("config/facelift.json")) {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Settings settings = new Settings();

			settings.general.mountThirdPerson = general.mountThirdPerson;
			settings.general.tabHeightOffset = general.tabHeightOffset;

			settings.inventory.rarityOpacity = inventory.rarityOpacity;
			settings.inventory.searchbar = inventory.searchbar;

			settings.combatTimer.enabled = combatTimer.enabled;
			settings.combatTimer.position = combatTimer.position;

			settings.dpsMeter.enabled = dpsMeter.enabled;
			settings.dpsMeter.duration = dpsMeter.duration;
			settings.dpsMeter.position = dpsMeter.position;

			settings.xpDisplay.enabled = xpDisplay.enabled;
			settings.xpDisplay.duration = xpDisplay.duration;
			settings.xpDisplay.position = xpDisplay.position;
			settings.xpDisplay.showLastGain = xpDisplay.showLastGain;
			settings.xpDisplay.displayType = xpDisplay.displayType;

			settings.arenaTimer.enabled = arenaTimer.enabled;
			settings.arenaTimer.position = arenaTimer.position;

			gson.toJson(settings, writer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Screen getScreen() {

		Settings settings = Settings.getDefault();

		ConfigBuilder builder = ConfigBuilder.create().setParentScreen(client.currentScreen).setTitle(translatable("title"));
		entryBuilder = builder.entryBuilder();

		ConfigCategory _general = builder.getOrCreateCategory(translatable("config.general"));
		ConfigCategory _inventory = builder.getOrCreateCategory(translatable("config.inventory"));
		ConfigCategory _combatTimer = builder.getOrCreateCategory(translatable("config.combatTimer"));
		ConfigCategory _dpsMeter = builder.getOrCreateCategory(translatable("config.dpsMeter"));
		ConfigCategory _xpDisplay = builder.getOrCreateCategory(translatable("config.xpDisplay"));
		ConfigCategory _arenaTimer = builder.getOrCreateCategory(translatable("config.arenaTimer"));

		addConfigEntry(_general, "config.general.mountThirdPerson", general.mountThirdPerson, settings.general.mountThirdPerson, "config.general.mountThirdPerson.tooltip", newValue -> general.mountThirdPerson = newValue);
		addConfigEntry(_general, "config.general.tabHeightOffset", general.tabHeightOffset, settings.general.tabHeightOffset, "config.general.tabHeightOffset.tooltip", newValue -> general.tabHeightOffset = newValue);

		addConfigEntry(_inventory, "config.inventory.rarityOpacity", inventory.rarityOpacity, settings.inventory.rarityOpacity, "config.general.rarityOpacity.tooltip", newValue -> inventory.rarityOpacity = newValue);
		/*
		 * These are not meant to be visible
		 * 
		 * addConfigEntry(inventory, "config.inventory.searchbar_highlight", inventory.searchbar_highlight, settings.inventory.searchbar_highlight, "config.inventory.searchbar_highlight.tooltip", newValue -> inventory.searchbar_highlight = newValue);
		 * addConfigEntry(inventory, "config.inventory.searchbar_query", inventory.searchbar_query, settings.inventory.searchbar_query, "config.inventory.searchbar_query.tooltip", newValue -> inventory.searchbar_query = newValue);
		 */

		addConfigEntry(_combatTimer, "config.combatTimer.enabled", combatTimer.enabled, settings.combatTimer.enabled, "config.combatTimer.enabled.tooltip", newValue -> combatTimer.enabled = newValue);
		addConfigEntry(_combatTimer, "config.combatTimer.showTimebar", combatTimer.showTimebar, settings.combatTimer.showTimebar, "config.combatTimer.showTimebar.tooltip", newValue -> combatTimer.showTimebar = newValue);
		addConfigEntry(_combatTimer, "config.combatTimer.position.x", combatTimer.position.x, settings.combatTimer.position.x, "config.combatTimer.position.x.tooltip", newValue -> combatTimer.position.x = newValue);
		addConfigEntry(_combatTimer, "config.combatTimer.position.y", combatTimer.position.y, settings.combatTimer.position.y, "config.combatTimer.position.y.tooltip", newValue -> combatTimer.position.y = newValue);

		addConfigEntry(_dpsMeter, "config.dpsMeter.enabled", dpsMeter.enabled, settings.dpsMeter.enabled, "config.dpsMeter.enabled.tooltip", newValue -> dpsMeter.enabled = newValue);
		addConfigEntry(_dpsMeter, "config.dpsMeter.showTimebar", dpsMeter.showTimebar, settings.dpsMeter.showTimebar, "config.dpsMeter.showTimebar.tooltip", newValue -> dpsMeter.showTimebar = newValue);
		addConfigEntry(_dpsMeter, "config.dpsMeter.duration", dpsMeter.duration, settings.dpsMeter.duration, "config.dpsMeter.duration.tooltip", newValue -> dpsMeter.duration = newValue);
		addConfigEntry(_dpsMeter, "config.dpsMeter.position.x", dpsMeter.position.x, settings.dpsMeter.position.x, "config.dpsMeter.position.x.tooltip", newValue -> dpsMeter.position.x = newValue);
		addConfigEntry(_dpsMeter, "config.dpsMeter.position.y", dpsMeter.position.y, settings.dpsMeter.position.y, "config.dpsMeter.position.y.tooltip", newValue -> dpsMeter.position.y = newValue);

		addConfigEntry(_xpDisplay, "config.xpDisplay.enabled", xpDisplay.enabled, settings.xpDisplay.enabled, "config.xpDisplay.enabled.tooltip", newValue -> xpDisplay.enabled = newValue);
		addConfigEntry(_xpDisplay, "config.xpDisplay.showTimebar", xpDisplay.showTimebar, settings.xpDisplay.showTimebar, "config.xpDisplay.showTimebar.tooltip", newValue -> xpDisplay.showTimebar = newValue);
		addConfigEntry(_xpDisplay, "config.xpDisplay.duration", xpDisplay.duration, settings.xpDisplay.duration, "config.xpDisplay.duration.tooltip", newValue -> xpDisplay.duration = newValue);
		addConfigEntry(_xpDisplay, "config.xpDisplay.position.x", xpDisplay.position.x, settings.xpDisplay.position.x, "config.xpDisplay.position.x.tooltip", newValue -> xpDisplay.position.x = newValue);
		addConfigEntry(_xpDisplay, "config.xpDisplay.position.y", xpDisplay.position.y, settings.xpDisplay.position.y, "config.xpDisplay.position.y.tooltip", newValue -> xpDisplay.position.y = newValue);
		addConfigEntry(_xpDisplay, "config.xpDisplay.showLastGain", xpDisplay.showLastGain, settings.xpDisplay.showLastGain, "config.xpDisplay.showLastGain.tooltip", newValue -> xpDisplay.showLastGain = newValue);
		addConfigEntry(_xpDisplay, "config.xpDisplay.displayType", xpDisplay.displayType, settings.xpDisplay.displayType, "config.xpDisplay.displayType.tooltip", newValue -> xpDisplay.displayType = newValue);

		addConfigEntry(_arenaTimer, "config.arenaTimer.enabled", arenaTimer.enabled, settings.arenaTimer.enabled, "config.arenaTimer.enabled.tooltip", newValue -> arenaTimer.enabled = newValue);
		addConfigEntry(_arenaTimer, "config.arenaTimer.position.x", arenaTimer.position.x, settings.arenaTimer.position.x, "config.arenaTimer.position.x.tooltip", newValue -> arenaTimer.position.x = newValue);
		addConfigEntry(_arenaTimer, "config.arenaTimer.position.y", arenaTimer.position.y, settings.arenaTimer.position.y, "config.arenaTimer.position.y.tooltip", newValue -> arenaTimer.position.y = newValue);

		builder.setSavingRunnable(Config::save);

		return builder.build();
	}

	public static Text translatable(String key) {
		return Text.translatable("facelift." + key);
	}

	@SuppressWarnings("unchecked")
	private static <T> void addConfigEntry(ConfigCategory category, String key, T variable,
			T defaultValue, String tooltipKey, Consumer<? super T> saveConsumer) {

		switch (variable.getClass().getSimpleName()) {
			case "Boolean": {
				category.addEntry(entryBuilder.startBooleanToggle(translatable(key), (Boolean) variable)
						.setDefaultValue((Boolean) defaultValue).setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<Boolean>) saveConsumer).build());
				break;
			}

			case "String": {
				category.addEntry(entryBuilder.startStrField(translatable(key), (String) variable)
						.setDefaultValue((String) defaultValue).setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<String>) saveConsumer).build());
				break;
			}

			case "Integer": {
				category.addEntry(entryBuilder.startIntField(translatable(key), (Integer) variable)
						.setDefaultValue((Integer) defaultValue).setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<Integer>) saveConsumer).build());
				break;
			}

			case "Float": {
				category.addEntry(entryBuilder.startFloatField(translatable(key), (Float) variable)
						.setDefaultValue((Float) defaultValue).setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<Float>) saveConsumer).build());
				break;
			}
		}
	}
}
