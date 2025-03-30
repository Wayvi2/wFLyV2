package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.models.Condition;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class ConditionManager {

    private List<Condition> authorizedConditions;
    private List<Condition> notAuthorizedConditions;
    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final Map<UUID, Boolean> flyStateCache = new HashMap<>();
    private final Map<UUID, Location> lastSafeLocation = new HashMap<>();
    private final Set<String> unregisteredPlaceholders = new HashSet<>();
    private final Map<UUID, Boolean> flyPermissionCache = new HashMap<>();

    public ConditionManager(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        loadConditions();
    }

    public void loadConditions() {
        resetUnregisteredPlaceholders();
        authorizedConditions = loadConditionsFromConfig("conditions.authorized");
        notAuthorizedConditions = loadConditionsFromConfig("conditions.not-authorized");
    }

    private List<Condition> loadConditionsFromConfig(String path) {
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

    private boolean checkPlaceholder(Player player, Condition c) {
        String placeholderValue = PlaceholderAPI.setPlaceholders(player, c.getPlaceholder());
        if (placeholderValue.equals(c.getPlaceholder())) {
            logPlaceholderError(placeholderValue);
        }
        String equalsValue = PlaceholderAPI.setPlaceholders(player, c.getEqualsValue());
        return placeholderValue.equalsIgnoreCase(equalsValue);
    }

    private void logPlaceholderError(String placeholder) {
        if (unregisteredPlaceholders.add(placeholder)) {
            plugin.getLogger().severe("Placeholder not registered or not working: " + placeholder);
        }
    }

    public void checkCanFly() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {



            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean isAuthorized = isFlyAuthorized(player);
                boolean isCurrentlyFlying = player.isFlying();
                if (!isAuthorized && isCurrentlyFlying) {
                    try {
                        plugin.getFlyManager().manageFly(player.getUniqueId(), false);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    Location safeLocation = getSafeLocation(player);
                    if (!safeLocation.equals(lastSafeLocation.get(player.getUniqueId()))) {
                        ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));
                        if (player.getWorld().getEnvironment() != World.Environment.NETHER) {
                            player.teleport(safeLocation);
                            lastSafeLocation.put(player.getUniqueId(), safeLocation);
                        }
                    }
                    for (Condition c : notAuthorizedConditions) {
                        if (checkPlaceholder(player, c)) {
                            for (String command : c.getCommands()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    private Location getSafeLocation(Player player) {
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

    public void resetUnregisteredPlaceholders() {
        unregisteredPlaceholders.clear();
    }

    public boolean hasBypassPermission(Player player) {
        return flyPermissionCache.computeIfAbsent(player.getUniqueId(), uuid ->
                player.isOp() || player.hasPermission(Permissions.BYPASS_FLY.getPermission()));
    }
}
