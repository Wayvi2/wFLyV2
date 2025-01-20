package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.services.DatabaseService;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
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

    Plugin plugin;

    private final RequestHelper requestHelper;

    private BukkitTask flyTask;

    private DatabaseService databaseService;


    public FlyManager(Plugin plugin, DatabaseService databaseService, RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        this.databaseService = databaseService;
        this.plugin = plugin;


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
                if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                    player.setAllowFlight(false);
                }
            }, 20L, 20L);
        }
    }
















/*
Dan,s le cas fly = False
On enleve le fly  et 1 tick plus tard on fait le calculer de quand enlever la permission

/*
                    new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setAllowFlight(false);
                        player.sendMessage("toto");
                    }
                }.runTaskLater(plugin, (long) Math.sqrt(2 * player.getFallDistance() / 0.08));
            } else {
                player.setAllowFlight(false);
        }
        player.setFlying(fly);
 */






        /*
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(String.valueOf(player.getAllowFlight()));
            }
        }.runTaskTimer(plugin, 0, 20);
  */


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
