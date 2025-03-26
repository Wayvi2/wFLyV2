package com.wayvi.wfly.wflyV2.util;

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

    private final ConfigUtil configUtil;

    /**
     * Constructs a new TimeFormatTranslatorUtil instance.
     *
     * @param configUtil the ConfigUtil instance used to retrieve configuration data
     */
    public TimeFormatTranslatorUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    /**
     * Retrieves the list of time formats defined in the configuration.
     *
     * @return a list of time format strings
     */
    public List<String> getTimeFormat() {
        return configUtil.getCustomConfig().getStringList("time-format");
    }

    /**
     * Retrieves the placeholder format defined in the configuration.
     *
     * @return the format for placeholders as a string
     */
    public String getPlaceholderFormat() {
        return configUtil.getCustomConfig().getString("format-placeholder.format");
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
        timeUnits.put("seconds", configUtil.getCustomConfig().getBoolean("format-placeholder.seconds"));
        timeUnits.put("minutes", configUtil.getCustomConfig().getBoolean("format-placeholder.minutes"));
        timeUnits.put("hours", configUtil.getCustomConfig().getBoolean("format-placeholder.hours"));
        timeUnits.put("days", configUtil.getCustomConfig().getBoolean("format-placeholder.days"));
        return timeUnits;
    }
}
