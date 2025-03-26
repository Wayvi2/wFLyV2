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
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if certain configuration values are missing in the config.yml file. If missing,
     * it adds default values for those keys.
     */
    public void checkAndAddMissingLines() {
        if (!configConfig.contains("pvp.enabled-permission-range")) {
            configConfig.set("pvp.enabled-permission-range", false);
        }

        if (!configConfig.contains("pvp.fly-disable-radius")) {
            configConfig.set("pvp.fly-disable-radius", 5);
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
