package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.managers.WConditionManager;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.maxlego08.sarah.RequestHelper;
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

/**
 * Listener class to handle fly-related events for players.
 */
public class FlyListener implements Listener {

    private final WFlyV2 plugin;
    private final FlyManager flyManager;
    private final ConfigUtil configUtil;

    /**
     * Constructs the FlyListener.
     *
     * @param plugin           The main plugin instance.
     * @param flyManager       The fly manager handling flight mechanics.
     * @param configUtil       The configuration utility for retrieving messages.
     */
    public FlyListener(WFlyV2 plugin, FlyManager flyManager, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.flyManager = flyManager;
        this.configUtil = configUtil;
    }

    /**
     * Handles the event when a player quits the server.
     * Saves the remaining fly time to the database.
     *
     * @param event The PlayerQuitEvent.
     * @throws SQLException If an SQL error occurs.
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) throws SQLException {
        Player player = event.getPlayer();
        int timeRemaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
        AccessPlayerDTO playerData = this.flyManager.getPlayerFlyData(player.getUniqueId());

        WflyApi.get().getTimeFlyManager().upsertTimeFly(playerData.uniqueId(), timeRemaining);
    }

    /**
     * Handles the event when a player joins the server.
     * Restores their previous fly state.
     *
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            AccessPlayerDTO playerFlyData = flyManager.getPlayerFlyData(player.getUniqueId());
            if (playerFlyData == null) {
                flyManager.createNewPlayer(player.getUniqueId());
            } else if (playerFlyData.isinFly()) {
                flyManager.manageFly(player.getUniqueId(), true);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error managing fly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChangeGameMode(PlayerGameModeChangeEvent event)  {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR && event.getNewGameMode() == GameMode.SURVIVAL) {

            boolean fly = WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId());
            if (fly) {
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), true);
            }
        }
    }

    /**
     * Handles the event when a player changes worlds.
     * Adjusts their flight permission accordingly.
     *
     * @param event The PlayerChangedWorldEvent.
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean authorized = WflyApi.get().getConditionManager().isFlyAuthorized(player);
            String messageKey = authorized ? "message.fly-activated" : "message.fly-deactivated";


            try {
                AccessPlayerDTO playerData = this.flyManager.getPlayerFlyData(player.getUniqueId());
                boolean isinFly = playerData.isinFly();
                if (isinFly) {
                    this.flyManager.manageFly(player.getUniqueId(), true);
                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString(messageKey));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        }, 15L);
    }
}
