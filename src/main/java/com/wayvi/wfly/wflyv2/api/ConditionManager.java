package com.wayvi.wfly.wflyv2.api;

import com.wayvi.wfly.wflyv2.models.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface ConditionManager {

    boolean isFlyAuthorized(Player player);

    void loadConditions();

    List<Condition> loadConditionsFromConfig(String path);

    boolean checkPlaceholder(Player player, Condition c);

    void logPlaceholderError(String placeholder);

    void checkCanFly();

    Location getSafeLocation(Player player);

    void resetUnregisteredPlaceholders();

    boolean hasBypassPermission(Player player);

    void executeNotAuthorizedCommands(Player player);

}
