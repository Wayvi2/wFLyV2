package com.wayvi.wfly.wflyV2.listeners;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(player);


        AccessPlayerDTO playerData = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());

        plugin.getTimeFlyManager().upsertTimeFly(playerData.uniqueId(), timeRemaining);
    }

}
