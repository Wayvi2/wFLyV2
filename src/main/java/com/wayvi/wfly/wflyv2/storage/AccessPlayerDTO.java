package com.wayvi.wfly.wflyv2.storage;

import fr.maxlego08.sarah.Column;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for storing player access information in the database.
 * This class represents the player's state regarding flight status and the remaining fly time.
 */
public class AccessPlayerDTO {

    @Column(value = "uniqueId", primary = true)
    private UUID uniqueId;

    @Column(value = "isinFly")
    private boolean isinFly;

    @Column(value = "FlyTimeRemaining")
    private int FlyTimeRemaining;

    /**
     * Constructs an AccessPlayerDTO with the specified player data.
     *
     * @param uniqueId the unique ID of the player
     * @param isinFly the current flying state of the player (true if flying, false otherwise)
     * @param FlyTimeRemaining the remaining time the player can stay in fly mode
     */
    public AccessPlayerDTO(UUID uniqueId, boolean isinFly, int FlyTimeRemaining) {
        this.uniqueId = uniqueId;
        this.isinFly = isinFly;
        this.FlyTimeRemaining = FlyTimeRemaining;
    }

    /**
     * Gets the unique identifier (UUID) of the player.
     *
     * @return the player's unique ID
     */
    public UUID uniqueId() {
        return uniqueId;
    }

    /**
     * Gets the current flying state of the player.
     *
     * @return true if the player is flying, false otherwise
     */
    public boolean isinFly() {
        return isinFly;
    }

    /**
     * Gets the remaining time the player can remain in fly mode.
     *
     * @return the remaining fly time in seconds
     */
    public int FlyTimeRemaining() {
        return FlyTimeRemaining;
    }
}
