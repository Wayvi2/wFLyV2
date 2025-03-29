package com.wayvi.wfly.wflyV2.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to handle the loading, saving, and managing of configuration files
 * (config.yml and message.yml) for the plugin.
 * This class ensures that the necessary configuration files exist, creates them if missing,
 * and provides methods to access and modify the configurations.
 */
public class ConfigUtil {

    //message.yml
    private File messageFile;
    private FileConfiguration messageConfig;

    //config.yml
    private File configFile;
    private FileConfiguration configConfig;

    private FileConfiguration customConfig;
    private String version = "1.0.1.2";
    private final Plugin plugin;

    /**
     * Constructor that initializes the ConfigUtil class and ensures the custom config files are created.
     *
     * @param plugin the plugin instance that owns this configuration utility
     */
    public ConfigUtil(Plugin plugin) {
        this.plugin = plugin;
        createCustomConfig();
        checkAndAddMissingLines();
    }

    /**
     * Creates the custom configuration files (message.yml and config.yml) if they do not exist.
     * It also loads the configurations into memory for use.
     */
    public void createCustomConfig() {
        //message.yml
        messageFile = new File(plugin.getDataFolder(), "message.yml");
        ifNotExistCreateCustomConfig(messageFile, "message.yml");

        //config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        ifNotExistCreateCustomConfig(configFile, "config.yml");

        messageConfig = new YamlConfiguration();
        configConfig = new YamlConfiguration();

        reloadCustomConfig();
    }

    /**
     * Checks if the given configuration file exists. If not, it creates the necessary directories
     * and saves the resource to the disk.
     *
     * @param file the configuration file to check
     * @param name the name of the configuration file to save if it doesn't exist
     */
    public void ifNotExistCreateCustomConfig(File file, String name) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }
    }

    /**
     * Saves the current state of the message and config configurations to disk.
     * This method ensures any changes made to the configurations are persisted.
     */
    public void saveCustomConfig() {
        try {
            messageConfig.save(messageFile);
            configConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the message and config configurations from their respective files.
     * This is useful when you want to refresh the configuration data during runtime.
     */
    public void reloadCustomConfig() {
        try {
            messageConfig.load(messageFile);
            configConfig.load(configFile);
            checkAndAddMissingLines();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if certain configuration values are missing in the config.yml file. If missing,
     * it adds default values for those keys.
     */
    public void checkAndAddMissingLines() {
        // Version
        if (!configConfig.contains("version")) {
            configConfig.set("version", version);
        }

        // Save database delay
        if (!configConfig.contains("save-database-delay")) {
            configConfig.set("save-database-delay", 60);
        }

        // Fly decrement method
        if (!configConfig.contains("fly-decrement-method")) {
            configConfig.set("fly-decrement-method", "PLAYER_FLY_MODE");
        }

        // Format placeholder
        if (!configConfig.contains("format-placeholder.seconds")) {
            configConfig.set("format-placeholder.seconds", true);
        }
        if (!configConfig.contains("format-placeholder.minutes")) {
            configConfig.set("format-placeholder.minutes", false);
        }
        if (!configConfig.contains("format-placeholder.hours")) {
            configConfig.set("format-placeholder.hours", true);
        }
        if (!configConfig.contains("format-placeholder.days")) {
            configConfig.set("format-placeholder.days", true);
        }
        if (!configConfig.contains("format-placeholder.unlimited")) {
            configConfig.set("format-placeholder.unlimited", "Unlimited");
        }
        if (!configConfig.contains("format-placeholder.auto-format")) {
            configConfig.set("format-placeholder.auto-format", true);
        }
        if (!configConfig.contains("format-placeholder.remove-null-values.enabled")) {
            configConfig.set("format-placeholder.remove-null-values.enabled", true);
        }
        if (!configConfig.contains("format-placeholder.remove-null-values.value")) {
            configConfig.set("format-placeholder.remove-null-values.value", "#FFC77A0seconds");
        }
        if (!configConfig.contains("format-placeholder.format")) {
            configConfig.set("format-placeholder.format", "#FFC77A%seconds%#FF9D00%seconds_suffixe%#FFC77A%minutes%#FF9D00%minutes_suffixe% #FFC77A%hours%#FF9D00%hours_suffixe% #FFC77A%days%#FF9D00%days_suffixe%");
        }
        if (!configConfig.contains("format-placeholder.other-format.seconds_suffixe")) {
            configConfig.set("format-placeholder.other-format.seconds_suffixe", "seconds");
        }
        if (!configConfig.contains("format-placeholder.other-format.minutes_suffixe")) {
            configConfig.set("format-placeholder.other-format.minutes_suffixe", "minutes");
        }
        if (!configConfig.contains("format-placeholder.other-format.hours_suffixe")) {
            configConfig.set("format-placeholder.other-format.hours_suffixe", "hours");
        }
        if (!configConfig.contains("format-placeholder.other-format.days_suffixe")) {
            configConfig.set("format-placeholder.other-format.days_suffixe", "days");
        }

        // Conditions
        if (!configConfig.contains("conditions")) {
            configConfig.createSection("conditions");
        }
        if (!configConfig.contains("conditions.not-authorized.my-first-conditions")) {
            configConfig.set("conditions.not-authorized.my-first-conditions.placeholder", "%multiverse_world_alias%");
            configConfig.set("conditions.not-authorized.my-first-conditions.equals", "world");
            configConfig.set("conditions.not-authorized.my-first-conditions.command", "playsound minecraft:entity.enderman.teleport ambient %player% ~ ~ ~ 51000");
        }
        if (!configConfig.contains("conditions.authorized.my-seconds-conditions")) {
            configConfig.set("conditions.authorized.my-seconds-conditions.placeholder", "%multiverse_world_alias%");
            configConfig.set("conditions.authorized.my-seconds-conditions.equals", "world_nether");
        }

        // Teleport on floor when fly disabled
        if (!configConfig.contains("tp-on-floor-when-fly-disabled")) {
            configConfig.set("tp-on-floor-when-fly-disabled", true);
        }

        // PvP Settings
        if (!configConfig.contains("pvp.enabled-permission-range")) {
            configConfig.set("pvp.enabled-permission-range", false);
        }
        if (!configConfig.contains("pvp.fly-disable-radius")) {
            configConfig.set("pvp.fly-disable-radius", 5);
        }
        if (!configConfig.contains("pvp.bypass.placeholders")) {
            configConfig.set("pvp.bypass.placeholders", java.util.Arrays.asList("%lands_land_name_plain%"));
        }

        // Sauvegarde des modifications
        saveCustomConfig();
    }

    /**
     * Gets the version of the plugin.
     *
     * @return the version string of the plugin
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the custom config.yml file configuration.
     *
     * @return the custom config.yml file configuration
     */
    public FileConfiguration getCustomConfig() {
        return this.configConfig;
    }

    /**
     * Gets the custom message.yml file configuration.
     *
     * @return the custom message.yml file configuration
     */
    public FileConfiguration getCustomMessage() {
        return this.messageConfig;
    }
}
