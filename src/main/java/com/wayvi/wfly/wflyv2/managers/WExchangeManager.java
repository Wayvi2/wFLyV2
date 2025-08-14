package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.ExchangeManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WExchangeManager implements ExchangeManager {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final Map<Player, Integer> cooldown = new HashMap<>();

    public WExchangeManager(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        startDecrementCooldown();
    }

    @Override
    public void exchangeTimeFly(Player donator, Player receiver, int time) {
        if (time < 1) return;

        int cooldownToSet;
        if (configUtil.getCustomConfig().getBoolean("cooldown-give.enabled", false)) {
            if (configUtil.getCustomConfig().getBoolean("cooldown-give.custom-cooldown.enabled", false)) {
                cooldownToSet = configUtil.getCustomConfig().getInt("cooldown-give.custom-cooldown.cooldown", 5);
            } else {
                cooldownToSet = time;
            }
        } else {
            cooldownToSet = 0;
        }

        WflyApi.get().getTimeFlyManager().removeFlyTime(donator, time);
        if (cooldownToSet > 0) {
            setCooldown(donator, cooldownToSet);
        } else {
            setCooldown(donator, 0);
        }
        WflyApi.get().getTimeFlyManager().addFlytime(receiver, time);
    }


    @Override
    public boolean canGiveHisTempFly(Player donator) {
        return getCooldown(donator) <= 0;
    }

    @Override
    public int getCooldown(Player player) {
        return cooldown.getOrDefault(player, 0);
    }

    @Override
    public void setCooldown(Player player, Integer cooldownToSet) {
        if (cooldownToSet == null || cooldownToSet <= 0) {
            cooldown.remove(player);
        } else {
            cooldown.put(player, cooldownToSet);
        }
    }

    @Override
    public void decrementCooldown(Player player) {
        int currentCooldown = getCooldown(player);
        if (currentCooldown <= 0) {
            cooldown.remove(player);
            return;
        }
        cooldown.put(player, currentCooldown - 1);
    }

    private void startDecrementCooldown() {
        int interval = configUtil.getCustomConfig().getInt("fly-decrement-interval", 20);
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (cooldown.containsKey(player)) {
                    decrementCooldown(player);
                }
            }
        }, 0L, interval);
    }
}
