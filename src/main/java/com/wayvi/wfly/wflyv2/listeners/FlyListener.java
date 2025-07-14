package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class FlyListener implements Listener {

    // ══════════════════════════════════════════════════════
    //                 FIELDS & DEPENDENCIES
    // ══════════════════════════════════════════════════════

    private final WFlyV2 plugin;
    private final FlyManager flyManager;
    private final ConfigUtil configUtil;

    // ══════════════════════════════════════════════════════
    //                 CONSTRUCTOR & INIT
    // ══════════════════════════════════════════════════════

    /**
     * Creates a new FlyListener with required dependencies.
     *
     * @param plugin     the main plugin instance
     * @param flyManager the FlyManager instance managing flight data
     * @param configUtil the configuration utility for accessing config/messages
     */
    public FlyListener(WFlyV2 plugin, FlyManager flyManager, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.flyManager = flyManager;
        this.configUtil = configUtil;
    }

    // ══════════════════════════════════════════════════════
    //                 EVENT HANDLERS
    // ══════════════════════════════════════════════════════

    /**
     * Called when a player quits the server.
     * Saves the player's remaining fly time to the database.
     *
     * @param event the quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try {
            AccessPlayerDTO playerData = flyManager.getPlayerFlyData(player.getUniqueId());
            if (playerData != null) {
                WflyApi.get().getTimeFlyManager().saveInDbFlyTime(player);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save flight time for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called when a player joins the server.
     * Loads fly time data and restores fly state if applicable.
     *
     * @param event the join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (configUtil.getCustomConfig().getBoolean("mysql.enabled")) {
            WflyApi.get().getTimeFlyManager().loadFlyTimesForPlayer(player);
        }

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

    /**
     * Called when a player changes game mode.
     * Specifically handles the transition from SPECTATOR to SURVIVAL to restore flight if applicable.
     *
     * @param event the game mode change event
     */
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

    /**
     * Called when a player changes world.
     * Checks if player is authorized to fly in the new world, manages flight state,
     * sends messages, teleports to safe location if needed, and executes commands on unauthorized flight.
     *
     * @param event the world change event
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                boolean authorized = WflyApi.get().getConditionManager().isFlyAuthorized(player);
                AccessPlayerDTO playerData = flyManager.getPlayerFlyData(player.getUniqueId());

                if (playerData != null && playerData.isinFly()) {
                    flyManager.manageFly(player.getUniqueId(), authorized);

                    String messageKey = authorized ? "message.fly-activated" : "message.fly-deactivated";
                    String message = configUtil.getCustomMessage().getString(messageKey);

                    ColorSupportUtil.sendColorFormat(player, message);

                    if (!authorized && configUtil.getCustomConfig().getBoolean("tp-on-floor-when-fly-disabled")) {
                        Location safeLoc = WflyApi.get().getConditionManager().getSafeLocation(player);
                        player.teleport(safeLoc);
                    }

                    if (!authorized) {
                        WflyApi.get().getConditionManager().executeNotAuthorizedCommands(player);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error during world change for player " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 5L);
    }

}
