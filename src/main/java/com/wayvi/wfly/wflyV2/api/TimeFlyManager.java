package com.wayvi.wfly.wflyV2.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public interface TimeFlyManager {

    void addFlytime(Player player, int time) throws SQLException;

    boolean removeFlyTime(Player sender, Player target, int time);

    void resetFlytime(Player player);

    int getTimeRemaining(Player player);

    void decrementTimeRemaining() throws SQLException;

    void saveFlyTimes() throws SQLException;

    void SaveFlyTimeOnDisable();

    void upsertTimeFly(@NotNull UUID playerUUID, int newTimeRemaining);

    void updateFlyStatus(UUID playerUUID, boolean isFlying);
}
