package com.wayvi.wfly.wflyv2.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface FlyManager {

    void manageFly(UUID player, boolean fly);

    void manageFlySpeed(Player player, double speed);



}
