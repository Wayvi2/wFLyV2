package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
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

    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper, MiniMessageSupportUtil miniMessageSupportUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
    }


    // TODO : Fix problem with the timefly in DB --> DB update 1/2 on disabling fly
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
                player.sendMessage(String.valueOf(timeRemaining));

            }, 20L, 20L);
        } else {
            upsertTimeFly(player, timeRemaining);
        }
    }


    public void addFlytime(Player player, int time) throws SQLException {

        if(timeTask != null && !timeTask.isCancelled()){
            int flyTime = timeRemaining;
            upsertTimeFly(player, flyTime + time);

        } else {
            int flyTime = getTimeRemaining(player);
            upsertTimeFly(player, flyTime + time);
        }

        if (timeTask != null && !timeTask.isCancelled()) {
            timeTask.cancel();
        }
        decrementTimeRemaining(player, plugin.getFlyManager().getFlyStatus(player));
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
        AccessPlayerDTO fly = plugin.getFlyManager().getPlayerFlyData(player);
        return fly.FlyTimeRemaining();
    }

}
