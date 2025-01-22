package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.services.DatabaseService;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FlyManager {

    public static ExecutorService service = Executors.newSingleThreadExecutor();

    private Plugin plugin;

    private final RequestHelper requestHelper;

    private BukkitTask flyTask;


    private ConfigUtil configUtil;

    private MiniMessageSupportUtil miniMessageSupportUtil;


    public FlyManager(Plugin plugin, RequestHelper requestHelper, ConfigUtil configUtil, MiniMessageSupportUtil miniMessageSupportUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.miniMessageSupportUtil = miniMessageSupportUtil;


    }

    public void manageFly(Player player, boolean fly) {

        upsertFlyStatus(player, fly);

        if (fly) {
            player.setAllowFlight(true);
            player.setFlying(true);
            if (flyTask != null && !flyTask.isCancelled()) {
                flyTask.cancel();
            }
        } else {
            player.setFlying(false);
            flyTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (player.isFlying()) {
                    player.setFlying(false);
                }
                if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                    player.setAllowFlight(false);
                    player.setFlySpeed(0.1F);
                }
            }, 20L, 20L);
        }
    }

    public void manageFlySpeed(Player player, double speed) {
        player.sendMessage(String.valueOf(speed));
        if (speed > 1.0) {player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(configUtil.getCustomMessage().getString("message.fly-speed-too-high")));
            return;
        }
        player.setFlySpeed((float) speed);
    }



    //ACCESS DATABASE METHODES
    public AccessPlayerDTO getIsInFlyBeforeDeconnect(Player player) throws SQLException {

        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> table.where("uniqueId", player.getUniqueId()));

        if (fly.isEmpty()) {
            return new AccessPlayerDTO(player.getUniqueId(), false);
        } else {
            return fly.getFirst();

        }
    }


    public void upsertFlyStatus(Player player, boolean isFlying) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", player.getUniqueId()).primary();
                table.bool("isinFly", isFlying);
            });
        });
    }




}
