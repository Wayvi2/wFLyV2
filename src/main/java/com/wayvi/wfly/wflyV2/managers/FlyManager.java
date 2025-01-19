package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.services.DatabaseService;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import fr.maxlego08.sarah.RequestHelper;
import fr.maxlego08.sarah.logger.JULogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlyManager {

    public static ExecutorService service = Executors.newFixedThreadPool(5);

    Plugin plugin;

    private final RequestHelper requestHelper;

    private DatabaseService databaseService;


    public FlyManager(Plugin plugin, DatabaseService databaseService, RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        this.databaseService = databaseService;
        this.plugin = plugin;


    }

    public void manageFly(Player player, boolean fly) {
        if (fly) {
            updateFlyStatusInDB(player, 1);
        } else {
            updateFlyStatusInDB(player, 0);
        }
        player.setAllowFlight(fly);
        player.setFlying(fly);
    }

    public boolean isFlying(Player player) {
        return player.isFlying();
    }

    public List<AccessPlayerDTO> getIsInFlyBeforeDeconnect(Player player) throws SQLException {
        return this.requestHelper.select("isinFly", AccessPlayerDTO.class, table -> table.where("uniqueId", player.getUniqueId()));
    }

    public void updateFlyStatusInDB(Player player, int fly) {
        service.execute(() -> {
            this.requestHelper.upsert("fly", table -> {
                table.uuid("player_id", player.getUniqueId());
                table.integer("is_flying");
            });
        });
    }




}
