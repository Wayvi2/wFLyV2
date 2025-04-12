package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class FlyListener implements Listener {

    private final WFlyV2 plugin;
    private final FlyManager flyManager;
    private final ConfigUtil configUtil;

    public FlyListener(WFlyV2 plugin, FlyManager flyManager, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.flyManager = flyManager;
        this.configUtil = configUtil;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try {
            int remainingTime = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
            AccessPlayerDTO playerData = flyManager.getPlayerFlyData(player.getUniqueId());

            if (playerData != null) {
                WflyApi.get().getTimeFlyManager().upsertTimeFly(playerData.uniqueId(), remainingTime);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save flight time for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            AccessPlayerDTO playerData = flyManager.getPlayerFlyData(player.getUniqueId());

            if (playerData == null) {
                flyManager.createNewPlayer(player.getUniqueId());
            } else if (playerData.isinFly()) {
                flyManager.manageFly(player.getUniqueId(), true);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to restore fly state for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChangeGameMode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR && event.getNewGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean fly = WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId());
                if (fly) {
                    WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), true);
                }
            });
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                boolean authorized = WflyApi.get().getConditionManager().isFlyAuthorized(player);
                AccessPlayerDTO playerData = flyManager.getPlayerFlyData(player.getUniqueId());

                if (playerData != null && playerData.isinFly()) {
                    flyManager.manageFly(player.getUniqueId(), true);

                    String messageKey = authorized ? "message.fly-activated" : "message.fly-deactivated";
                    String message = configUtil.getCustomMessage().getString(messageKey);

                    ColorSupportUtil.sendColorFormat(player, message);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error during world change for player " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 15L);
    }
}
