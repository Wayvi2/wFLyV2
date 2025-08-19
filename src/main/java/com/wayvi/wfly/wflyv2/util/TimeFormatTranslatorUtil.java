package com.wayvi.wfly.wflyv2.util;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for managing and translating time format configurations.
 * This class provides methods to retrieve time-related configuration data,
 * such as time format and enabled units (seconds, minutes, hours, days)
 * based on the settings defined in the plugin's configuration.
 */
public class TimeFormatTranslatorUtil {

    private WFlyV2 plugin;

    /**
     * Constructs a new TimeFormatTranslatorUtil instance.
     *
     */
    public TimeFormatTranslatorUtil(WFlyV2 plugin) {
        this.plugin = plugin;
    }


    /**
     * Retrieves the placeholder format defined in the configuration.
     *
     * @return the format for placeholders as a string
     */
    public String getPlaceholderFormat() {
        return plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_FORMAT);
    }

    /**
     * Retrieves a map of time units and their enabled/disabled state based on the configuration.
     * The map contains time units such as seconds, minutes, hours, and days with their respective
     * boolean values indicating whether the unit is enabled.
     *
     * @return a map with time units as keys and their enabled state as boolean values
     */
    public Map<String, Boolean> getTimeUnitsEnabled() {
        Map<String, Boolean> timeUnits = new HashMap<>();
        timeUnits.put("seconds", plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_SECONDS));
        timeUnits.put("minutes", plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_MINUTES));
        timeUnits.put("hours", plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_HOURS));
        timeUnits.put("days", plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_DAYS));

        return timeUnits;
    }
}
