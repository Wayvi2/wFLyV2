package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import org.bukkit.Bukkit;

import java.util.logging.Level;

/**
 * Manages the integration of PlaceholderAPI within the WFlyV2 plugin.
 * It checks the presence of PlaceholderAPI and initializes necessary placeholders.
 */
public class PlaceholerapiManager {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;

    /**
     * Constructor to initialize the PlaceholderAPI manager.
     *
     * @param plugin    The WFlyV2 plugin instance.
     * @param configutil The ConfigUtil instance for managing configurations.
     */
    public PlaceholerapiManager(WFlyV2 plugin, ConfigUtil configutil) {
        this.plugin = plugin;
        this.configUtil = configutil;
    }

    /**
     * Checks if PlaceholderAPI is installed and enabled on the server.
     * If PlaceholderAPI is missing, the plugin is disabled with an error message.
     */
    public void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().log(Level.SEVERE, "Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Initializes and registers custom placeholders for WFlyV2 using PlaceholderAPI.
     * Creates a new instance of {@link WFlyPlaceholder} and registers the defined placeholders.
     */
    public void initialize() {
        new WFlyPlaceholder(plugin, configUtil).register();
    }
}
