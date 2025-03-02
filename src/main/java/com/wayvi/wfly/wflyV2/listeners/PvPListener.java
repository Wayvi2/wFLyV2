package com.wayvi.wfly.wflyV2.listeners;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
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

public class PvPListener implements Listener {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private int FLY_DISABLE_RADIUS;
    private boolean featureActive;

    private final Map<UUID, Boolean> playerFlyState = new HashMap<>();
    private final Map<UUID, Location> playerLastSafeLocation = new HashMap<>();

    public PvPListener(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        reloadConfigValues();
    }

    public void reloadConfigValues() {
        FLY_DISABLE_RADIUS = configUtil.getCustomConfig().getInt("pvp.fly-disable-radius");
    }

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
            plugin.getFlyManager().manageFly(player.getUniqueId(), false);

            Location location = getSafeLocation(player);
            player.teleport(location);

            playerFlyState.put(player.getUniqueId(), false);
            playerLastSafeLocation.put(player.getUniqueId(), location);
            ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.player-in-range"));
        }
    }

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

    private Location getSafeLocation(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (y > 0 && world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ());
    }

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
