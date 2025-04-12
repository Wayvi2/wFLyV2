package com.wayvi.wfly.wflyv2.api;

import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public interface FlyManager {

    void manageFly(UUID player, boolean fly);

    void manageFlySpeed(Player player, double speed);

    AccessPlayerDTO getPlayerFlyData(UUID player) throws SQLException;

    void upsertFlyStatus(Player player, boolean isFlying);

    void createNewPlayer(UUID player) throws SQLException;


}
