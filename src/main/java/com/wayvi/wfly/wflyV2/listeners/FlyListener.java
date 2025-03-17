package com.wayvi.wfly.wflyV2.listeners;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.managers.ConditionManager;
import com.wayvi.wfly.wflyV2.managers.fly.FlyManager;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import fr.maxlego08.sarah.RequestHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.List;

public class FlyListener implements Listener {

    private final WFlyV2 plugin;
    private final FlyManager flyManager;
    private final RequestHelper requestHelper;
    private ConditionManager conditionManager;
    private final ConfigUtil configUtil;

    public FlyListener(WFlyV2 plugin, FlyManager flyManager, RequestHelper requestHelper, ConditionManager conditionManager, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.flyManager = flyManager;
        this.requestHelper = requestHelper;
        this.conditionManager = conditionManager;
        this.configUtil = configUtil;
    }



    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) throws SQLException {
        Player player = event.getPlayer();
        int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(player);
        AccessPlayerDTO playerData = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());

        plugin.getTimeFlyManager().upsertTimeFly(playerData.uniqueId(), timeRemaining);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {


        Player player = event.getPlayer();

        AccessPlayerDTO playerFlyData = flyManager.getPlayerFlyData(player.getUniqueId());

        if (playerFlyData.isinFly()) {
            flyManager.manageFly(player.getUniqueId(), true);
        }

        List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {
            table.where("uniqueId", player.getUniqueId());
        });

        if (fly.isEmpty()) {
            try {
                plugin.getFlyManager().createNewPlayer(player.getUniqueId());

            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating new player: " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();


        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (conditionManager.cannotFly(player)) {
                try {
                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));
                    plugin.getFlyManager().manageFly(player.getUniqueId(), false);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (conditionManager.canFly(player)) {
                try {

                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-activated"));
                    plugin.getFlyManager().manageFly(player.getUniqueId(), true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 15L);
    }

}
