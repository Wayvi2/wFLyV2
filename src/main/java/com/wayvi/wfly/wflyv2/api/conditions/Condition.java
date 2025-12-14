package com.wayvi.wfly.wflyv2.api.conditions;


import org.bukkit.entity.Player;

public interface Condition {
    boolean check(Player player);
}