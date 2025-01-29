package com.wayvi.wfly.wflyV2.util;

import java.util.List;
import java.util.Map;

public class TimeFormatTranslatorUtil {

    private final ConfigUtil configUtil;

    public TimeFormatTranslatorUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    public List<String> getTimeFormat() {
        return configUtil.getCustomConfig().getStringList("time-format");
    }

    public String getPlaceholderFormat() {
        return configUtil.getCustomConfig().getString("format-placeholder.format");
    }

    public Map<String, Boolean> getTimeUnitsEnabled() {
        return Map.of(
                "seconds", configUtil.getCustomConfig().getBoolean("format-placeholder.seconds"),
                "minutes", configUtil.getCustomConfig().getBoolean("format-placeholder.minutes"),
                "hours", configUtil.getCustomConfig().getBoolean("format-placeholder.hours"),
                "days", configUtil.getCustomConfig().getBoolean("format-placeholder.days")
        );
    }
}
