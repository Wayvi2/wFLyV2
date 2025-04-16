package com.wayvi.wfly.wflyv2.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

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

    private String version = "1.0.1.7";
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

        // Aliases
        if (!configConfig.contains("command.alias")) {
            configConfig.set("command.alias", new ArrayList<String>());
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
            configConfig.set("pvp.bypass.placeholders", Collections.singletonList("%lands_land_name_plain%"));
        }

        // Messages
        if (!messageConfig.contains("message.reload")) {
            messageConfig.set("message.reload", "&cPlugin has been reloaded!");
        }
        if (!messageConfig.contains("message.fly-activated")) {
            messageConfig.set("message.fly-activated", "&aYou have been set to fly! Use /fly to disable fly.");
        }
        if (!messageConfig.contains("message.fly-deactivated")) {
            messageConfig.set("message.fly-deactivated", "&cYou have been set to walk! Use /fly to enable fly.");
        }
        if (!messageConfig.contains("message.fly-speed-too-high")) {
            messageConfig.set("message.fly-speed-too-high", "&cSpeed too high! Maximum speed is %speed%");
        }
        if (!messageConfig.contains("message.fly-speed")) {
            messageConfig.set("message.fly-speed", "&aYou have set your fly speed to &e%speed%");
        }
        if (!messageConfig.contains("message.fly-speed-no-permission")) {
            messageConfig.set("message.fly-speed-no-permission", "&cYou do not have permission to set your fly speed to &e%speed%");
        }
        if (!messageConfig.contains("message.no-timefly-remaining")) {
            messageConfig.set("message.no-timefly-remaining", "&cYou have no timefly remaining!");
        }
        if (!messageConfig.contains("message.fly-time-added")) {
            messageConfig.set("message.fly-time-added", "&aYou have been given &e%time% &atimefly!");
        }
        if (!messageConfig.contains("message.fly-time-added-to-player")) {
            messageConfig.set("message.fly-time-added-to-player", "&aYou have been given &e%time% &atimefly to &e%player%");
        }
        if (!messageConfig.contains("message.fly-time-remove-to-player")) {
            messageConfig.set("message.fly-time-remove-to-player", "&aYou have removed &e%time% &atimefly from &e%player%");
        }
        if (!messageConfig.contains("message.fly-time-reset-to-player")) {
            messageConfig.set("message.fly-time-reset-to-player", "&aYou have been given reset &atimefly to &e%player%");
        }
        if (!messageConfig.contains("message.fly-time-removed")) {
            messageConfig.set("message.fly-time-removed", "&cYou have been taken &e%time% &ctimefly!");
        }
        if (!messageConfig.contains("message.fly-time-reset")) {
            messageConfig.set("message.fly-time-reset", "&aYou have been given &e0 &atimefly!");
        }
        if (!messageConfig.contains("message.no-fly-here")) {
            messageConfig.set("message.no-fly-here", "&cYou cannot fly here!");
        }
        if (!messageConfig.contains("message.fly-remove-too-high")) {
            messageConfig.set("message.fly-remove-too-high", "&cYou cannot remove too much timefly!");
        }
        if (!messageConfig.contains("message.no-permission")) {
            messageConfig.set("message.no-permission", "&cYou do not have permission to use this command!");
        }
        if (!messageConfig.contains("message.only-in-game")) {
            messageConfig.set("message.only-in-game", "&cThis command can only be used in game!");
        }
        if (!messageConfig.contains("message.missing-args")) {
            messageConfig.set("message.missing-args", "&cMissing arguments!");
        }
        if (!messageConfig.contains("message.arg-not-recognized")) {
            messageConfig.set("message.arg-not-recognized", "&cArgument not recognized!");
        }
        if (!messageConfig.contains("message.message-requirement")) {
            messageConfig.set("message.message-requirement", "&cYou do not meet the requirements to use this command!");
        }
        if (!messageConfig.contains("message.player-in-range")) {
            messageConfig.set("message.player-in-range", "&cPlayer is too far away!");
        }
        if (!messageConfig.contains("message.no-spectator")) {
            messageConfig.set("message.no-spectator", "&cYou can deactivate fly in spectator mode!");
        }

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
