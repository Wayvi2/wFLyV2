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
    private WFlyPlaceholder wFlyPlaceholder;

    /**
     * Constructor to initialize the PlaceholderAPI manager.
     *
     * @param plugin    The WFlyV2 plugin instance.
     */
    public PlaceholerapiManager(WFlyV2 plugin) {
        this.plugin = plugin;

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
        this.wFlyPlaceholder = new WFlyPlaceholder(plugin);
        this.wFlyPlaceholder.register();
    }

    /**
     * Returns the instance of the registered WFlyPlaceholder.
     *
     * @return WFlyPlaceholder instance, or null if not initialized yet.
     */
    public WFlyPlaceholder getPlaceholder() {
        return wFlyPlaceholder;
    }
}

