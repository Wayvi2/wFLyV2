package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeFlyManager {

    public static ExecutorService service = Executors.newSingleThreadExecutor();

    private final RequestHelper requestHelper;

    private WFlyV2 plugin;

    private int timeRemaining;

    private BukkitTask timeTask;

    private MiniMessageSupportUtil miniMessageSupportUtil;

    private ConfigUtil configUtil;

    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper, MiniMessageSupportUtil miniMessageSupportUtil, ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
        this.configUtil = configUtil;
    }

/*
    public void decrementTimeRemaining(Player player, boolean isFlyEnabled) throws SQLException {
        if (timeTask != null && !timeTask.isCancelled()) {
            timeTask.cancel();
        }

        if (isFlyEnabled) {


            timeRemaining = getTimeRemaining(player);
            timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

                if (timeRemaining <= 1) {
                    timeRemaining = 0;
                    upsertTimeFly(player, timeRemaining);

                    try {
                        plugin.getFlyManager().manageFly(player, false);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    timeTask.cancel();
                    return;
                }
                timeRemaining--;

            }, 20L, 20L);
        } else {

            upsertTimeFly(player, timeRemaining);
            timeTask.cancel();
        }
    }

     */
    public void decrementTimeRemaining() {
        if (timeTask != null && !timeTask.isCancelled()) {
            timeTask.cancel();
        }

        timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});

                for (AccessPlayerDTO accessPlayerDTO : fly) {
                    timeRemaining = accessPlayerDTO.FlyTimeRemaining();

                    if (accessPlayerDTO.isinFly()) { // Vérifie si le joueur est en fly
                        if (timeRemaining > 0) {
                            timeRemaining--; // Diminue le temps restant
                            upsertTimeFly(accessPlayerDTO.uniqueId(), timeRemaining);
                        } else {
                            // Désactive le vol seulement si le temps est à 0
                            timeRemaining = 0;
                            upsertTimeFly(accessPlayerDTO.uniqueId(), timeRemaining);
                            plugin.getFlyManager().manageFly(accessPlayerDTO.uniqueId(), false);
                            plugin.getFlyManager().upsertFlyStatus(Bukkit.getPlayer(accessPlayerDTO.uniqueId()), false);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erreur lors de la gestion du temps fly : " + e.getMessage());
            }
        }, 20L, 20L);
    }






    public void addFlytime(Player player, int time) throws SQLException {
            int flyTime = getTimeRemaining(player);
            int newTime = flyTime + time;
            upsertTimeFly(player.getUniqueId(), newTime);
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
        if (timeTask != null && !timeTask.isCancelled()) {

            return timeRemaining;
        } else {

            AccessPlayerDTO fly = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());
            return fly.FlyTimeRemaining();
        }
    }

}
