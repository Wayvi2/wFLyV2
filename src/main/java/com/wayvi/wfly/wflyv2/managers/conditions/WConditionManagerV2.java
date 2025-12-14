package com.wayvi.wfly.wflyv2.managers.conditions;

import com.wayvi.wfly.wflyv2.ActionBar;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.ConditionManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.api.conditions.Condition;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.conditions.ConditionType;
import com.wayvi.wfly.wflyv2.constants.conditions.FlyResult;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;

import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;


import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class WConditionManagerV2 implements ConditionManager {

    private final WFlyV2 plugin;
    private final List<FlyRule> activeRules = new ArrayList<>();


    private final Map<UUID, Location> lastSafeLocation = new HashMap<>();
    private final Map<UUID, Boolean> wasFlyingBefore = new HashMap<>();
    private final Set<String> unregisteredPlaceholders = new HashSet<>();



    private final List<DecrementRule> decrementRules = new ArrayList<>();

    private final Map<UUID, Boolean> bypassCache = new HashMap<>();

    public WConditionManagerV2(WFlyV2 plugin) {
        this.plugin = plugin;
        loadConditions();
    }

    @Override
    public void loadConditions() {

        activeRules.clear();
        resetUnregisteredPlaceholders();
        bypassCache.clear();

        ConfigurationSection flySection = plugin.getConfigFile().getRaw().getConfigurationSection("fly-rules");

        if (flySection != null) {
            for (String ruleId : flySection.getKeys(false)) {
                ConfigurationSection ruleSection = flySection.getConfigurationSection(ruleId);
                if (ruleSection == null) continue;

                int priority = ruleSection.getInt("priority", 999);
                String resStr = ruleSection.getString("result", "DENY");
                FlyResult result = FlyResult.ALLOW;
                try {
                    result = FlyResult.valueOf(resStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid result for rule " + ruleId + " -> defaulting to DENY");
                    result = FlyResult.DENY;
                }

                List<String> actions = ruleSection.getStringList("actions");

                List<Condition> conditions = new ArrayList<>();
                ConfigurationSection condsSec = ruleSection.getConfigurationSection("conditions");
                if (condsSec != null) {
                    for (String condKey : condsSec.getKeys(false)) {
                        Condition cond = ConditionType.parse(condsSec.getConfigurationSection(condKey));
                        conditions.add(cond);
                    }
                }

                FlyRule rule = new FlyRule(ruleId, priority, conditions, result, actions);
                activeRules.add(rule);
            }
            Collections.sort(activeRules);
            plugin.getLogger().info("[WFly] " + activeRules.size() + " fly rules loaded.");
        }


        decrementRules.clear();
        ConfigurationSection decSec = plugin.getConfigFile().getRaw()
                .getConfigurationSection("decrementation-disable-by-condition");

        if (decSec != null) {
            for (String key : decSec.getKeys(false)) {
                ConfigurationSection ruleSec = decSec.getConfigurationSection(key);
                if (ruleSec == null) continue;

                int priority = ruleSec.getInt("priority", 999);

                boolean stopTimer = ruleSec.getBoolean("stop_timer", true);

                List<Condition> conditions = new ArrayList<>();
                ConfigurationSection condsSec = ruleSec.getConfigurationSection("conditions");
                if (condsSec != null) {
                    for (String cKey : condsSec.getKeys(false)) {
                        Condition cond = ConditionType.parse(condsSec.getConfigurationSection(cKey));
                        conditions.add(cond);
                    }
                }

                DecrementRule rule = new DecrementRule(key, priority, conditions, stopTimer);
                decrementRules.add(rule);
            }
            // 4. On TRIE par priorité
            Collections.sort(decrementRules);
            plugin.getLogger().info("[WFly] " + decrementRules.size() + " règles de décrémentation chargées.");
        }
    }

    @Override
    public void reloadConditions() {
        loadConditions();
    }

    // ══════════════════════════════════════════════════════
    //                 CORE LOGIC (UPDATED)
    // ══════════════════════════════════════════════════════

    @Override
    public void checkCanFly() {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handlePlayerFlyState(player);
            }
        }, 20L, 20L);
    }


    private void handlePlayerFlyState(Player player) {
        if (hasBypassPermission(player)) return;

        FlyRule matchingRule = null;
        for (FlyRule rule : activeRules) {
            if (rule.canApply(player)) {
                matchingRule = rule;
                break;
            }
        }

        boolean isAuthorized = (matchingRule != null && matchingRule.getResult() == FlyResult.ALLOW);


        boolean isCurrentlyFlying = player.isFlying();
        boolean wasFlying = getWasFlyingBefore(player);
        int time = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);

        if (!isAuthorized) {
            if (time > 0 || player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {

                if (isCurrentlyFlying || player.getAllowFlight()) {
                    deactivateFlyForPlayer(player);
                    setFlyingBefore(player, true);


                    if (matchingRule != null) {
                        executeRuleActions(player, matchingRule.getActions());
                    }
                }
            }
            return;
        }

        boolean autoReactivate = plugin.getConfigFile().get(ConfigEnum.AUTO_REACTIVATE_FLY_AFTER_CONDITIONS_DISABLE);

        if (isAuthorized && autoReactivate && wasFlying) {
            if (!player.getAllowFlight()) {
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), true);
                setFlyingBefore(player, false);

                if (matchingRule != null) {
                    executeRuleActions(player, matchingRule.getActions());
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //                 ACTIONS & UTILS
    // ══════════════════════════════════════════════════════

    private void executeRuleActions(Player player, List<String> actions) {
        if (actions == null || actions.isEmpty()) return;

        for (String actionRaw : actions) {
            String action = PlaceholderAPI.setPlaceholders(player, actionRaw);

            try {
                if (action.startsWith("[message] ")) {
                    ColorSupportUtil.sendColorFormat(player, action.substring(10));
                }


                else if (action.startsWith("[command] ")) {
                    String cmd = action.substring(10).replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }


                else if (action.startsWith("[player] ")) {
                    String cmd = action.substring(9);
                    player.performCommand(cmd);
                }


                else if (action.startsWith("[sound] ")) {
                    String[] parts = action.substring(8).split(" ");
                    if (parts.length >= 1) {
                        Sound sound = Sound.valueOf(parts[0].toUpperCase());
                        float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                        float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    }
                }

                else if (action.startsWith("[actionbar] ")) {
                    String msg = action.substring(12);
                    ActionBar.sendActionBar(player, (String) ColorSupportUtil.convertColorFormat(msg));
                }


            } catch (Exception e) {
                Bukkit.getLogger().warning("[WFly] Error in action: " + actionRaw + " -> " + e.getMessage());
            }
        }
    }

    private void deactivateFlyForPlayer(Player player) {
        if (player.isFlying()) {
            Location safeLocation = getSafeLocation(player);

            if (safeLocation == null) {
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
            } else {
                if (!safeLocation.equals(lastSafeLocation.get(player.getUniqueId()))) {
                    player.teleport(safeLocation);
                    lastSafeLocation.put(player.getUniqueId(), safeLocation);
                }
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
            }

        } else {
            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
        }
    }

    @Override
    public boolean isFlyAuthorized(Player player) {
        if (hasBypassPermission(player)) return true;

        for (FlyRule rule : activeRules) {
            if (rule.canApply(player)) {
                return rule.getResult() == FlyResult.ALLOW;
            }
        }
        return false;
    }

    @Override
    public boolean hasBypassPermission(Player player) {
        return player.isOp()
                || player.hasPermission(Permissions.BYPASS_FLY.getPermission())
                || player.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean getDecrementationDisable(Player player) {
        if (decrementRules.isEmpty()) return false;
        for (DecrementRule rule : decrementRules) {
            if (rule.canApply(player)) {
                return rule.shouldStopTimer();
            }
        }

        return false;
    }


    @Override
    public Location getSafeLocation(Player player) {
        boolean tpOnFloor = plugin.getConfigFile().get(ConfigEnum.TP_ON_FLOOR_WHEN_FLY_DISABLED);
        if (!tpOnFloor) return null;

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (y > world.getMinHeight() && world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        if (y <= world.getMinHeight()) return null;

        return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
    }


    @Override
    public List<com.wayvi.wfly.wflyv2.models.Condition> loadConditionsFromConfig(String path) {
        return Collections.emptyList(); // Obsolète
    }

    @Override
    public boolean checkPlaceholder(Player player, com.wayvi.wfly.wflyv2.models.Condition c) {
        return false; //
    }

    @Override
    public void executeNotAuthorizedCommands(Player player) {
        //
    }

    @Override
    public void logPlaceholderError(String placeholder) {
        if (unregisteredPlaceholders.add(placeholder)) {
            plugin.getLogger().warning("Placeholder inconnu : " + placeholder);
        }
    }

    @Override
    public void resetUnregisteredPlaceholders() {
        unregisteredPlaceholders.clear();
    }

    @Override
    public boolean getWasFlyingBefore(Player player) {
        return wasFlyingBefore.getOrDefault(player.getUniqueId(), false);
    }

    @Override
    public void setFlyingBefore(Player player, boolean bool) {
        wasFlyingBefore.put(player.getUniqueId(), bool);
    }
}