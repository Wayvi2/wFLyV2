package com.wayvi.wfly.wflyv2.storage.models;

import org.bukkit.entity.Player;

;

public class PlayerSnapshot {
    public final Player player;
    public final boolean isExempt;
    public final boolean isStatic;
    public final boolean isConditionDisabled;
    public final boolean isActuallyFlying;

    public PlayerSnapshot(Player player, boolean isExempt, boolean isStatic, boolean isConditionDisabled, boolean isActuallyFlying) {
        this.player = player;
        this.isExempt = isExempt;
        this.isStatic = isStatic;
        this.isConditionDisabled = isConditionDisabled;
        this.isActuallyFlying = isActuallyFlying;
    }
}
