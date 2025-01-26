package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.placeholders.TimeFlyPlaceholder;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class PlaceholerapiManager {
    WFlyV2 plugin;

    public PlaceholerapiManager(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    public void checkPlaceholderAPI() {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
                plugin.getLogger().log(Level.SEVERE,"Could not find PlaceholderAPI! This plugin is required.");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
    }


    public void initialize() {
        new TimeFlyPlaceholder(plugin).register();
    }
}




