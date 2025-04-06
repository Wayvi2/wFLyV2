package com.wayvi.wfly.wflyV2.api;

import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.sql.SQLException;
import java.util.UUID;

public interface FlyManager {

    void manageFly(UUID player, boolean fly) throws SQLException;

    void manageFlySpeed(Player player, double speed);

    AccessPlayerDTO getPlayerFlyData(UUID player) throws SQLException;

    void upsertFlyStatus(Player player, boolean isFlying);

    void createNewPlayer(UUID player) throws SQLException;

}
