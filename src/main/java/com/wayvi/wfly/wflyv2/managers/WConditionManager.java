package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.api.ConditionManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.models.Condition;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class WConditionManager implements ConditionManager {

    private List<Condition> authorizedConditions;
    private List<Condition> notAuthorizedConditions;
    private final ConfigUtil configUtil;
    private final Map<UUID, Location> lastSafeLocation = new HashMap<>();
    private final Set<String> unregisteredPlaceholders = new HashSet<>();
    private final Map<UUID, Boolean> flyPermissionCache = new HashMap<>();

    public WConditionManager(ConfigUtil configUtil) {
        this.configUtil = configUtil;
        loadConditions();
    }

    @Override
    public void loadConditions() {
        resetUnregisteredPlaceholders();
        authorizedConditions = loadConditionsFromConfig("conditions.authorized");
        notAuthorizedConditions = loadConditionsFromConfig("conditions.not-authorized");
    }

    @Override
    public List<Condition> loadConditionsFromConfig(String path) {
        List<Condition> result = new ArrayList<>();
        ConfigurationSection section = configUtil.getCustomConfig().getConfigurationSection(path);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String placeholder = section.getString(key + ".placeholder");
                String equalsValue = section.getString(key + ".equals");
                List<String> commands = section.getStringList(key + ".commands");
                if (placeholder != null && equalsValue != null) {
                    result.add(new Condition(placeholder, equalsValue, commands));
                }
            }
        }
        return result;
    }

    @Override
    public boolean isFlyAuthorized(Player player) {
        if (hasBypassPermission(player)) {
            return true;
        }
        for (Condition c : authorizedConditions) {
            if (checkPlaceholder(player, c)) {
                return true;
            }
        }
        for (Condition c : notAuthorizedConditions) {
            if (checkPlaceholder(player, c)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPlaceholder(Player player, Condition c) {
        String placeholderValue = PlaceholderAPI.setPlaceholders(player, c.getPlaceholder());
        if (placeholderValue.equals(c.getPlaceholder())) {
            logPlaceholderError(placeholderValue);
        }
        String equalsValue = PlaceholderAPI.setPlaceholders(player, c.getEqualsValue());
        return placeholderValue.equalsIgnoreCase(equalsValue);
    }

    @Override
    public void logPlaceholderError(String placeholder) {
        if (unregisteredPlaceholders.add(placeholder)) {
            WflyApi.get().getPlugin().getLogger().severe("Placeholder not registered or not working: " + placeholder);
        }
    }


    @Override
    public void checkCanFly() {
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handlePlayerFlyState(player);
            }
        }, 20L, 20L);
    }

    private void handlePlayerFlyState(Player player) {
        boolean isAuthorized = WflyApi.get().getConditionManager().isFlyAuthorized(player);
        boolean isCurrentlyFlying = player.isFlying();

        if (player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()) {
            return;
        }

        if (!isAuthorized && isCurrentlyFlying) {
            deactivateFlyForPlayer(player);
        }
    }

    private void deactivateFlyForPlayer(Player player) {
        WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);

        Location safeLocation = getSafeLocation(player);
        if (!safeLocation.equals(lastSafeLocation.get(player.getUniqueId()))) {
            handleFlyDeactivation(player, safeLocation);
        }

        executeNotAuthorizedCommands(player);
    }

    private void handleFlyDeactivation(Player player, Location safeLocation) {
        ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));
        if (player.getWorld().getEnvironment() != World.Environment.NETHER) {
            player.teleport(safeLocation);
            lastSafeLocation.put(player.getUniqueId(), safeLocation);
        }
    }

    private void executeNotAuthorizedCommands(Player player) {
        for (Condition c : notAuthorizedConditions) {
            if (checkPlaceholder(player, c)) {
                for (String command : c.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            }
        }
    }


    @Override
    public Location getSafeLocation(Player player) {
        boolean tpOnFloorWhenFlyDisabled = configUtil.getCustomConfig().getBoolean("tp-on-floor-when-fly-disabled");
        if (!tpOnFloorWhenFlyDisabled) {
            return player.getLocation();
        }

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public void resetUnregisteredPlaceholders() {
        unregisteredPlaceholders.clear();
    }

    public boolean hasBypassPermission(Player player) {
        return flyPermissionCache.computeIfAbsent(player.getUniqueId(), uuid ->
                player.isOp() || player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.getGameMode() == GameMode.SPECTATOR);
    }
}
