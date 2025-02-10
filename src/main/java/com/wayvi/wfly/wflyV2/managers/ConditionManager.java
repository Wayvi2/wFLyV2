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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
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
        return false;
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
        return false;
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

                    if (shouldDisable && player.isFlying()) {
                        flyStateCache.put(accessPlayerDTO.uniqueId(), false);
                        plugin.getFlyManager().manageFly(accessPlayerDTO.uniqueId(), false);

                        Location playerLocation = player.getLocation();
                        int highestY = player.getWorld().getHighestBlockYAt(playerLocation);
                        Location safeLocation = new Location(player.getWorld(), playerLocation.getX(), highestY + 1, playerLocation.getZ());

                        if (!safeLocation.equals(lastSafeLocation.get(player.getUniqueId()))) {
                            ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.fly-deactivated"));
                            if (!player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || !player.isOp()){
                                player.teleport(safeLocation);
                                lastSafeLocation.put(player.getUniqueId(), safeLocation);
                            }

                            // POUR LA SECTION NOT AUTHORIZED
                            if (config.isConfigurationSection("conditions.not-authorized")) {
                                ConfigurationSection conditionsSection = config.getConfigurationSection("conditions.not-authorized");
                                for (String key : conditionsSection.getKeys(false)) {
                                    List<String> commands = conditionsSection.getStringList(key + ".commands");
                                    if (commands != null) {
                                        for (String command : commands) {
                                            command = command.replace("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in checkCanFly: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 20L, 20L);
    }
}
