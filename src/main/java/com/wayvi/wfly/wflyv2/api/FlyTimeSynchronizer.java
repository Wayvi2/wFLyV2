package com.wayvi.wfly.wflyv2.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface FlyTimeSynchronizer {

    void handlePlayerQuitSynchronizer(Player player);

    void handlePlayerJoinSynchronizer(Player player);

    boolean getIsOffline();

    long getLastUpdate(UUID uuid);

    void setPlayerLastUpdate(Player player, long time);

}
