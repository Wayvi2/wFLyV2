package com.wayvi.wfly.wflyV2.listeners;

import com.wayvi.wfly.wflyV2.WFlyV2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerLeaveListener implements Listener {

    WFlyV2 plugin;

    public PlayerLeaveListener(WFlyV2 plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) throws SQLException {

        int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(event.getPlayer());
        plugin.getTimeFlyManager().upsertTimeFly(event.getPlayer(), timeRemaining);


    }

}
