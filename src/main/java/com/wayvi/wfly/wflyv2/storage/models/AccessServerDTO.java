package com.wayvi.wfly.wflyv2.storage.models;

import fr.maxlego08.sarah.Column;

public class AccessServerDTO {
    @Column(value = "shutdownMillisTime", primary = true)
    private long shutdownMillisTime;


    public AccessServerDTO(long shutdownMillisTime) {
        this.shutdownMillisTime = shutdownMillisTime;

    }

    /**
     * Gets the unique identifier (UUID) of the player.
     *
     * @return the player's unique ID
     */
    public long shutdownMillisTime() {
        return shutdownMillisTime;
    }

}

