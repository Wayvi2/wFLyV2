package com.wayvi.wfly.wflyV2.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {


    //message.yml
    private File messageFile;
    private FileConfiguration messageConfig;

    //config.yml
    private File configFile;
    private FileConfiguration configConfig;



    private FileConfiguration customConfig;
    private String version = "1.0.0.7";
    private final Plugin plugin;

    public ConfigUtil(Plugin plugin) {
        this.plugin = plugin;
        createCustomConfig();
    }



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

    public void ifNotExistCreateCustomConfig(File file, String name) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }
    }

    public void saveCustomConfig() {
        try {
            messageConfig.save(messageFile);
            configConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadCustomConfig() {
        try {
            messageConfig.load(messageFile);
            configConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public FileConfiguration getCustomConfig() {
        return this.configConfig;
    }

    public FileConfiguration getCustomMessage() {
        return this.messageConfig;
    }
}