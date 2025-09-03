package com.wayvi.wfly.wflyv2.api;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Interface for managing players' flight and flight speed.
 * <p>
 * Provides methods to enable or disable flight for a player,
 * as well as to control their flying speed.
 */
public interface FlyManager {

    /**
     * Enables or disables flight for the specified player.
     *
     * @param player the UUID of the player whose flight status will be changed
     * @param fly    true to enable flight, false to disable
     */
    void manageFly(UUID player, boolean fly);

    /**
     * Sets the flight speed of the specified player.
     *
     * @param player the player whose flight speed will be modified
     * @param speed  the new flying speed (typically between 0.0 and 1.0)
     */
    void manageFlySpeed(Player player, double speed);

}
