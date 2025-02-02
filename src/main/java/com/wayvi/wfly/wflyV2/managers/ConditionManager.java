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
import java.util.List;

public class ConditionManager {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final RequestHelper requestHelper;

    public ConditionManager(WFlyV2 plugin, ConfigUtil configUtil, RequestHelper requestHelper) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.requestHelper = requestHelper;
    }

    public boolean canFly(Player player) {

        if (player.isOp()) {
            return false;
        }

        FileConfiguration config = configUtil.getCustomConfig();

        if (config.isConfigurationSection("conditions")) {
            ConfigurationSection conditionsSection = config.getConfigurationSection("conditions");
            if (conditionsSection != null) {
                for (String key : conditionsSection.getKeys(false)) {
                    String placeholder = conditionsSection.getString( key + ".placeholder");
                    String value = conditionsSection.getString(key + ".equals");

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
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            List<AccessPlayerDTO> fly = this.requestHelper.select("fly", AccessPlayerDTO.class, table -> {});

            for (AccessPlayerDTO accessPlayerDTO : fly) {
                Player player = Bukkit.getPlayer(accessPlayerDTO.uniqueId());
                try {
                    if (player != null && canFly(player)){
                        plugin.getFlyManager().manageFly(player.getUniqueId(), false);

                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe(e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 20L, 20L);
    }
}
