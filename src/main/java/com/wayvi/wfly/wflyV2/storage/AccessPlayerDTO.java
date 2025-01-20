package com.wayvi.wfly.wflyV2.storage;

import fr.maxlego08.sarah.Column;
import org.bukkit.entity.Player;

import java.util.UUID;

public record AccessPlayerDTO(
        @Column(value ="uniqueId", primary = true) UUID uniqueId,
        @Column(value ="isinFly") boolean isinFly) {

    public boolean isInFly() {
        return isinFly;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }
}
