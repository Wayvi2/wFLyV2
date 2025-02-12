package com.wayvi.wfly.wflyV2.storage;

import fr.maxlego08.sarah.Column;

import java.util.UUID;

public class AccessPlayerDTO {

    @Column(value = "uniqueId", primary = true)
    private UUID uniqueId;

    @Column(value = "isinFly")
    private boolean isinFly;

    @Column(value = "FlyTimeRemaining")
    private int FlyTimeRemaining;

    public AccessPlayerDTO(UUID uniqueId, boolean isinFly, int FlyTimeRemaining) {
        this.uniqueId = uniqueId;
        this.isinFly = isinFly;
        this.FlyTimeRemaining = FlyTimeRemaining;
    }

    public UUID uniqueId() {
        return uniqueId;
    }

    public boolean isinFly() {
        return isinFly;
    }

    public int FlyTimeRemaining() {
        return FlyTimeRemaining;
    }
}
