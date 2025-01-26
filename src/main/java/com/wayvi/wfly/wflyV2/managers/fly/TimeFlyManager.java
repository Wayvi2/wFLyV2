package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
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


    public void addFlytime(Player player, int time) throws SQLException {

        if (timeTask != null && !timeTask.isCancelled()) {
            // Ajouter le temps au vol actif
            timeRemaining += time;
            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(configUtil.getCustomMessage().getString("message.fly-time-added").replace("%time%", String.valueOf(time))
            ));
        } else {
            // Si le vol est désactivé, récupérer le temps restant depuis la base de données et l'ajouter
            int flyTime = getTimeRemaining(player);
            int newTime = flyTime + time;
            upsertTimeFly(player, newTime);
            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(configUtil.getCustomMessage().getString("message.fly-time-added").replace("%time%", String.valueOf(time))
            ));
        }
    }


    public void upsertTimeFly(Player player, int newtimeRemaining) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", player.getUniqueId()).primary();
                try {
                    AccessPlayerDTO playerFlyData = plugin.getFlyManager().getPlayerFlyData(player);
                    table.bool("isinFly", !playerFlyData.isinFly());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                table.bigInt("FlyTimeRemaining", newtimeRemaining);

            });
        });
    }

    public int getTimeRemaining(Player player) throws SQLException {
        if (timeTask != null && !timeTask.isCancelled()) {
            // Si une tâche est en cours, retourner la valeur locale
            return timeRemaining;
        } else {
            // Sinon, récupérer le temps restant à partir de la base de données
            AccessPlayerDTO fly = plugin.getFlyManager().getPlayerFlyData(player);
            return fly.FlyTimeRemaining();
        }
    }

}
