package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class manages the fly time for players and coordinates the decrement of fly time, as well as handling the
 * fly status of players based on remaining time. It also manages saving and loading fly time to/from a database.
 */
public class TimeFlyManager {

    private final WFlyV2 plugin;
    private final RequestHelper requestHelper;
    private final ConfigUtil configUtil;
    private final Map<UUID, Integer> flyTimes = new ConcurrentHashMap<>();
    private Map<UUID, Boolean> isFlying = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastNotifiedTime = new ConcurrentHashMap<>();
    private final Set<UUID> needsUpdate = ConcurrentHashMap.newKeySet();
    private Map<UUID, Location> lastSafeLocation = new HashMap<>();

    static int threads = Runtime.getRuntime().availableProcessors();
    public static ExecutorService sqlExecutor = Executors.newFixedThreadPool(threads);

    /**
     * Constructor to initialize the TimeFlyManager with plugin dependencies and load existing fly times from the database.
     *
     * @param plugin         The main plugin instance.
     * @param requestHelper  Helper for database requests.
     * @param configUtil     Configuration utility for custom messages and settings.
     */
    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.requestHelper = requestHelper;
        this.configUtil = configUtil;
        loadFlyTimes();
        startDecrementTask();
    }



    /**
     * Loads fly time data from the database into memory.
     */
    public void loadFlyTimes() {
        List<AccessPlayerDTO> flyData = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {
        });
        for (AccessPlayerDTO accessPlayerDTO : flyData) {
            upsertTimeFly(accessPlayerDTO.uniqueId(), accessPlayerDTO.FlyTimeRemaining());
            flyTimes.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.FlyTimeRemaining());
            isFlying.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.isinFly());
        }
    }

    /**
     * Saves all updated fly times to the database periodically based on the configured delay.
     */
    public void saveFlyTimes() {
        int seconds = configUtil.getCustomConfig().getInt("save-database-delay");
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerUUID : needsUpdate) {
                int time = flyTimes.getOrDefault(playerUUID, 0);
                upsertTimeFly(playerUUID, time);
            }
            needsUpdate.clear();
        }, 0L, 20L * seconds);
    }

    /**
     * Saves fly times to the database when the server is disabled.
     */
    public void SaveFlyTimeOnDisable() {
        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
            upsertTimeFly(entry.getKey(), entry.getValue());
            plugin.getLogger().info("Fly time saved");
        }
    }

    /**
     * Starts a task to decrement the fly time of players who are flying and have a time limit.
     */
    private void startDecrementTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                decrementTimeRemaining();
                manageCommandMessageOnTimeLeft();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error managing fly time: " + e.getMessage());
            }

        }, 0, 20);
    }

    /**
     * Decrements the remaining fly time for each player and handles fly status accordingly.
     *
     * @throws SQLException If there is an issue with the database.
     */
    public void decrementTimeRemaining() throws SQLException {
        for (UUID playerUUID : flyTimes.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);

            if (player == null || !player.isOnline()) {
                continue;
            }
            if (player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()) {
                continue;
            }

            int timeRemaining = flyTimes.getOrDefault(playerUUID, 0);
            boolean currentlyFlying = isFlying.getOrDefault(playerUUID, false);

            if (timeRemaining <= 0 && currentlyFlying) {
                plugin.getFlyManager().manageFly(playerUUID, false);
                isFlying.put(playerUUID, false);

                Location safeLocation = getSafeLocation(player);
                if (!safeLocation.equals(lastSafeLocation.get(playerUUID))) {
                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));
                    if (player.getWorld().getEnvironment() != World.Environment.NETHER) {
                        player.teleport(safeLocation);
                        lastSafeLocation.put(playerUUID, safeLocation);
                    }
                }
                continue;
            }

            if (timeRemaining <= 0) {
                continue;
            }

            String decrementMethod = configUtil.getCustomConfig().getString("fly-decrement-method", "PLAYER_FLY_MODE");

            if (decrementMethod.equalsIgnoreCase("PLAYER_FLYING_MODE")) {
                if (currentlyFlying && player.isFlying()) {
                    timeRemaining--;
                    flyTimes.put(playerUUID, timeRemaining);
                    needsUpdate.add(playerUUID);
                }
            } else {
                if (currentlyFlying) {
                    timeRemaining--;
                    flyTimes.put(playerUUID, timeRemaining);
                    needsUpdate.add(playerUUID);
                }
            }
        }
    }

    /**
     * Adds fly time to a player's remaining time.
     *
     * @param player The player to add time for.
     * @param time   The amount of time to add (in seconds).
     * @throws SQLException If there is an issue with the database.
     */
    public void addFlytime(Player player, int time) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        int newTime = flyTimes.getOrDefault(playerUUID, 0) + time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
    }

    /**
     * Removes fly time from a player's remaining time.
     *
     * @param sender The player removing time.
     * @param target The target player whose time is being removed.
     * @param time   The amount of time to remove (in seconds).
     * @return True if time was successfully removed, false otherwise.
     */
    public boolean removeFlyTime(Player sender, Player target, int time) {
        UUID playerUUID = sender.getUniqueId();
        int currentFlyTime = getTimeRemaining(target);

        if (time > currentFlyTime) {
            ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage().getString("message.fly-remove-too-high"));
            return false;
        }

        int newTime = flyTimes.getOrDefault(playerUUID, 0) - time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
        return true;
    }

    /**
     * Resets the fly time for a player to 0.
     *
     * @param player The player whose fly time is being reset.
     */
    public void resetFlytime(Player player) {
        flyTimes.put(player.getUniqueId(), 0);
        needsUpdate.add(player.getUniqueId());
    }

    /**
     * Manages and executes commands based on the remaining fly time for each player.
     *
     * @throws SQLException If there is an issue with the database.
     */
    public void manageCommandMessageOnTimeLeft() throws SQLException {
        FileConfiguration config = configUtil.getCustomMessage();
        ConfigurationSection conditionsSection = config.getConfigurationSection("commands-time-remaining");
        if (conditionsSection == null) {
            return;
        }

        Map<Integer, String> timeCommandMap = new HashMap<>();
        for (String key : conditionsSection.getKeys(false)) {
            try {
                int timeKey = Integer.parseInt(key);
                String command = conditionsSection.getString(key + ".commands");
                if (command != null) {
                    timeCommandMap.put(timeKey, command);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
            UUID playerUUID = entry.getKey();
            int playerTimeRemaining = entry.getValue();

            if (lastNotifiedTime.getOrDefault(playerUUID, -1) == playerTimeRemaining) {
                continue;
            }

            String command = timeCommandMap.get(playerTimeRemaining);
            if (command == null) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue;
            }

            if (player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()) {
                continue;
            }

            lastNotifiedTime.put(playerUUID, playerTimeRemaining);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    /**
     * Upserts the fly time for a player in the database.
     *
     * @param playerUUID      The player's UUID.
     * @param newTimeRemaining The new fly time remaining (in seconds).
     */
    public void upsertTimeFly(@NotNull UUID playerUUID, int newTimeRemaining) {
        sqlExecutor.execute(() -> {
            List<AccessPlayerDTO> existingRecords = this.requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", playerUUID));

            if (existingRecords.isEmpty()) {
                this.requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", playerUUID).primary();
                    try {
                        AccessPlayerDTO playerFlyData = plugin.getFlyManager().getPlayerFlyData(playerUUID);
                        table.bool("isinFly", playerFlyData.isinFly());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    table.bigInt("FlyTimeRemaining", newTimeRemaining);
                });
            } else {
                this.requestHelper.update("fly", table -> {
                    table.where("uniqueId", playerUUID);
                    table.bool("isinFly", existingRecords.get(0).isinFly());
                    table.bigInt("FlyTimeRemaining", newTimeRemaining);
                });
            }
        });
    }

    /**
     * Gets the remaining fly time for a player.
     *
     * @param player The player to check.
     * @return The remaining fly time (in seconds).
     */
    public int getTimeRemaining(Player player) {
        return flyTimes.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Updates the flying status of a player.
     *
     * @param playerUUID The player's UUID.
     * @param isFlying   Whether the player is currently flying.
     */
    public void updateFlyStatus(UUID playerUUID, boolean isFlying) {
        this.isFlying.put(playerUUID, isFlying);
    }

    /**
     * Returns a safe location for the player to teleport to when their fly time expires.
     *
     * @param player The player to check.
     * @return A safe location for the player.
     */
    private Location getSafeLocation(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ());
    }
}
