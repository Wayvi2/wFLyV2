package com.wayvi.wfly.wflyv2.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TimeFlyManager {

    void addFlytime(Player player, int time) throws SQLException;

    boolean removeFlyTime(CommandSender sender, Player target, int time);

    void resetFlytime(Player player);

    void saveInDbFlyTime(Player player);

    int getTimeRemaining(Player player);

    void decrementTimeRemaining() throws SQLException;

    void saveFlyTimes() throws SQLException;

    void updateFlyStatus(UUID playerUUID, boolean isFlying);

    boolean getIsFlying(UUID playerUUID);

    CompletableFuture<Void> saveFlyTimeOnDisable();

    CompletableFuture<Void> saveInDbFlyTimeDisable(Player player);

    void saveFlyTimeOnDisableOnline();

    void loadFlyTimesForPlayer(Player player);

    void resetFlytimeForAllPlayers();

    void addFlytimeForAllPlayers(int time);

    void removeFlytimeForAllPlayers(int time);

}
