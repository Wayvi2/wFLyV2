package com.wayvi.wfly.wflyV2.api;

import com.wayvi.wfly.wflyV2.WFlyV2;
import org.bukkit.entity.Player;

public class WflyApi {

    private static WFlyV2 plugin;

    public static void inject(WFlyV2 plugin) {
        WflyApi.plugin = plugin;
    }

    public int getTimeRemaining(Player player) {
        return plugin.getTimeFlyManager().getTimeRemaining(player);
    }


    public WFlyV2 getPlugin() {
        return plugin;
    }





}
