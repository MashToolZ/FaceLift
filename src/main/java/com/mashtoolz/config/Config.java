package com.mashtoolz.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mashtoolz.FaceLift;
import com.mashtoolz.helpers.RegexPattern;
import com.mashtoolz.helpers.XPDisplay;

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

	private FaceLift instance = FaceLift.getInstance();

	private MinecraftClient client;

	public boolean mounted = false;
	public boolean inCombat = false;
	public float hurtTime = 0;
	public long lastHurtTime = 0;

	public RegexPattern[] regexes = new RegexPattern[] {
			new RegexPattern("skillXP", "Gained (\\w+) XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("combatXP", "\\+(\\d+)XP")
	};

	public Map<String, XPDisplay> xpDisplays = new HashMap<String, XPDisplay>();
	public XPDisplay lastXPDisplay;

	public Settings settings = Settings.getDefault();

	public KeyBinding configKey;
	public KeyBinding mountKey;

	public KeyBinding spell1Key;
	public KeyBinding spell2Key;
	public KeyBinding spell3Key;
	public KeyBinding spell4Key;

	public boolean mountThirdPerson = settings.mountThirdPerson;

	public boolean combatTimerEnabled = settings.combatTimerEnabled;
	public int combatTimerPosX = settings.combatTimerPosX;
	public int combatTimerPosY = settings.combatTimerPosY;

	public boolean dpsMeterEnabled = settings.dpsMeterEnabled;
	public int dpsMeterTime = settings.dpsMeterTime;
	public int dpsMeterPosX = settings.dpsMeterPosX;
	public int dpsMeterPosY = settings.dpsMeterPosY;

	public boolean xpDisplayEnabled = settings.xpDisplayEnabled;
	public int xpDisplayTime = settings.xpDisplayTime;
	public int xpDisplayPosX = settings.xpDisplayPosX;
	public int xpDisplayPosY = settings.xpDisplayPosY;

	public Config() {
		this.client = instance.client;

		this.configKey = addKeybind("facelift.key.config");
		this.mountKey = addKeybind("facelift.key.mount");

		this.spell1Key = addKeybind("facelift.key.spell1");
		this.spell2Key = addKeybind("facelift.key.spell2");
		this.spell3Key = addKeybind("facelift.key.spell3");
		this.spell4Key = addKeybind("facelift.key.spell4");

		loadConfig();
	}

	public KeyBinding addKeybind(String key) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "facelift.title"));
	}

	private void loadConfig() {
		try (FileReader reader = new FileReader("config/facelift.json")) {
			Gson gson = new Gson();
			Settings settings = gson.fromJson(reader, Settings.class);

			this.mountThirdPerson = settings.mountThirdPerson;

			this.combatTimerEnabled = settings.combatTimerEnabled;
			this.combatTimerPosX = settings.combatTimerPosX;
			this.combatTimerPosY = settings.combatTimerPosY;

			this.dpsMeterEnabled = settings.dpsMeterEnabled;
			this.dpsMeterTime = settings.dpsMeterTime;
			this.dpsMeterPosX = settings.dpsMeterPosX;
			this.dpsMeterPosY = settings.dpsMeterPosY;

			this.xpDisplayEnabled = settings.xpDisplayEnabled;
			this.xpDisplayTime = settings.xpDisplayTime;
			this.xpDisplayPosX = settings.xpDisplayPosX;
			this.xpDisplayPosY = settings.xpDisplayPosY;

		} catch (IOException | JsonSyntaxException e) {
			this.saveConfig();
		}
	}

	private void saveConfig() {
		try (FileWriter writer = new FileWriter("config/facelift.json")) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Settings settings = new Settings();

			settings.mountThirdPerson = this.mountThirdPerson;

			settings.combatTimerEnabled = this.combatTimerEnabled;
			settings.combatTimerPosX = this.combatTimerPosX;
			settings.combatTimerPosY = this.combatTimerPosY;

			settings.dpsMeterEnabled = this.dpsMeterEnabled;
			settings.dpsMeterTime = this.dpsMeterTime;
			settings.dpsMeterPosX = this.dpsMeterPosX;
			settings.dpsMeterPosY = this.dpsMeterPosY;

			settings.xpDisplayEnabled = this.xpDisplayEnabled;
			settings.xpDisplayTime = this.xpDisplayTime;
			settings.xpDisplayPosX = this.xpDisplayPosX;
			settings.xpDisplayPosY = this.xpDisplayPosY;

			gson.toJson(settings, writer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Screen getScreen() {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(client.currentScreen)
				.setTitle(translatable("title"));

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(translatable("config.category.general"));
		ConfigCategory combatTimer = builder.getOrCreateCategory(translatable("config.category.combatTimer"));
		ConfigCategory dpsMeter = builder.getOrCreateCategory(translatable("config.category.dpsMeter"));
		ConfigCategory xpDisplay = builder.getOrCreateCategory(translatable("config.category.xpDisplay"));

		addConfigEntry(entryBuilder, general, "config.mountThirdPerson", this.mountThirdPerson, settings.mountThirdPerson, "config.mountThirdPerson.tooltip", newValue -> this.mountThirdPerson = newValue);

		addConfigEntry(entryBuilder, combatTimer, "config.combatTimerEnabled", this.combatTimerEnabled, settings.combatTimerEnabled, "config.combatTimerEnabled.tooltip", newValue -> this.combatTimerEnabled = newValue);
		addConfigEntry(entryBuilder, combatTimer, "config.combatTimerPosX", this.combatTimerPosX, settings.combatTimerPosX, "config.combatTimerPosX.tooltip", newValue -> this.combatTimerPosX = newValue);
		addConfigEntry(entryBuilder, combatTimer, "config.combatTimerPosY", this.combatTimerPosY, settings.combatTimerPosY, "config.combatTimerPosY.tooltip", newValue -> this.combatTimerPosY = newValue);

		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeterEnabled", this.dpsMeterEnabled, settings.dpsMeterEnabled, "config.dpsMeterEnabled.tooltip", newValue -> this.dpsMeterEnabled = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeterTime", this.dpsMeterTime, settings.dpsMeterTime, "config.dpsMeterTime.tooltip", newValue -> this.dpsMeterTime = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeterPosX", this.dpsMeterPosX, settings.dpsMeterPosX, "config.dpsMeterPosX.tooltip", newValue -> this.dpsMeterPosX = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeterPosY", this.dpsMeterPosY, settings.dpsMeterPosY, "config.dpsMeterPosY.tooltip", newValue -> this.dpsMeterPosY = newValue);

		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplayEnabled", this.xpDisplayEnabled, settings.xpDisplayEnabled, "config.xpDisplayEnabled.tooltip", newValue -> this.xpDisplayEnabled = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplayTime", this.xpDisplayTime, settings.xpDisplayTime, "config.xpDisplayTime.tooltip", newValue -> this.xpDisplayTime = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplayPosX", this.xpDisplayPosX, settings.xpDisplayPosX, "config.xpDisplayPosX.tooltip", newValue -> this.xpDisplayPosX = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplayPosY", this.xpDisplayPosY, settings.xpDisplayPosY, "config.xpDisplayPosY.tooltip", newValue -> this.xpDisplayPosY = newValue);

		builder.setSavingRunnable(() -> {
			saveConfig();
		});

		return builder.build();
	}

	public Text translatable(String key) {
		return Text.translatable("facelift." + key);
	}

	@SuppressWarnings("unchecked")
	private <T> void addConfigEntry(ConfigEntryBuilder entryBuilder, ConfigCategory category, String key, T variable, T defaultValue, String tooltipKey, Consumer<? super T> saveConsumer) {

		switch (variable.getClass().getSimpleName()) {
			case "Boolean": {
				category.addEntry(entryBuilder.startBooleanToggle(translatable(key), (Boolean) variable)
						.setDefaultValue((Boolean) defaultValue)
						.setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<Boolean>) saveConsumer)
						.build());
				break;
			}

			case "String": {
				category.addEntry(entryBuilder.startStrField(translatable(key), (String) variable)
						.setDefaultValue((String) defaultValue)
						.setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<String>) saveConsumer)
						.build());
				break;
			}

			case "Integer": {
				category.addEntry(entryBuilder.startIntField(translatable(key), (Integer) variable)
						.setDefaultValue((Integer) defaultValue)
						.setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<Integer>) saveConsumer)
						.build());
				break;
			}

			case "Float": {
				category.addEntry(entryBuilder.startFloatField(translatable(key), (Float) variable)
						.setDefaultValue((Float) defaultValue)
						.setTooltip(translatable(tooltipKey))
						.setSaveConsumer((Consumer<Float>) saveConsumer)
						.build());
				break;
			}
		}
	}
}
