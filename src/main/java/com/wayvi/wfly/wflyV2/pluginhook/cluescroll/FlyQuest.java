package com.wayvi.wfly.wflyV2.pluginhook.cluescroll;

import com.electro2560.dev.cluescrolls.api.*;
import com.wayvi.wfly.wflyV2.WFlyV2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class FlyQuest {

    private final WFlyV2 plugin;

    public FlyQuest(WFlyV2 plugin) {
        this.plugin = plugin;
    }

    public void initializeFlyClue() {

        CustomClue flyClue = ClueScrollsAPI.getInstance().registerCustomClue(plugin, "wfly_flytime_to_fly", new ClueConfigData("wfly_flytime_to_fly", DataType.NUMBER_INTEGER));

        final ClueDataPair[] clueData = new ClueDataPair[]{
                new ClueDataPair("wfly_flytime_to_fly",""),
        };

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isFlying()) {
                    flyClue.handle(player, new ClueDataPair("wfly_flytime_to_fly","1"));
                }
            }
        }, 20L, 20L);
    }

}
