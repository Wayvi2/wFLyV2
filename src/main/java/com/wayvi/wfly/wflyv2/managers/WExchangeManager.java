package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.ExchangeManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WExchangeManager implements ExchangeManager {

    private final WFlyV2 plugin;
    private final Map<Player, Integer> cooldown = new HashMap<>();

    public WExchangeManager(WFlyV2 plugin) {
        this.plugin = plugin;
        startDecrementCooldown();
    }

    @Override
    public void exchangeTimeFly(Player donator, Player receiver, int time) {
        if (time < 1) return;

        boolean activate = plugin.getConfigFile().get(ConfigEnum.COOLDOWN_GIVE_ENABLED);
        int cooldownToSet;
        if (activate) {
            boolean activateCustomCooldown = plugin.getConfigFile().get(ConfigEnum.COOLDOWN_GIVE_CUSTOM_ENABLED);
            if (activateCustomCooldown) {
                cooldownToSet = plugin.getConfigFile().get(ConfigEnum.COOLDOWN_GIVE_CUSTOM_TIME);
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
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (cooldown.containsKey(player)) {
                    decrementCooldown(player);
                }
            }
        }, 0L, 20);
    }
}
