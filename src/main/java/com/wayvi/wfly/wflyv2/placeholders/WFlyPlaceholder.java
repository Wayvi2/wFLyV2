package com.wayvi.wfly.wflyv2.placeholders;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Custom placeholder expansion for the WFlyV2 plugin that allows retrieving flying-related data
 * through PlaceholderAPI.
 */
public class WFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;
    private FlyTimeRepository flyTimeRepository;

    public WFlyPlaceholder(WFlyV2 plugin, FlyTimeRepository flyTimeRepository){
        this.plugin = plugin;
        this.flyTimeRepository = flyTimeRepository;
    }

    /**
     * Constructs a new WFlyPlaceholder instance.
     *
     * @param plugin The main plugin instance.
     */
    public WFlyPlaceholder(WFlyV2 plugin) {
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
        return "1.0.2.9";
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
                    return formatTime(timeRemaining);
                case "fly_activate":
                    UUID player1 = offlinePlayer.getUniqueId();
                    AccessPlayerDTO isFlying = flyTimeRepository.getPlayerFlyData(player.getUniqueId());
                    return String.valueOf(isFlying.isinFly());
            }
        }
        return null;
    }

    /**
     * Formats the given time in seconds into a readable format, taking into account the enabled
     * time units (days, hours, minutes, seconds) and applying the specified format.
     *
     * @param seconds The time in seconds to be formatted.
     * @return A formatted string representing the time.
     */
    public String formatTime(int seconds) {
        Map<String, Boolean> enabledFormats = WflyApi.get().getPlugin().getTimeFormatTranslatorUtil().getTimeUnitsEnabled();
        String format = WflyApi.get().getPlugin().getTimeFormatTranslatorUtil().getPlaceholderFormat();



        boolean autoFormat = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_AUTO_FORMAT);
        boolean removeNullValues = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_ENABLED);
        String nullValue = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_VALUE);

        String secondsSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_SECONDS);
        String minutesSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_MINUTES);
        String hoursSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_HOURS);
        String daysSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_DAYS);


        int days = seconds / 86400;
        int hours = (seconds % 86400) / 3600;
        int minutes = (seconds % 3600) / 60;
        int sec = seconds % 60;

        if (!enabledFormats.get("days")) {
            hours += days * 24;
            days = 0;
        }
        if (!enabledFormats.get("hours")) {
            minutes += hours * 60;
            hours = 0;
        }
        if (!enabledFormats.get("minutes")) {
            sec += minutes * 60;
            minutes = 0;
        }
        if (enabledFormats.get("minutes")) {
            minutes += sec / 60;
            sec %= 60;
        }
        if (enabledFormats.get("hours")) {
            hours += minutes / 60;
            minutes %= 60;
        }
        if (enabledFormats.get("days")) {
            days += hours / 24;
            hours %= 24;
        }

        if (autoFormat) {
            if (!enabledFormats.getOrDefault("days", false)) {
                format = format.replace("%days%", "").replace("%days_suffixe%", "");
            }
            if (!enabledFormats.getOrDefault("hours", false)) {
                format = format.replace("%hours%", "").replace("%hours_suffixe%", "");
            }
            if (!enabledFormats.getOrDefault("minutes", false)) {
                format = format.replace("%minutes%", "").replace("%minutes_suffixe%", "");
            }
            if (!enabledFormats.getOrDefault("seconds", false)) {
                format = format.replace("%seconds%", "").replace("%seconds_suffixe%", "");
            }
        }

        String finalDays = (removeNullValues && days == 0) ? "" : String.valueOf(days);
        String finalHours = (removeNullValues && hours == 0) ? "" : String.valueOf(hours);
        String finalMinutes = (removeNullValues && minutes == 0) ? "" : String.valueOf(minutes);
        String finalSeconds = (removeNullValues && sec == 0) ? "" : String.valueOf(sec);

        String finalFormat = format
                .replace("%seconds%", finalSeconds)
                .replace("%minutes%", finalMinutes)
                .replace("%hours%", finalHours)
                .replace("%days%", finalDays)
                .replace("%seconds_suffixe%", finalSeconds.isEmpty() ? "" : secondsSuffix)
                .replace("%minutes_suffixe%", finalMinutes.isEmpty() ? "" : minutesSuffix)
                .replace("%hours_suffixe%", finalHours.isEmpty() ? "" : hoursSuffix)
                .replace("%days_suffixe%", finalDays.isEmpty() ? "" : daysSuffix);

        if (finalDays.isEmpty() && finalHours.isEmpty() && finalMinutes.isEmpty() && finalSeconds.isEmpty()) {
            finalFormat = nullValue;
        }

        finalFormat = finalFormat.replaceAll("\\s+", " ").trim();

        return (String) ColorSupportUtil.convertColorFormat(finalFormat);
    }

}
