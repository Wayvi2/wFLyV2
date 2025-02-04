package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
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

    private static final ExecutorService sqlExecutor = Executors.newSingleThreadExecutor();

    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.requestHelper = requestHelper;
        this.configUtil = configUtil;
        loadFlyTimes();
        startDecrementTask();
    }

    public void loadFlyTimes() {
        List<AccessPlayerDTO> flyData = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});
        for (AccessPlayerDTO accessPlayerDTO : flyData) {
            flyTimes.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.FlyTimeRemaining());
            isFlying.put(accessPlayerDTO.uniqueId(), accessPlayerDTO.isinFly());
            //isFlying.put(UUID.fromString("f4cef720-d43b-4f2b-a3a0-71b77bfbbd47"), true);
        }
    }

    public void saveFlyTimes() {
        int seconds = configUtil.getCustomConfig().getInt("save-database-delay");
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {

                upsertTimeFly(entry.getKey(), entry.getValue());
                plugin.getLogger().info("Fly time saved");
            }
        }, 0L, 20L*seconds);
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


            if (player == null || !player.isOnline()) continue;

            if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                return;
            }


            int timeRemaining = this.flyTimes.getOrDefault(playerUUID, 0);
            boolean isFlying = this.isFlying.getOrDefault(playerUUID, false);

            if (timeRemaining == 0 && isFlying) {
                plugin.getFlyManager().manageFly(playerUUID, false);
                this.isFlying.put(playerUUID, false);
            }

            if (timeRemaining <= 0) continue;

            String decrementMethod = configUtil.getCustomConfig().getString("fly-decrement-method");

            assert decrementMethod != null;
            if (decrementMethod.equals("PLAYER_FLYING_MODE")) {
                if (isFlying && player.isFlying()) {
                    if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                        return;
                    }
                    timeRemaining--;
                    flyTimes.put(playerUUID, timeRemaining);
                }
            } else if (decrementMethod.equals("PLAYER_FLY_MODE")) {
                if (this.isFlying.getOrDefault(playerUUID, false)) {
                    if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                        return;
                    }
                    timeRemaining--;
                    flyTimes.put(playerUUID, timeRemaining);
                }
            }
        }
    }

    public void addFlytime(Player player, int time) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        int newTime = flyTimes.getOrDefault(playerUUID, 0) + time;
        flyTimes.put(playerUUID, newTime);
        upsertTimeFly(playerUUID, newTime);
    }

    public boolean removeFlyTime(Player player, int time) {
        UUID playerUUID = player.getUniqueId();
        int currentFlyTime = flyTimes.getOrDefault(playerUUID, 0);

        if (time > currentFlyTime) {
            ColorSupportUtil.sendColorFormat(player,configUtil.getCustomMessage().getString("message.fly-remove-too-high"));
            return false;
        }

        int newTime = currentFlyTime - time;
        flyTimes.put(playerUUID, newTime);
        upsertTimeFly(playerUUID, newTime);
        plugin.getTimeFlyManager().loadFlyTimes();
        return true;
    }

    public void resetFlytime(Player player) {
        flyTimes.put(player.getUniqueId(), 0);
        upsertTimeFly(player.getUniqueId(), 0);
        plugin.getTimeFlyManager().loadFlyTimes();
    }

    private void manageCommandMessageOnTimeLeft() throws SQLException {
        FileConfiguration config = configUtil.getCustomMessage();
        ConfigurationSection conditionsSection = config.getConfigurationSection("commands-time-remaining");
        if (conditionsSection == null) return;


        for (String key : conditionsSection.getKeys(false)) {
            int targetTime;
            try {
                targetTime = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            String command = conditionsSection.getString(key + ".commands");
            if (command == null) continue;

            for (UUID playerUUID : flyTimes.keySet()) {
                int playerTimeRemaining = flyTimes.get(playerUUID);
                if (playerTimeRemaining != targetTime || lastNotifiedTime.getOrDefault(playerUUID, -1) == targetTime) {
                    continue;
                }

                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) {
                    lastNotifiedTime.put(playerUUID, targetTime);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            }
        }
    }

    public void upsertTimeFly(@NotNull UUID playerUUID, int newTimeRemaining) {
        sqlExecutor.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", playerUUID).primary();
                try {
                    AccessPlayerDTO playerFlyData = plugin.getFlyManager().getPlayerFlyData(playerUUID);
                    table.bool("isinFly", playerFlyData.isinFly());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                table.bigInt("FlyTimeRemaining", newTimeRemaining);
            });
        });
    }

    public int getTimeRemaining(Player player) {
        return flyTimes.getOrDefault(player.getUniqueId(), 0);
    }

    public void updateFlyStatus(UUID playerUUID, boolean isFlying) {
        this.isFlying.put(playerUUID, isFlying);
    }

    public Boolean putDefaultFlyStatus(UUID playerUUID) {
        return this.isFlying.putIfAbsent(playerUUID, false);

    }
}
