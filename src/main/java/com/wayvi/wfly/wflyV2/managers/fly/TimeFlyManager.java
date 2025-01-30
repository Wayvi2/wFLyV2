package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeFlyManager {

    public static ExecutorService service = Executors.newSingleThreadExecutor();

    private final RequestHelper requestHelper;

    private WFlyV2 plugin;

    private int timeRemaining;

    private BukkitTask timeTask;

    private ConfigUtil configUtil;

    private final Map<UUID, Integer> lastNotifiedTime = new HashMap<>();

    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    public void decrementTimeRemaining() {
        if (timeTask != null && !timeTask.isCancelled()) {
            timeTask.cancel();
        }

        timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                manageCommandMessageOnTimeLeft();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});
                for (AccessPlayerDTO accessPlayerDTO : fly) {

                    if (configUtil.getCustomConfig().getString("fly-decrement-method").equals("PLAYER_FLYING_MODE")) {
                        decrementTimeRemainingFlying();
                        return;
                    }

                    Player player = Bukkit.getPlayer(accessPlayerDTO.uniqueId());
                    timeRemaining = accessPlayerDTO.FlyTimeRemaining();

                    if (!accessPlayerDTO.isinFly() || !player.isOnline()) {
                        continue;
                    }

                    if (timeRemaining > 0) {
                        timeRemaining--;
                        upsertTimeFly(accessPlayerDTO.uniqueId(), timeRemaining);
                        return;
                    }

                    timeRemaining = 0;
                    upsertTimeFly(accessPlayerDTO.uniqueId(), timeRemaining);
                    plugin.getFlyManager().manageFly(accessPlayerDTO.uniqueId(), false);
                    plugin.getFlyManager().upsertFlyStatus(Bukkit.getPlayer(accessPlayerDTO.uniqueId()), false);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error to manage fly mode " + e.getMessage());
            }
        }, 20L, 20L);
    }



    //Manage the time remaining when the player is flying : equals to PLAYER_FLYING_MODE in config
    public void decrementTimeRemainingFlying() throws SQLException {
        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});

        for (AccessPlayerDTO accessPlayerDTO : fly) {
            Player player = Bukkit.getPlayer(accessPlayerDTO.uniqueId());
            timeRemaining = accessPlayerDTO.FlyTimeRemaining();

            if (!player.isFlying() || !player.isOnline()) {
                continue;
            }

            if (timeRemaining > 0) {
                timeRemaining--;
                upsertTimeFly(accessPlayerDTO.uniqueId(), timeRemaining);
                return;
            }

            timeRemaining = 0;
            upsertTimeFly(accessPlayerDTO.uniqueId(), timeRemaining);

            try {
                plugin.getFlyManager().manageFly(accessPlayerDTO.uniqueId(), false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            plugin.getFlyManager().upsertFlyStatus(Bukkit.getPlayer(accessPlayerDTO.uniqueId()), false);
        }
    }

    public void addFlytime(Player player, int time) throws SQLException {
            int flyTime = getTimeRemaining(player);
            int newTime = flyTime + time;
            upsertTimeFly(player.getUniqueId(), newTime);
    }

    public boolean removeFlytime(Player player, int time) throws SQLException {
        int flyTime = getTimeRemaining(player);
        int newTime = flyTime - time;
        if (newTime < 0) {
            player.sendMessage("§cYou can't remove too much time");
            player.sendMessage("§cMax time is " + flyTime);
            return false;
        }
        upsertTimeFly(player.getUniqueId(), newTime);
        return true;
    }

    public void resetFlytime(Player player) throws SQLException {
        upsertTimeFly(player.getUniqueId(), 0);
    }


    public void manageCommandMessageOnTimeLeft() throws SQLException {;

        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});
        FileConfiguration config = configUtil.getCustomMessage();
        if (!config.isConfigurationSection("commands-time-remaining")) {
            return;
        }

        ConfigurationSection conditionsSection = config.getConfigurationSection("commands-time-remaining");
        if (conditionsSection == null) {
            return;
        }

        for (String key : conditionsSection.getKeys(false)) {
            String command = conditionsSection.getString(key + ".commands");
            if (command == null) {
                continue;
            }


            int timeRemaining;
            try {
                timeRemaining = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            for (AccessPlayerDTO accessPlayerDTO : fly) {
                if (accessPlayerDTO.FlyTimeRemaining() != timeRemaining) {
                    continue;
                }

                int playerTimeRemaining = accessPlayerDTO.FlyTimeRemaining();
                UUID playerUUID = accessPlayerDTO.uniqueId();

                if (lastNotifiedTime.getOrDefault(playerUUID, -1) == playerTimeRemaining) {
                    continue;
                }

                Player player = Bukkit.getPlayer(accessPlayerDTO.uniqueId());
                if (player == null) {
                    continue;
                }

                lastNotifiedTime.put(playerUUID, playerTimeRemaining);
                command = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }




    public void upsertTimeFly(@NotNull UUID player, int newtimeRemaining) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", player).primary();
                try {
                    AccessPlayerDTO playerFlyData = plugin.getFlyManager().getPlayerFlyData(player);
                    table.bool("isinFly", playerFlyData.isinFly());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                table.bigInt("FlyTimeRemaining", newtimeRemaining);

            });
        });
    }

    public int getTimeRemaining(Player player) throws SQLException {
        AccessPlayerDTO fly = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());
        return fly.FlyTimeRemaining();
    }
}
