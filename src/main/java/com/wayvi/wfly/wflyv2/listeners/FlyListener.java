package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.FlyManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
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

public class FlyListener implements Listener {

    // ══════════════════════════════════════════════════════
    //                 FIELDS & DEPENDENCIES
    // ══════════════════════════════════════════════════════

    private final WFlyV2 plugin;
    private final FlyManager flyManager;

    // ══════════════════════════════════════════════════════
    //                 CONSTRUCTOR & INIT
    // ══════════════════════════════════════════════════════

    /**
     * Creates a new FlyListener with required dependencies.
     *
     * @param plugin     the main plugin instance
     * @param flyManager the FlyManager instance managing flight data
     */
    public FlyListener(WFlyV2 plugin, FlyManager flyManager) {
        this.plugin = plugin;
        this.flyManager = flyManager;
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
        plugin.getStorage().save(player);

    }
    /**
     * Called when a player joins the server.
     * Loads fly time data and restore fly state if applicable.
     *
     * @param event the join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        WflyApi.get().getTimeFlyManager().loadFlyTimesForPlayer(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId())) {
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), true);
            }
        }, 1L);
    }

    /**
     * Called when a player changes game mode.
     * Specifically, handle the transition from SPECTATOR to SURVIVAL to restore flight if applicable.
     *
     * @param event the game mode change event
     */
    @EventHandler
    public void onPlayerChangeGameMode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR && event.getNewGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                boolean fly = WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId());
                if (fly) {
                    WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), true);
                }
            },7L);
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
            boolean authorized = WflyApi.get().getConditionManager().isFlyAuthorized(player);
            boolean isInFly = WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId());

            if (!isInFly){
                Location safeLoc = WflyApi.get().getConditionManager().getSafeLocation(player);
                if (safeLoc != null) {
                    player.teleport(safeLoc);
                }
            }

            if (isInFly) {
                flyManager.manageFly(player.getUniqueId(), authorized);

                String message = authorized ? plugin.getMessageFile().get(MessageEnum.FLY_ACTIVATED) : plugin.getMessageFile().get(MessageEnum.FLY_DEACTIVATED);


                ColorSupportUtil.sendColorFormat(player, message);
                boolean tpFloor = plugin.getConfigFile().get(ConfigEnum.TP_ON_FLOOR_WHEN_FLY_DISABLED);

                if ((!authorized && tpFloor) ) {
                    Location safeLoc = WflyApi.get().getConditionManager().getSafeLocation(player);
                    if (safeLoc != null) {
                        player.teleport(safeLoc);
                    }
                    ColorSupportUtil.sendColorFormat(player, plugin.getMessageFile().get(MessageEnum.NO_FLY_HERE));
                }

                if (!authorized) {
                    WflyApi.get().getConditionManager().executeNotAuthorizedCommands(player);
                }
            }
        }, 5L);
    }
}
