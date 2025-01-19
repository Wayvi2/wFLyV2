package com.wayvi.wfly.wflyV2.managers;

import org.bukkit.entity.Player;

public class FlyManager {

    public void manageFly(Player player, boolean fly) {
        player.setAllowFlight(fly);
        player.setFlying(fly);
    }

    public boolean isFlying(Player player) {
        return player.isFlying();
    }


}
