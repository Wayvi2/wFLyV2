package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import fr.maxlego08.sarah.RequestHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConditionManager {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final RequestHelper requestHelper;
    private final Map<UUID, Boolean> flyStateCache = new HashMap<>();
    Map<UUID, Location> lastSafeLocation = new HashMap<>();

    public ConditionManager(WFlyV2 plugin, ConfigUtil configUtil, RequestHelper requestHelper) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.requestHelper = requestHelper;
    }

    public boolean cannotFly(Player player) {

        FileConfiguration config = configUtil.getCustomConfig();

        if (config.isConfigurationSection("conditions")) {
            ConfigurationSection conditionsSection = config.getConfigurationSection("conditions.not-authorized");
            if (conditionsSection != null) {
                for (String key : conditionsSection.getKeys(false)) {
                    String placeholder = conditionsSection.getString(key + ".placeholder");
                    String value = conditionsSection.getString(key + ".equals");

                    if (placeholder == null || value == null) {
                        plugin.getLogger().warning("Invalid condition configuration for key: " + key + " Skipping...");
                        continue;
                    }

                    String actualPlaceholder = PlaceholderAPI.setPlaceholders(player, placeholder);
                    String actualValue = PlaceholderAPI.setPlaceholders(player, value);

                    if (actualValue.equalsIgnoreCase(actualPlaceholder)) {
                        return true;
                    }
                }
            }
        }
        return player.isOp() || player.hasPermission(Permissions.BYPASS_FLY.getPermission());
    }

    public boolean canFly(Player player) {

        FileConfiguration config = configUtil.getCustomConfig();

        if (config.isConfigurationSection("conditions")) {
            ConfigurationSection conditionsSection = config.getConfigurationSection("conditions.authorized");
            if (conditionsSection != null) {
                for (String key : conditionsSection.getKeys(false)) {
                    String placeholder = conditionsSection.getString(  key + ".placeholder");
                    String value = conditionsSection.getString( key + ".equals");

                    if (placeholder == null || value == null) {
                        plugin.getLogger().warning("Invalid condition configuration for key: " + key+ " Skipping..."); ;
                        continue;
                    }

                    String actualPlaceholder = PlaceholderAPI.setPlaceholders(player, placeholder);
                    String actualValue = PlaceholderAPI.setPlaceholders(player, value);


                    if (actualValue.equalsIgnoreCase(actualPlaceholder)) {
                        return true;
                    }
                }
            }
        }
        return player.isOp() || player.hasPermission(Permissions.BYPASS_FLY.getPermission());
    }

    public void checkCanFly() {
        FileConfiguration config = configUtil.getCustomConfig();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<AccessPlayerDTO> flyPlayers = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});
            for (AccessPlayerDTO accessPlayerDTO : flyPlayers) {
                Player player = Bukkit.getPlayer(accessPlayerDTO.uniqueId());
                if (player == null) continue;

                try {
                    boolean shouldEnable = canFly(player);
                    boolean shouldDisable = cannotFly(player) && !shouldEnable;

                    if (!shouldDisable || !player.isFlying()) continue;

                    flyStateCache.put(accessPlayerDTO.uniqueId(), false);

                    plugin.getFlyManager().manageFly(accessPlayerDTO.uniqueId(), false);

                    Location safeLocation = getSafeLocation(player);
                    if (safeLocation.equals(lastSafeLocation.get(player.getUniqueId()))) continue;

                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));

                    if (player.getWorld().getEnvironment() != World.Environment.NETHER) {
                        player.teleport(safeLocation);
                        lastSafeLocation.put(player.getUniqueId(), safeLocation);
                    }

                    if (!config.isConfigurationSection("conditions.not-authorized")) continue;

                    ConfigurationSection conditionsSection = config.getConfigurationSection("conditions.not-authorized");
                    for (String key : conditionsSection.getKeys(false)) {
                        List<String> commands = conditionsSection.getStringList(key + ".commands");

                        for (String command : commands) {
                            command = command.replace("%player%", player.getName());
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                        }
                    }

                } catch (Exception e) {
                    plugin.getLogger().severe("Error in checkCanFly: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 20L, 20L);
    }

    private Location getSafeLocation(Player player) {

        Location loc = player.getLocation();
        World world = player.getWorld();
        int y = loc.getBlockY();

        while (world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) {
            y--;
        }

        return new Location(world, loc.getX(), y + 1, loc.getZ());
    }
}
