package com.wayvi.wfly.wflyv2.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing players' flight time.
 * <p>
 * Provides methods to add, remove, reset, and retrieve flight time for players.
 * It also handles persistence, automatic time decrementing, flight status tracking,
 * and bulk operations on all players.
 */
public interface TimeFlyManager {

    /**
     * Adds flight time to the specified player.
     *
     * @param player the player who will receive additional flight time
     * @param time   the amount of time (in seconds) to add
     */
    void addFlytime(Player player, int time);

    /**
     * Removes flight time from the specified player.
     *
     * @param target the player whose flight time will be reduced
     * @param time   the amount of time (in seconds) to remove
     * @return true if the time was successfully removed, false if not enough time remained
     */
    boolean removeFlyTime(Player target, int time);

    /**
     * Resets the flight time of the specified player to zero.
     *
     * @param player the player whose flight time will be reset
     */
    void resetFlytime(Player player);

    /**
     * Gets the remaining flight time for the specified player.
     *
     * @param player the player whose remaining time is requested
     * @return the remaining flight time (in seconds)
     */
    int getTimeRemaining(Player player);

    /**
     * Decrements the remaining flight time for all tracked players.
     * <p>
     * This is typically called periodically (e.g., every second).
     *
     * @throws SQLException if a database error occurs during the update
     */
    void decrementTimeRemaining() throws SQLException;

    /**
     * Saves the current flight times of all players to persistent storage.
     *
     * @throws SQLException if a database error occurs during the save
     */
    void saveFlyTimes() throws SQLException;

    /**
     * Updates the flying status of a specific player.
     *
     * @param playerUUID the UUID of the player
     * @param isFlying   true if the player is flying, false otherwise
     */
    void updateFlyStatus(UUID playerUUID, boolean isFlying);

    /**
     * Retrieves whether the specified player is currently flying.
     *
     * @param playerUUID the UUID of the player
     * @return true if the player is flying, false otherwise
     */
    boolean getIsFlying(UUID playerUUID);

    /**
     * Saves all flight times asynchronously when the plugin is disabled.
     *
     * @return a {@link CompletableFuture} that completes once the save is finished
     */
    CompletableFuture<Void> saveFlyTimeOnDisable();

    /**
     * Loads the stored flight time for the specified player into memory.
     *
     * @param player the player whose flight time will be loaded
     */
    void loadFlyTimesForPlayer(Player player);

    /**
     * Adds flight time to all tracked players.
     *
     * @param time the amount of time (in seconds) to add
     */
    void addFlytimeForAllPlayers(int time);

    /**
     * Removes flight time from all tracked players.
     *
     * @param time the amount of time (in seconds) to remove
     */
    void removeFlytimeForAllPlayers(int time);

}
