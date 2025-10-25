package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.sql.SQLException;
import java.util.List;

/**
 * This listener handles all events related to the player's fly ability in PvP scenarios.
 * It disables flying when players are within a certain radius of each other and checks conditions based on placeholders.
 */
public class PvPListener implements Listener {


    private WFlyV2 plugin;

    private int FLY_DISABLE_RADIUS;

    /**
     * Constructs a PvPListener instance.
     */
    public PvPListener(WFlyV2 plugin) {
        this.plugin = plugin;
        reloadConfigValues();
    }

    /**
     * Reloads the configuration values for the PvP feature.
     */
    public void reloadConfigValues() {
        FLY_DISABLE_RADIUS = plugin.getConfigFile().get(ConfigEnum.PVP_FLY_DISABLE_RADIUS);
    }

    /**
     * Event handler for when a player moves. Disables the player's fly if they are within a certain range of another player.
     *
     * @param event The player move event.
     * @throws SQLException If there is an error accessing the database.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws SQLException {
        if (!isPvPFeatureActive()) return;

        Player player = event.getPlayer();
        if (isInBypassMode(player)) return;

        if (shouldDisableFly(player) && player.isFlying()) {
            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
            Location safeLoc = WflyApi.get().getConditionManager().getSafeLocation(player);
            if (safeLoc != null) {
                player.teleport(safeLoc);
            }
        }
    }

    private boolean isPvPFeatureActive() {
        return plugin.getConfigFile().get(ConfigEnum.PVP_ENABLED_PERMISSION_RANGE);
    }

    private boolean isInBypassMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE ||
                player.getGameMode() == GameMode.SPECTATOR ||
                player.hasPermission(Permissions.BYPASS_FLY.getPermission());
    }

    private boolean shouldDisableFly(Player player) {
        List<String> bypassPlaceholders = plugin.getConfigFile().get(ConfigEnum.PVP_BYPASS_PLACEHOLDERS);

        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (isPotentialThreat(player, nearbyPlayer)) {
                if (!haveMatchingPlaceholders(player, nearbyPlayer, bypassPlaceholders)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPotentialThreat(Player player, Player nearbyPlayer) {
        return !nearbyPlayer.equals(player)
                && nearbyPlayer.getWorld().equals(player.getWorld())
                && nearbyPlayer.getLocation().distance(player.getLocation()) <= FLY_DISABLE_RADIUS;
    }

    private boolean haveMatchingPlaceholders(Player player, Player nearbyPlayer, List<String> placeholders) {
        for (String placeholder : placeholders) {
            String playerValue = PlaceholderAPI.setPlaceholders(player, placeholder);
            String nearbyValue = PlaceholderAPI.setPlaceholders(nearbyPlayer, placeholder);

            if (!playerValue.equals(nearbyValue)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Event handler for when a player toggles flight. If the player is within the fly-disable radius of another player,
     * the flight toggle is canceled.
     *
     * @param event The player toggle flight event.
     */
    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        if (!isPvPFeatureActive()) return;

        Player player = event.getPlayer();

        if (isInBypassMode(player)) return;

        if (hasNearbyThreat(player)) {
            event.setCancelled(true);
        }
    }

    private boolean hasNearbyThreat(Player player) {
        List<String> bypassPlaceholders = plugin.getConfigFile().get(ConfigEnum.PVP_BYPASS_PLACEHOLDERS);

        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> isNearbyPlayerThreat(player, p))
                .anyMatch(p -> !haveMatchingPlaceholders(player, p, bypassPlaceholders));
    }

    private boolean isNearbyPlayerThreat(Player player, Player nearbyPlayer) {
        return !nearbyPlayer.equals(player)
                && nearbyPlayer.getWorld().equals(player.getWorld())
                && nearbyPlayer.getLocation().distance(player.getLocation()) <= FLY_DISABLE_RADIUS;
    }



    /**
     * Checks if there are any nearby players that would disable the player's flying.
     *
     * @param player The player to check for nearby players.
     * @return True if there are nearby players who disable flight, otherwise false.
     */
    public boolean HasNearbyPlayers(Player player) {
        List<String> bypassPlaceholders = plugin.getConfigFile().get(ConfigEnum.PVP_BYPASS_PLACEHOLDERS);

        return Bukkit.getOnlinePlayers().stream()
                .filter(nearbyPlayer -> isValidNearbyPlayer(player, nearbyPlayer))
                .anyMatch(nearbyPlayer -> !haveMatchingPlaceholders(player, nearbyPlayer, bypassPlaceholders));
    }

    private boolean isValidNearbyPlayer(Player player, Player nearbyPlayer) {
        return !nearbyPlayer.equals(player)
                && nearbyPlayer.getWorld().equals(player.getWorld())
                && nearbyPlayer.getLocation().distance(player.getLocation()) <= FLY_DISABLE_RADIUS;
    }

}
