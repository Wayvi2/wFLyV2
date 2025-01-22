package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
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

    public TimeFlyManager(WFlyV2 plugin, RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
    }

    public void decrementTimeRemaining(Player player, boolean bool) throws SQLException {

        timeRemaining = plugin.getFlyManager().getPlayerFlyData(player).FlyTimeRemaining();
        if (bool){
            timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (timeRemaining == 0) {

                    return;
                }
                timeRemaining = timeRemaining - 1;
                player.sendMessage(String.valueOf(timeRemaining));
            }, 20L, 20L);

        } else {
            timeTask.cancel();
        }
        upsertTimeFly(player, timeRemaining);
    }


    public void upsertTimeFly(Player player, int timeRemaining) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", player.getUniqueId()).primary();
                table.bigInt("FlyTimeRemaining", timeRemaining);
            });
        });
    }

    public int getTimeRemaining(Player player) {
        return timeRemaining;
    }

}
