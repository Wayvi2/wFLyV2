package com.wayvi.wfly.wflyV2.storage;

import fr.maxlego08.sarah.Column;
import java.util.UUID;

public record AccessPlayerDTO(
        @Column(value ="uniqueId", primary = true) UUID uniqueId,
        @Column(value ="isinFly") int isinFly) {

    public int getIsInFly() {
        return this.isinFly;
    }
}
