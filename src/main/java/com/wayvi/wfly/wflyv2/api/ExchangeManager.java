package com.wayvi.wfly.wflyv2.api;

import org.bukkit.entity.Player;

public interface ExchangeManager {

    void exchangeTimeFly(Player donator, Player receiver, int time);

    boolean canGiveHisTempFly(Player donator);

    int getCooldown(Player player);

    void setCooldown(Player player, Integer cooldownToSet);

    void decrementCooldown(Player player);
}
