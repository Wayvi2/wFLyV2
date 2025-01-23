package com.wayvi.wfly.wflyV2.listeners;

import com.wayvi.wfly.wflyV2.managers.fly.FlyManager;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {

    private final FlyManager flyManager;

    public PlayerJoinListener(FlyManager flyManager) {
        this.flyManager = flyManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        AccessPlayerDTO playerFlyData = flyManager.getPlayerFlyData(player);

        if (playerFlyData.isinFly()) {
            flyManager.manageFly(player, true);
        }
    }

}
