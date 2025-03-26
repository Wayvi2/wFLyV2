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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class TimeFlyManager {

    private final WFlyV2 plugin;
    private final RequestHelper requestHelper;
    private final ConfigUtil configUtil;
    private final Map<UUID, Integer> flyTimes = new ConcurrentHashMap<>();
    private Map<UUID, Boolean> isFlying = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastNotifiedTime = new ConcurrentHashMap<>();
    private final Set<UUID> needsUpdate = ConcurrentHashMap.newKeySet();
    Map<UUID, Location> lastSafeLocation = new HashMap<>();

    static int threads = Runtime.getRuntime().availableProcessors();
    public static ExecutorService sqlExecutor = Executors.newFixedThreadPool(threads);

    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.requestHelper = requestHelper;
        this.configUtil = configUtil;
        loadFlyTimes();
        startDecrementTask();
    }

    public void loadFlyTimes() {
        List<AccessPlayerDTO> flyData = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {
        });
        for (AccessPlayerDTO accessPlayerDTO : flyData) {
            upsertTimeFly(accessPlayerDTO.uniqueId(), accessPlayerDTO.FlyTimeRemaining());
            flyTimes.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.FlyTimeRemaining());
            isFlying.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.isinFly());

        }
    }

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

    public void SaveFlyTimeOnDisable(){
        for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {

            upsertTimeFly(entry.getKey(), entry.getValue());
            plugin.getLogger().info("Fly time saved");
        }
    }

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

    public void addFlytime(Player player, int time) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        int newTime = flyTimes.getOrDefault(playerUUID, 0) + time;
        flyTimes.put(playerUUID, newTime);
        needsUpdate.add(playerUUID);
    }

    public boolean removeFlyTime(Player sender , Player target, int time) {
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

    public void resetFlytime(Player player) {
        flyTimes.put(player.getUniqueId(), 0);
        needsUpdate.add(player.getUniqueId());
    }

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


    public int getTimeRemaining(Player player) {
        return flyTimes.getOrDefault(player.getUniqueId(), 0);
    }

    public void updateFlyStatus(UUID playerUUID, boolean isFlying) {
        this.isFlying.put(playerUUID, isFlying);
    }

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