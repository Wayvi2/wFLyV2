package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
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

    public void decrementTimeRemaining(Player player, boolean bool) throws SQLException {

        timeRemaining = getTimeRemaining(player);

        if (bool){
            timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (timeRemaining == 0) {
                    plugin.getFlyManager().manageFly(player, false);
                    timeTask.cancel();
                    upsertTimeFly(player, 0);
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


    public void upsertTimeFly(Player player, int newtimeRemaining) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", player.getUniqueId()).primary();
                try {
                    table.bool("isinFly", plugin.getFlyManager().getPlayerFlyData(player).isinFly());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                table.bigInt("FlyTimeRemaining", newtimeRemaining);

            });
        });
    }

    public int getTimeRemaining(Player player) throws SQLException {
        AccessPlayerDTO fly = plugin.getFlyManager().getPlayerFlyData(player);
        timeRemaining = fly.FlyTimeRemaining();
        return timeRemaining;
    }

}
