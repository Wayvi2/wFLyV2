package com.wayvi.wfly.wflyV2.managers.fly;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlyManager {

    public static ExecutorService service = Executors.newSingleThreadExecutor();

    private WFlyV2 plugin;
    private final RequestHelper requestHelper;
    private BukkitTask flyTask;
    private ConfigUtil configUtil;
    private MiniMessageSupportUtil miniMessageSupportUtil;

    public FlyManager(WFlyV2 plugin, RequestHelper requestHelper, ConfigUtil configUtil, MiniMessageSupportUtil miniMessageSupportUtil) {
        this.requestHelper = requestHelper;
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
    }

    public void manageFly(Player player, boolean fly) throws SQLException {
        String messageFly = fly ? configUtil.getCustomMessage().getString("message.fly-activated") : configUtil.getCustomMessage().getString("message.fly-deactivated");


        if (fly) {
            player.setAllowFlight(true);
            player.setFlying(true);

            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageFly));

            if (flyTask != null && !flyTask.isCancelled()) {
                flyTask.cancel();
            }
        } else {

            player.setFlying(false);
            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageFly));

            flyTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (player.isFlying()) {
                    player.setFlying(false);
                }
                if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                    player.setAllowFlight(false);
                    player.setFlySpeed(0.1F);
                    flyTask.cancel();
                }
            }, 20L, 20L);

            //if(plugin.getTimeFlyManager().getTimeRemaining(player) == 0) {
            //player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(configUtil.getCustomMessage().getString("message.no-timefly-remaining")));
            //return;
            //}

        }

        upsertFlyStatus(player, fly);

    }

    public void manageFlySpeed(Player player, double speed) {
        speed = speed / 10.0;

        if (speed > 1.0) {
            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(
                    configUtil.getCustomMessage().getString("message.fly-speed-too-high")));
            return;
        }

        for (int i = (int) (speed * 10); i >= 1; i--) {
            if (player.hasPermission("wfly.fly.speed." + i)) {
                player.setFlySpeed((float) i / 10.0f);
                player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(
                        configUtil.getCustomMessage().getString("message.fly-speed").replace("%speed%", String.valueOf(i))));
                return;
            }
        }

        player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(
                configUtil.getCustomMessage().getString("message.fly-speed-no-permission")));
    }

    // ACCESS DATABASE METHODS
    public AccessPlayerDTO getPlayerFlyData(Player player) throws SQLException {
        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class,
                table -> table.where("uniqueId", player.getUniqueId()));

        if (fly.isEmpty()) {
            return new AccessPlayerDTO(player.getUniqueId(), false, 0);
        } else {
            return fly.getFirst();
        }
    }

    public void upsertFlyStatus(Player player, boolean isFlying) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("uniqueId", player.getUniqueId()).primary();
                table.bool("isinFly", isFlying);
                try {
                    table.bigInt("FlyTimeRemaining", plugin.getTimeFlyManager().getTimeRemaining(player));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public boolean getFlyStatus(Player player) throws SQLException {
        AccessPlayerDTO fly = plugin.getFlyManager().getPlayerFlyData(player);
        return !fly.isinFly();
    }
}
