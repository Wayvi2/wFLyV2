package com.wayvi.wfly.wflyV2.api;

import com.wayvi.wfly.wflyV2.WFlyV2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WflyApi {

    private static WflyApi instance;
    private final WFlyV2 plugin;

    public WflyApi(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    public WflyApi getInstance() {
        if (instance == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("WFlyV2");
            if (plugin instanceof WFlyV2) {
                instance = new WflyApi((WFlyV2) plugin);
            } else {
                throw new IllegalStateException("WFlyV2 n'est pas chargé !");
            }
        }
        return instance;
    }

    public int getTimeFly(Player player) {
        return plugin.getTimeFlyManager().getTimeRemaining(player);
    }
}
