package com.wayvi.wfly.wflyv2.api;

import org.bukkit.entity.Player;

/**
 * Interface for managing the exchange of flight time between players.
 * <p>
 * Provides methods to transfer flight time from one player to another,
 * as well as to handle cooldowns that prevent repeated exchanges within
 * a short period of time.
 */
public interface ExchangeManager {

    /**
     * Transfers a given amount of flight time from one player to another.
     *
     * @param donator the player who gives away flight time
     * @param receiver the player who receives the flight time
     * @param time the amount of time (in seconds) to transfer
     */
    void exchangeTimeFly(Player donator, Player receiver, int time);

    /**
     * Gets the current cooldown (in seconds) for the specified player.
     * <p>
     * The cooldown represents the time the player must wait before performing
     * another exchange.
     *
     * @param player the player whose cooldown is being requested
     * @return the remaining cooldown time (in seconds), or 0 if no cooldown is active
     */
    int getCooldown(Player player);

    /**
     * Sets the cooldown time for the specified player.
     *
     * @param player the player whose cooldown will be set
     * @param cooldownToSet the cooldown value to apply (in seconds)
     */
    void setCooldown(Player player, Integer cooldownToSet);

    /**
     * Decreases the cooldown timer for the specified player by one unit (typically one second).
     * <p>
     * This is usually called periodically (e.g., every second in a scheduled task).
     *
     * @param player the player whose cooldown will be decreased
     */
    void decrementCooldown(Player player);
}
