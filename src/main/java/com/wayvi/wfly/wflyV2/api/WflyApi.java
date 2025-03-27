package com.wayvi.wfly.wflyV2.api;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.managers.fly.TimeFlyManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WflyApi {

    private static WFlyV2 plugin;

    public static void inject(WFlyV2 plugin) {
        WflyApi.plugin = plugin;
    }

    public int getTimeRemaining(Player player) {
        return plugin.getTimeFlyManager().getTimeRemaining(player);
    }





}
