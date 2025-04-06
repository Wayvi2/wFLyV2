package com.wayvi.wfly.wflyv2.listeners;

import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This listener handles all events related to the player's fly ability in PvP scenarios.
 * It disables flying when players are within a certain radius of each other and checks conditions based on placeholders.
 */
public class PvPListener implements Listener {

    private final ConfigUtil configUtil;
    private int FLY_DISABLE_RADIUS;
    private boolean featureActive;

    private final Map<UUID, Boolean> playerFlyState = new HashMap<>();
    private final Map<UUID, Location> playerLastSafeLocation = new HashMap<>();

    /**
     * Constructs a PvPListener instance.
     *
     * @param configUtil The configuration utility.
     */
    public PvPListener( ConfigUtil configUtil) {
        this.configUtil = configUtil;
        reloadConfigValues();
    }

    /**
     * Reloads the configuration values for the PvP feature.
     */
    public void reloadConfigValues() {
        FLY_DISABLE_RADIUS = configUtil.getCustomConfig().getInt("pvp.fly-disable-radius");
    }

    /**
     * Event handler for when a player moves. Disables the player's fly if they are within a certain range of another player.
     *
     * @param event The player move event.
     * @throws SQLException If there is an error accessing the database.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws SQLException {
        featureActive = configUtil.getCustomConfig().getBoolean("pvp.enabled-permission-range");
        if (!featureActive) return;

        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.hasPermission(Permissions.BYPASS_FLY.getPermission())) return;

        List<String> bypassPlaceholders = configUtil.getCustomConfig().getStringList("pvp.bypass.placeholders");

        boolean shouldDisableFly = false;

        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer != player && nearbyPlayer.getWorld().equals(player.getWorld())) {
                if (nearbyPlayer.getLocation().distance(player.getLocation()) <= FLY_DISABLE_RADIUS) {
                    boolean hasMatchingPlaceholder = true;

                    for (String placeholder : bypassPlaceholders) {
                        String playerPlaceholder = PlaceholderAPI.setPlaceholders(player, placeholder);
                        String nearbyPlayerPlaceholder = PlaceholderAPI.setPlaceholders(nearbyPlayer, placeholder);

                        if (!playerPlaceholder.equals(nearbyPlayerPlaceholder)) {
                            hasMatchingPlaceholder = false;
                            break;
                        }
                    }

                    if (!hasMatchingPlaceholder) {
                        shouldDisableFly = true;
                        break;
                    }
                }
            }
        }

        if (shouldDisableFly && player.isFlying()) {
            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);


            Location location = getSafeLocation(player);
            player.teleport(location);

            playerFlyState.put(player.getUniqueId(), false);
            playerLastSafeLocation.put(player.getUniqueId(), location);
            ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.player-in-range"));
        }
    }

    /**
     * Event handler for when a player toggles flight. If the player is within the fly-disable radius of another player,
     * the flight toggle is canceled.
     *
     * @param event The player toggle flight event.
     */
    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        featureActive = configUtil.getCustomConfig().getBoolean("pvp.enabled-permission-range");
        if (!featureActive) return;

        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission(Permissions.BYPASS_FLY.getPermission())) return;

        List<String> bypassPlaceholders = configUtil.getCustomConfig().getStringList("pvp.bypass.placeholders");

        boolean shouldDisableFly = false;

        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer != player && nearbyPlayer.getWorld().equals(player.getWorld())) {
                if (nearbyPlayer.getLocation().distance(player.getLocation()) <= FLY_DISABLE_RADIUS) {
                    boolean hasMatchingPlaceholder = true;

                    for (String placeholder : bypassPlaceholders) {
                        String playerPlaceholder = PlaceholderAPI.setPlaceholders(player, placeholder);
                        String nearbyPlayerPlaceholder = PlaceholderAPI.setPlaceholders(nearbyPlayer, placeholder);

                        if (!playerPlaceholder.equals(nearbyPlayerPlaceholder)) {
                            hasMatchingPlaceholder = false;
                            break;
                        }
                    }

                    if (!hasMatchingPlaceholder) {
                        shouldDisableFly = true;
                        break;
                    }
                }
            }
        }

        if (shouldDisableFly) {
            event.setCancelled(true);
        }
    }

    /**
     * Gets a safe location for the player to teleport to if they need to be grounded.
     *
     * @param player The player to find a safe location for.
     * @return The safe location for the player to teleport to.
     */
    private Location getSafeLocation(Player player) {
        boolean tpOnFloorWhenFlyDisabled = configUtil.getCustomConfig().getBoolean("tp-on-floor-when-fly-disabled");
        if (!tpOnFloorWhenFlyDisabled) {
            return player.getLocation();
        }

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Checks if there are any nearby players that would disable the player's flying.
     *
     * @param player The player to check for nearby players.
     * @return True if there are nearby players who disable flight, otherwise false.
     */
    public boolean HasNearbyPlayers(Player player) {
        List<String> bypassPlaceholders = configUtil.getCustomConfig().getStringList("pvp.bypass.placeholders");

        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer != player && nearbyPlayer.getWorld().equals(player.getWorld())) {
                if (nearbyPlayer.getLocation().distance(player.getLocation()) <= FLY_DISABLE_RADIUS) {
                    boolean hasMatchingPlaceholder = true;

                    for (String placeholder : bypassPlaceholders) {
                        String playerPlaceholder = PlaceholderAPI.setPlaceholders(player, placeholder);
                        String nearbyPlayerPlaceholder = PlaceholderAPI.setPlaceholders(nearbyPlayer, placeholder);

                        if (!playerPlaceholder.equals(nearbyPlayerPlaceholder)) {
                            hasMatchingPlaceholder = false;
                            break;
                        }
                    }

                    if (!hasMatchingPlaceholder) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
