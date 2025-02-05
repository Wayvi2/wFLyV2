package com.wayvi.wfly.wflyV2.managers;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import fr.maxlego08.sarah.RequestHelper;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
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
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<AccessPlayerDTO> flyPlayers = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});
            for (AccessPlayerDTO accessPlayerDTO : flyPlayers) {
                Player player = Bukkit.getPlayer(accessPlayerDTO.uniqueId());
                if (player == null) continue;

                //DEBUG IF NEEDED
                //Bukkit.broadcastMessage(String.valueOf(cannotFly(player)));
                //Bukkit.broadcastMessage(String.valueOf(canFly(player)));

                try {
                    FileConfiguration config = configUtil.getCustomConfig();
                    ConfigurationSection notAuthorizedSection = config.getConfigurationSection("conditions.not-authorized");

                    if (notAuthorizedSection != null) {
                        for (String key : notAuthorizedSection.getKeys(false)) {
                            String placeholder = notAuthorizedSection.getString(key + ".placeholder");
                            String value = notAuthorizedSection.getString(key + ".equals");

                            if (placeholder == null || value == null) {
                                plugin.getLogger().warning("Invalid condition configuration for key: " + key + " Skipping...");
                                continue;
                            }

                            boolean shouldDisable = cannotFly(player) && !canFly(player);

                            if (!flyStateCache.containsKey(accessPlayerDTO.uniqueId()) || flyStateCache.get(accessPlayerDTO.uniqueId()) != shouldDisable) {
                                flyStateCache.put(accessPlayerDTO.uniqueId(), shouldDisable);
                                plugin.getFlyManager().manageFly(accessPlayerDTO.uniqueId(), !shouldDisable);
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe(e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 20L, 20L);
    }
}
