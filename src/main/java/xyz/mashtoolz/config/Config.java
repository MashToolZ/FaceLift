package xyz.mashtoolz.config;

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
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.helpers.RegexPattern;
import xyz.mashtoolz.helpers.XPDisplay;
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

	public RegexPattern[] xpRegexes = new RegexPattern[] {
			new RegexPattern("skillXP", "Gained (\\w+ ?){1,2} XP! \\(\\+(\\d+)XP\\)"),
			new RegexPattern("combatXP", "\\+(\\d+)XP")
	};

	public Map<String, XPDisplay> xpDisplays = new HashMap<String, XPDisplay>();
	public XPDisplay lastXPDisplay;

	public Settings settings = Settings.getDefault();

	public Category_General general = settings.general;
	public Category_DPSMeter dpsMeter = settings.dpsMeter;
	public Category_CombatTimer combatTimer = settings.combatTimer;
	public Category_XPDisplay xpDisplay = settings.xpDisplay;
	public Category_ArenaTimer arenaTimer = settings.arenaTimer;

	public KeyBinding configKey;
	public KeyBinding mountKey;

	public KeyBinding potionKey;

	public KeyBinding spell1Key;
	public KeyBinding spell2Key;
	public KeyBinding spell3Key;
	public KeyBinding spell4Key;

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
		return KeyBindingHelper.registerKeyBinding(
				new KeyBinding(key, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "facelift.title"));
	}

	private void loadConfig() {
		try (FileReader reader = new FileReader("config/facelift.json")) {

			Gson gson = new Gson();
			Settings settings = gson.fromJson(reader, Settings.class);

			this.general.mountThirdPerson = settings.general.mountThirdPerson;

			this.combatTimer.enabled = settings.combatTimer.enabled;
			this.combatTimer.position = settings.combatTimer.position;

			this.dpsMeter.enabled = settings.dpsMeter.enabled;
			this.dpsMeter.duration = settings.dpsMeter.duration;
			this.dpsMeter.position = settings.dpsMeter.position;

			this.xpDisplay.enabled = settings.xpDisplay.enabled;
			this.xpDisplay.duration = settings.xpDisplay.duration;
			this.xpDisplay.position = settings.xpDisplay.position;
			this.xpDisplay.displayType = settings.xpDisplay.displayType;

			this.arenaTimer.enabled = settings.arenaTimer.enabled;
			this.arenaTimer.position = settings.arenaTimer.position;

		} catch (IOException | JsonSyntaxException e) {
			this.saveConfig();
		}
	}

	private void saveConfig() {
		try (FileWriter writer = new FileWriter("config/facelift.json")) {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Settings settings = new Settings();

			settings.general.mountThirdPerson = this.general.mountThirdPerson;

			settings.combatTimer.enabled = this.combatTimer.enabled;
			settings.combatTimer.position = this.combatTimer.position;

			settings.dpsMeter.enabled = this.dpsMeter.enabled;
			settings.dpsMeter.duration = this.dpsMeter.duration;
			settings.dpsMeter.position = this.dpsMeter.position;

			settings.xpDisplay.enabled = this.xpDisplay.enabled;
			settings.xpDisplay.duration = this.xpDisplay.duration;
			settings.xpDisplay.position = this.xpDisplay.position;
			settings.xpDisplay.displayType = this.xpDisplay.displayType;

			settings.arenaTimer.enabled = this.arenaTimer.enabled;
			settings.arenaTimer.position = this.arenaTimer.position;

			gson.toJson(settings, writer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Screen getScreen() {
		ConfigBuilder builder = ConfigBuilder.create().setParentScreen(client.currentScreen)
				.setTitle(translatable("title"));

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(translatable("config.general"));
		ConfigCategory combatTimer = builder.getOrCreateCategory(translatable("config.combatTimer"));
		ConfigCategory dpsMeter = builder.getOrCreateCategory(translatable("config.dpsMeter"));
		ConfigCategory xpDisplay = builder.getOrCreateCategory(translatable("config.xpDisplay"));
		ConfigCategory arenaTimer = builder.getOrCreateCategory(translatable("config.arenaTimer"));

		addConfigEntry(entryBuilder, general, "config.general.mountThirdPerson", this.general.mountThirdPerson,
				settings.general.mountThirdPerson, "config.general.mountThirdPerson.tooltip",
				newValue -> this.general.mountThirdPerson = newValue);
		addConfigEntry(entryBuilder, combatTimer, "config.combatTimer.enabled", this.combatTimer.enabled,
				settings.combatTimer.enabled, "config.combatTimer.enabled.tooltip",
				newValue -> this.combatTimer.enabled = newValue);
		addConfigEntry(entryBuilder, combatTimer, "config.combatTimer.position.x", this.combatTimer.position.x,
				settings.combatTimer.position.x, "config.combatTimer.position.x.tooltip",
				newValue -> this.combatTimer.position.x = newValue);
		addConfigEntry(entryBuilder, combatTimer, "config.combatTimer.position.y", this.combatTimer.position.y,
				settings.combatTimer.position.y, "config.combatTimer.position.y.tooltip",
				newValue -> this.combatTimer.position.y = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeter.enabled", this.dpsMeter.enabled,
				settings.dpsMeter.enabled, "config.dpsMeter.enabled.tooltip",
				newValue -> this.dpsMeter.enabled = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeter.duration", this.dpsMeter.duration,
				settings.dpsMeter.duration, "config.dpsMeter.duration.tooltip",
				newValue -> this.dpsMeter.duration = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeter.position.x", this.dpsMeter.position.x,
				settings.dpsMeter.position.x, "config.dpsMeter.position.x.tooltip",
				newValue -> this.dpsMeter.position.x = newValue);
		addConfigEntry(entryBuilder, dpsMeter, "config.dpsMeter.position.y", this.dpsMeter.position.y,
				settings.dpsMeter.position.y, "config.dpsMeter.position.y.tooltip",
				newValue -> this.dpsMeter.position.y = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplay.enabled", this.xpDisplay.enabled,
				settings.xpDisplay.enabled, "config.xpDisplay.enabled.tooltip",
				newValue -> this.xpDisplay.enabled = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplay.duration", this.xpDisplay.duration,
				settings.xpDisplay.duration, "config.xpDisplay.duration.tooltip",
				newValue -> this.xpDisplay.duration = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplay.position.x", this.xpDisplay.position.x,
				settings.xpDisplay.position.x, "config.xpDisplay.position.x.tooltip",
				newValue -> this.xpDisplay.position.x = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplay.position.y", this.xpDisplay.position.y,
				settings.xpDisplay.position.y, "config.xpDisplay.position.y.tooltip",
				newValue -> this.xpDisplay.position.y = newValue);
		addConfigEntry(entryBuilder, xpDisplay, "config.xpDisplay.displayType", this.xpDisplay.displayType,
				settings.xpDisplay.displayType, "config.xpDisplay.displayType.tooltip",
				newValue -> this.xpDisplay.displayType = newValue);
		addConfigEntry(entryBuilder, arenaTimer, "config.arenaTimer.enabled", this.arenaTimer.enabled,
				settings.arenaTimer.enabled, "config.arenaTimer.enabled.tooltip",
				newValue -> this.arenaTimer.enabled = newValue);
		addConfigEntry(entryBuilder, arenaTimer, "config.arenaTimer.position.x", this.arenaTimer.position.x,
				settings.arenaTimer.position.x, "config.arenaTimer.position.x.tooltip",
				newValue -> this.arenaTimer.position.x = newValue);
		addConfigEntry(entryBuilder, arenaTimer, "config.arenaTimer.position.y", this.arenaTimer.position.y,
				settings.arenaTimer.position.y, "config.arenaTimer.position.y.tooltip",
				newValue -> this.arenaTimer.position.y = newValue);

		builder.setSavingRunnable(() -> {
			saveConfig();
		});

		return builder.build();
	}

	public Text translatable(String key) {
		return Text.translatable("facelift." + key);
	}

	@SuppressWarnings("unchecked")
	private <T> void addConfigEntry(ConfigEntryBuilder entryBuilder, ConfigCategory category, String key, T variable,
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
