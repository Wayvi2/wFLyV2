package com.wayvi.wfly.wflyv2.placeholders;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.storage.FlyTimeHybridRepository;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom placeholder expansion for the WFlyV2 plugin that allows retrieving flying-related data
 * through PlaceholderAPI.
 */
public class WFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;


    public WFlyPlaceholder(WFlyV2 plugin){
        this.plugin = plugin;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getIdentifier() {
        return "wfly";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getAuthor() {
        return "Wayvi2";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getVersion() {
        return "1.0.4.0";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Handles the request for a specific placeholder. This method will return the corresponding
     * value for the placeholder request related to the fly state of the player.
     *
     * @param offlinePlayer The offline player whose placeholder is requested.
     * @param params The placeholder parameters.
     * @return The value for the placeholder, or null if not handled.
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();


            switch (params) {
                case "fly_remaining":
                    int timeRemaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
                    if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                        return (String) ColorSupportUtil.convertColorFormat(plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_UNLIMITED));
                    }
                    return formatTime(plugin,timeRemaining);
                case "fly_activate":
                    UUID player1 = offlinePlayer.getUniqueId();
                    boolean playerIsFlying = WflyApi.get().getTimeFlyManager().getIsFlying(player1);
                    return String.valueOf(playerIsFlying);
                case "fly_remaining_seconds": {
                    int timeRemainingSeconds = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
                    return String.valueOf(timeRemainingSeconds);
                }

                case "fly_remaining_minutes": {
                    int timeRemainingSeconds = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
                    int minutes = timeRemainingSeconds >= 60 ? timeRemainingSeconds / 60 : 0;
                    return String.valueOf(minutes);
                }

                case "fly_remaining_hours": {
                    int timeRemainingSeconds = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
                    int hours = timeRemainingSeconds >= 3600 ? timeRemainingSeconds / 3600 : 0;
                    return String.valueOf(hours);
                }

                case "fly_remaining_days": {
                    int timeRemainingSeconds = WflyApi.get().getTimeFlyManager().getTimeRemaining(player);
                    int days = timeRemainingSeconds >= 86400 ? timeRemainingSeconds / 86400 : 0;
                    return String.valueOf(days);
                }
            }
        }
        return null;
    }


    public static String formatTime(WFlyV2 plugin, int totalSeconds) {
        Map<String, Boolean> enabledFormats = WflyApi.get()
                .getPlugin()
                .getTimeFormatTranslatorUtil()
                .getTimeUnitsEnabled();

        String result = WflyApi.get()
                .getPlugin()
                .getTimeFormatTranslatorUtil()
                .getPlaceholderFormat();
        boolean autoFormat = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_AUTO_FORMAT);
        boolean removeNull = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_ENABLED);
        String nullValue = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_VALUE);
        String secSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_SECONDS);
        String minSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_MINUTES);
        String hrSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_HOURS);
        String daySuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_DAYS);

        if (removeNull && totalSeconds == 0) {
            return (String) ColorSupportUtil.convertColorFormat(nullValue);
        }

        if (autoFormat) {
            int tempDays = totalSeconds / 86400;
            int tempHours = (totalSeconds % 86400) / 3600;
            int tempMinutes = (totalSeconds % 3600) / 60;

            if (tempDays > 0 && enabledFormats.getOrDefault("days", false)) {

            } else if (tempHours > 0 && enabledFormats.getOrDefault("hours", false)) {
                enabledFormats.put("days", false);
            } else if (tempMinutes > 0 && enabledFormats.getOrDefault("minutes", false)) {
                enabledFormats.put("days", false);
                enabledFormats.put("hours", false);
            } else if (totalSeconds > 0 && enabledFormats.getOrDefault("seconds", false)) {
                enabledFormats.put("days", false);
                enabledFormats.put("hours", false);
                enabledFormats.put("minutes", false);
            } else if (totalSeconds > 0) {
                enabledFormats.put("days", false);
                enabledFormats.put("hours", false);
                enabledFormats.put("minutes", false);
                enabledFormats.put("seconds", true);
            }
        }


        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        Map<String, Integer> unitToSeconds = new HashMap<>();
        unitToSeconds.put("days", 86400);
        unitToSeconds.put("hours", 3600);
        unitToSeconds.put("minutes", 60);
        unitToSeconds.put("seconds", 1);
        String[] unitOrder = {"days", "hours", "minutes", "seconds"};
        int remainingSeconds = totalSeconds;

        for (String unit : unitOrder) {
            int secondsInUnit = unitToSeconds.get(unit);

            if (enabledFormats.getOrDefault(unit, true)) {
                int value = remainingSeconds / secondsInUnit;
                values.put(unit, value);
                remainingSeconds = remainingSeconds % secondsInUnit;
            } else {
                values.put(unit, 0);
            }
        }

        LinkedHashMap<String, String> suffixes = new LinkedHashMap<>();
        suffixes.put("days", daySuffix);
        suffixes.put("hours", hrSuffix);
        suffixes.put("minutes", minSuffix);
        suffixes.put("seconds", secSuffix);

        for (String unit : unitOrder) {
            result = replaceUnit(result, unit, values.get(unit), suffixes.get(unit), enabledFormats, removeNull, nullValue);
        }
        String finalResult = result.trim();

        return (String) ColorSupportUtil.convertColorFormat(finalResult);
    }
    public static String formatTimeAlways(WFlyV2 plugin, int totalSeconds) {

        Map<String, Boolean> enabledFormats = new HashMap<>();
        enabledFormats.put("days", true);
        enabledFormats.put("hours", true);
        enabledFormats.put("minutes", true);
        enabledFormats.put("seconds", true);


        String result = WflyApi.get()
                .getPlugin()
                .getTimeFormatTranslatorUtil()
                .getPlaceholderFormat();


        boolean removeNull = true;

        String nullValue = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_VALUE);
        String secSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_SECONDS);
        String minSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_MINUTES);
        String hrSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_HOURS);
        String daySuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_DAYS);

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return (String) ColorSupportUtil.convertColorFormat(nullValue);
        }

        if (days > 0) {
        } else if (hours > 0) {
            enabledFormats.put("days", false);
        } else if (minutes > 0) {
            enabledFormats.put("days", false);
            enabledFormats.put("hours", false);
        } else {
            enabledFormats.put("days", false);
            enabledFormats.put("hours", false);
            enabledFormats.put("minutes", false);
        }

        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        values.put("days", days);
        values.put("hours", hours);
        values.put("minutes", minutes);
        values.put("seconds", seconds);

        Map<String, String> suffixes = new HashMap<>();
        suffixes.put("days", daySuffix);
        suffixes.put("hours", hrSuffix);
        suffixes.put("minutes", minSuffix);
        suffixes.put("seconds", secSuffix);

        Map<String, Integer> unitToSeconds = new HashMap<>();
        unitToSeconds.put("days", 86400);
        unitToSeconds.put("hours", 3600);
        unitToSeconds.put("minutes", 60);
        unitToSeconds.put("seconds", 1);

        String[] unitOrder = {"days", "hours", "minutes", "seconds"};

        for (int i = 0; i < unitOrder.length; i++) {
            String unit = unitOrder[i];
            int value = values.get(unit);

            if (value > 0 && !enabledFormats.getOrDefault(unit, true)) {
                for (int j = i + 1; j < unitOrder.length; j++) {
                    String nextUnit = unitOrder[j];

                    if (enabledFormats.getOrDefault(nextUnit, true)) {
                        int factor = unitToSeconds.get(unit) / unitToSeconds.get(nextUnit);
                        values.put(nextUnit, values.get(nextUnit) + (value * factor));
                        values.put(unit, 0);
                        break;
                    }
                }
            }
        }

        for (String unit : unitOrder) {
            result = replaceUnit(
                    result,
                    unit,
                    values.get(unit),
                    suffixes.get(unit),
                    enabledFormats,
                    removeNull,
                    nullValue
            );
        }
        String finalResult = result.trim().replaceAll("\\s{2,}", " ");

        return (String) ColorSupportUtil.convertColorFormat(finalResult);
    }


    private static String replaceUnit(String currentResult, String unit, int value, String suffix,
                                      Map<String, Boolean> enabledFormats, boolean removeNull, String nullValue) {

        String valuePlaceholder = "%" + unit + "%";
        String suffixPlaceholder = "%" + unit + "_suffixe%";

        boolean isEnabled = enabledFormats.getOrDefault(unit, true);
        boolean shouldShow = isEnabled && (value > 0 || !removeNull);

        String patternString = "(\\s*)" +
                "(" +
                "(\\S*#\\S+)" +
                "\\s*" +
                Pattern.quote(valuePlaceholder) +
                ".*?" +
                Pattern.quote(suffixPlaceholder) +
                ")";

        Pattern unitPattern = Pattern.compile(patternString);
        Matcher matcher = unitPattern.matcher(currentResult);

        if (matcher.find()) {
            String precedingSpaces = matcher.group(1);
            String fullBlock = matcher.group(2);

            if (shouldShow) {

                String finalReplacedBlock = fullBlock
                        .replace(valuePlaceholder, String.valueOf(value))
                        .replace(suffixPlaceholder, suffix);

                return currentResult.replace(fullBlock, finalReplacedBlock);

            } else {
                return currentResult.replace(precedingSpaces + fullBlock, "");
            }
        }
        return currentResult;
    }
}

