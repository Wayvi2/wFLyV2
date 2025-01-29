package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.placeholders.TimeFlyPlaceholder;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class PlaceholerapiManager {
    private WFlyV2 plugin;
    private ConfigUtil configUtil;
    private MiniMessageSupportUtil miniMessageSupportUtil;


    public PlaceholerapiManager(WFlyV2 plugin, ConfigUtil configutil, MiniMessageSupportUtil miniMessageSupportUtil) {
        this.plugin = plugin;
        this.configUtil = configutil;
        this.miniMessageSupportUtil=miniMessageSupportUtil;
    }

    public void checkPlaceholderAPI() {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
                plugin.getLogger().log(Level.SEVERE,"Could not find PlaceholderAPI! This plugin is required.");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
    }


    public void initialize() {
        new TimeFlyPlaceholder(plugin, configUtil, miniMessageSupportUtil).register();
    }
}




