package com.wayvi.wfly.wflyv2.managers;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.ConditionManager;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.models.Condition;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class WConditionManager implements ConditionManager {

    private final WFlyV2 plugin;

    private List<Condition> authorizedConditions;
    private List<Condition> notAuthorizedConditions;
    private final Map<UUID, Location> lastSafeLocation = new HashMap<>();
    private final Set<String> unregisteredPlaceholders = new HashSet<>();
    private final Map<UUID, Boolean> flyPermissionCache = new HashMap<>();

    private final Map<UUID, Boolean> wasFlyingBefore = new HashMap<>();

    // ══════════════════════════════════════════════════════
    //                 CONSTRUCTOR & INIT
    // ══════════════════════════════════════════════════════

    /**
     * Creates a new WConditionManager with the given configuration utility.
     */
    public WConditionManager(WFlyV2 plugin) {
        this.plugin = plugin;
        loadConditions();
    }

    /**
     * Loads both authorized and non-authorized conditions from the configuration file.
     */
    @Override
    public void loadConditions() {
        resetUnregisteredPlaceholders();
        authorizedConditions = loadConditionsFromConfig("conditions.authorized");
        notAuthorizedConditions = loadConditionsFromConfig("conditions.not-authorized");
    }

    /**
     * Loads a list of conditions from the specified config path.
     *
     * @param path the config section path
     * @return list of loaded conditions
     */
    @Override
    public List<Condition> loadConditionsFromConfig(String path) {
        List<Condition> result = new ArrayList<>();
        ConfigurationSection section = plugin.getConfigFile().getRaw().getConfigurationSection(path);
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

    // ══════════════════════════════════════════════════════
    //                 PUBLIC MAIN METHODS
    // ══════════════════════════════════════════════════════

    /**
     * Checks if the given player is authorized to fly based on conditions and permissions.
     *
     * @param player the player to check
     * @return true if flight is authorized, false otherwise
     */
    @Override
    public boolean isFlyAuthorized(Player player) {
        if (hasBypassPermission(player)) return true;

        for (Condition c : authorizedConditions) {
            if (checkPlaceholder(player, c)) return true;
        }
        for (Condition c : notAuthorizedConditions) {
            if (checkPlaceholder(player, c)) return false;
        }
        return true;
    }

    /**
     * Starts a repeating task to check all online players' ability to fly.
     */
    @Override
    public void checkCanFly() {
        Bukkit.getScheduler().runTaskTimer(WflyApi.get().getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handlePlayerFlyState(player);
            }
        }, 20L, 20L);
    }

    /**
     * Executes commands associated with unauthorized flight conditions for a player.
     *
     * @param player the player for whom to execute the commands
     */
    @Override
    public void executeNotAuthorizedCommands(Player player) {
        for (Condition c : notAuthorizedConditions) {
            if (checkPlaceholder(player, c)) {
                for (String command : c.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            }
        }
    }

    /**
     * Gets a safe location for the player to teleport to when flight is disabled.
     *
     * @param player the player in question
     * @return the safe ground location, or current location if config disables the feature
     */
    @Override
    public Location getSafeLocation(Player player) {
        boolean tpOnFloor = plugin.getConfigFile().get(ConfigEnum.TP_ON_FLOOR_WHEN_FLY_DISABLED);
        if (!tpOnFloor) return null;

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Logs a placeholder error only once to avoid spam in the console.
     *
     * @param placeholder the unregistered or broken placeholder
     */
    @Override
    public void logPlaceholderError(String placeholder) {
        if (unregisteredPlaceholders.add(placeholder)) {
            WflyApi.get().getPlugin().getLogger().info("Placeholder not registered or not working: " + placeholder);
        }
    }

    /**
     * Clears the set of unregistered placeholders.
     */
    @Override
    public void resetUnregisteredPlaceholders() {
        unregisteredPlaceholders.clear();
    }

    // ══════════════════════════════════════════════════════
    //                INTERNAL LOGIC HANDLERS
    // ══════════════════════════════════════════════════════

    /**
     * Checks the current flight status of a player and disables flight if unauthorized.
     *
     * @param player the player to handle
     */
    private void handlePlayerFlyState(Player player) {
        if (hasBypassPermission(player)) return;

        UUID uuid = player.getUniqueId();
        boolean isAuthorized = WflyApi.get().getConditionManager().isFlyAuthorized(player);
        boolean isCurrentlyFlying = player.isFlying();
        boolean wasFlying = getWasFlyingBefore(player);


        if (!isAuthorized) {
            deactivateFlyForPlayer(player);
            setFlyingBefore(player, true);
            return;
        }

        boolean reactivate = plugin.getConfigFile().get(ConfigEnum.AUTO_REACTIVATE_FLY_AFTER_CONDITIONS_DISABLE);
        if (reactivate && wasFlying) {
            if (!player.isFlying() && !player.getAllowFlight()) {
                WflyApi.get().getFlyManager().manageFly(uuid, true);
                setFlyingBefore(player, false);
            }
        }
    }


    /**
     * Disables flight for a player and teleports them to a safe location.
     *
     * @param player the player to affect
     */
    private void deactivateFlyForPlayer(Player player) {
        if (WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId())) {

            if (player.isFlying()) {
                Location safeLocation = getSafeLocation(player);
                if (safeLocation == null) {
                    WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
                    return;
                }

                if (!safeLocation.equals(lastSafeLocation.get(player.getUniqueId()))) {
                    handleFlyDeactivation(player, safeLocation);
                }
            }



            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
            ColorSupportUtil.sendColorFormat(player, plugin.getMessageFile().get(MessageEnum.NO_FLY_HERE));


            executeNotAuthorizedCommands(player);
        }
    }

    /**
     * Handles teleporting and caching of a player's last safe location.
     *
     * @param player       the player to teleport
     * @param safeLocation the safe location to move them to
     */
    private void handleFlyDeactivation(Player player, Location safeLocation) {
        WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
        player.teleport(safeLocation);
        lastSafeLocation.put(player.getUniqueId(), safeLocation);
    }

    // ══════════════════════════════════════════════════════
    //               HELPER / UTILITY METHODS
    // ══════════════════════════════════════════════════════

    /**
     * Checks whether a placeholder for a condition matches its expected value.
     *
     * @param player     the player context
     * @param condition  the condition to check
     * @return true if the placeholder matches the expected value
     */
    public boolean checkPlaceholder(Player player, Condition condition) {
        String placeholderRaw = condition.getPlaceholder();
        String equalsRaw = condition.getEqualsValue();

        String placeholderValue = PlaceholderAPI.setPlaceholders(player, placeholderRaw);
        if (placeholderValue.equals(placeholderRaw)) {
            logPlaceholderError(placeholderRaw);
        }

        String equalsValue;
        if (equalsRaw.startsWith("%") && equalsRaw.endsWith("%")) {
            equalsValue = PlaceholderAPI.setPlaceholders(player, equalsRaw);
            if (equalsValue.equals(equalsRaw)) {
                logPlaceholderError(equalsRaw);
            }
        } else {
            equalsValue = equalsRaw;
        }

        return placeholderValue.equalsIgnoreCase(equalsValue);
    }




    /**
     * Checks if a player has permission to bypass the flight checks.
     *
     * @param player the player to check
     * @return true if the player can bypass all condition checks
     */
    public boolean hasBypassPermission(Player player) {
        return flyPermissionCache.computeIfAbsent(
                player.getUniqueId(),
                uuid -> player.isOp()
                        || player.hasPermission(Permissions.BYPASS_FLY.getPermission())
                        || player.getGameMode() == GameMode.SPECTATOR
        ) || player.hasPermission(Permissions.BYPASS_FLY.getPermission());
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
