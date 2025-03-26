package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlyManager {

    static int threads = Runtime.getRuntime().availableProcessors();
    public static ExecutorService service = Executors.newFixedThreadPool(threads);

    private final WFlyV2 plugin;
    private final RequestHelper requestHelper;
    private BukkitTask flyTask;
    private final ConfigUtil configUtil;


    public FlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    public void manageFly(UUID player, boolean fly) throws SQLException {
        Player player1 = Bukkit.getPlayer(player);

        if (flyTask != null) {
            flyTask.cancel();
            flyTask = null;
        }

        if (player1 == null) {
            return;
        }


        if (fly) {
            player1.setAllowFlight(true);
            player1.setFlying(true);
            upsertFlyStatus(player1, true);
            plugin.getTimeFlyManager().updateFlyStatus(player1.getUniqueId(), true);
        } else {
            player1.setFlying(false);
            player1.setAllowFlight(false);
            plugin.getTimeFlyManager().updateFlyStatus(player1.getUniqueId(), false);

            upsertFlyStatus(player1, false);
        }
    }

    public void manageFlySpeed(Player player, double speed) {
        speed = speed / 10.0;

        if (speed > 1.0) {
            ColorSupportUtil.sendColorFormat(player,configUtil.getCustomMessage().getString("message.fly-speed-too-high").replace("%speed%", String.valueOf(speed*10)));
            return;
        }

        for (int i = (int) (speed * 10); i >= 1; i--) {
            if (player.hasPermission("wfly.fly.speed." + i)) {
                player.setFlySpeed((float) i / 10.0f);
                ColorSupportUtil.sendColorFormat(player,configUtil.getCustomMessage().getString("message.fly-speed").replace("%speed%", String.valueOf(i)));
                return;
            }
        }

        ColorSupportUtil.sendColorFormat(player,configUtil.getCustomMessage().getString("message.fly-speed-no-permission").replace("%speed%", String.valueOf(speed*10)));
    }

    // ACCESS DATABASE METHODS
    public AccessPlayerDTO getPlayerFlyData(UUID player) throws SQLException {
        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", player));

        if (fly.isEmpty()) {
            return new AccessPlayerDTO(player, false, 0);
        } else {
            return fly.get(0);
        }
    }

    public void upsertFlyStatus(Player player, boolean isFlying) {
        service.execute(() -> {
            List<AccessPlayerDTO> existingRecords = this.requestHelper.select("fly", AccessPlayerDTO.class,
                    table -> table.where("uniqueId", player.getUniqueId()));

            if (existingRecords.isEmpty()) {
                this.requestHelper.insert("fly", table -> {
                    table.uuid("uniqueId", player.getUniqueId()).primary();
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", plugin.getTimeFlyManager().getTimeRemaining(player));
                });
            } else {
                this.requestHelper.update("fly", table -> {
                    table.where("uniqueId", player.getUniqueId());
                    table.bool("isinFly", isFlying);
                    table.bigInt("FlyTimeRemaining", plugin.getTimeFlyManager().getTimeRemaining(player));
                });
            }
        });
    }



    public void createNewPlayer(UUID player) throws SQLException {
        this.requestHelper.insert("fly", table -> {
            table.uuid("uniqueId", player).primary();
            table.bool("isinFly", false);
            table.bigInt("FlyTimeRemaining", 0);
        });
    }
}
