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

/**
 * Manages the conditions under which players are authorized or not to fly.
 * This class handles the loading and checking of conditions for fly authorization,
 * and manages the actions related to fly deactivation and teleportation to safe locations.
 */
public class ConditionManager {

    private List<Condition> authorizedConditions;
    private List<Condition> notAuthorizedConditions;

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final Map<UUID, Boolean> flyStateCache = new HashMap<>();
    private Map<UUID, Location> lastSafeLocation = new HashMap<>();
    private final Set<String> unregisteredPlaceholders = new HashSet<>();


    /**
     * Initializes the ConditionManager with the provided plugin, config utility, and request helper.
     *
     * @param plugin        the plugin instance
     * @param configUtil    the configuration utility for loading custom configurations
     */
    public ConditionManager(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        loadConditions();
    }

    /**
     * Loads the conditions from the configuration for both authorized and not authorized conditions.
     */
    public void loadConditions() {
        resetUnregisteredPlaceholders();
        authorizedConditions = loadConditionsFromConfig("conditions.authorized");
        notAuthorizedConditions = loadConditionsFromConfig("conditions.not-authorized");
    }

    /**
     * Loads the conditions from the configuration file based on the specified path.
     *
     * @param path the path in the configuration file to load conditions from
     * @return a list of conditions
     */
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

    /**
     * Checks if a player is authorized to fly based on their conditions and permissions.
     *
     * @param player the player to check
     * @return true if the player is authorized to fly, false otherwise
     */
    public boolean isFlyAuthorized(Player player) {
        if (player.isOp() || player.hasPermission(Permissions.BYPASS_FLY.getPermission())) {
            return true;
        }

        for (Condition c : authorizedConditions) {
            String placeholderValue = PlaceholderAPI.setPlaceholders(player, c.getPlaceholder());
            String equalsValue = PlaceholderAPI.setPlaceholders(player, c.getEqualsValue());

            if (plugin.isStartup() && placeholderValue.equals(c.getPlaceholder())) {
                if (!unregisteredPlaceholders.contains(placeholderValue)) {
                    plugin.getLogger().severe("Placeholder not registered: " + placeholderValue);
                    unregisteredPlaceholders.add(placeholderValue);
                }
            }

            if (plugin.isStartup() && isPlaceholder(equalsValue)) {
                if (!PlaceholderAPI.isRegistered(equalsValue) && !unregisteredPlaceholders.contains(equalsValue)) {
                    plugin.getLogger().severe("Placeholder not registered: " + equalsValue);
                    unregisteredPlaceholders.add(equalsValue);
                }
            }

            if (placeholderValue.equalsIgnoreCase(equalsValue)) {
                return true;
            }
        }

        for (Condition c : notAuthorizedConditions) {
            String placeholderValue = PlaceholderAPI.setPlaceholders(player, c.getPlaceholder());
            String equalsValue = PlaceholderAPI.setPlaceholders(player, c.getEqualsValue());

            if (plugin.isStartup() && placeholderValue.equals(c.getPlaceholder())) {
                if (!unregisteredPlaceholders.contains(placeholderValue)) {
                    plugin.getLogger().severe("Placeholder not registered: " + placeholderValue);
                    unregisteredPlaceholders.add(placeholderValue);
                }
            }

            if (plugin.isStartup() && isPlaceholder(equalsValue)) {
                if (!PlaceholderAPI.isRegistered(equalsValue) && !unregisteredPlaceholders.contains(equalsValue)) {
                    plugin.getLogger().severe("Placeholder not registered: " + equalsValue);
                    unregisteredPlaceholders.add(equalsValue);
                }
            }
            if (placeholderValue.equalsIgnoreCase(equalsValue)) {
                return false;
            }
        }
        return true;
    }


    public boolean isPlaceholder(String input) {
        if (input == null || input.length() < 3) {
            return false;
        }

        return input.startsWith("%") && input.endsWith("%");
    }

    /**
     * Periodically checks the fly status of all players and deactivates fly if they are not authorized.
     * Also teleports the player to a safe location if necessary and executes commands if required.
     */
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
                        String placeholderValue = PlaceholderAPI.setPlaceholders(player, c.getPlaceholder());
                        String equalsValue = PlaceholderAPI.setPlaceholders(player, c.getEqualsValue());
                        if (placeholderValue.equalsIgnoreCase(equalsValue)) {
                            for (String command : c.getCommands()) {
                                command = command.replace("%player%", player.getName());
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    /**
     * Gets a safe location for the player to teleport to if they are not authorized to fly.
     * The location will be the highest block in the world that is not AIR.
     *
     * @param player the player to check
     * @return the safe location for the player
     */
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
}
