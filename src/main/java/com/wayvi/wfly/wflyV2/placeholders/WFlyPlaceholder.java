package com.wayvi.wfly.wflyV2.placeholders;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Custom placeholder expansion for the WFlyV2 plugin that allows retrieving flying-related data
 * through PlaceholderAPI.
 */
public class WFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;

    /**
     * Constructs a new WFlyPlaceholder instance.
     *
     * @param plugin The main plugin instance.
     * @param configUtil The configuration utility.
     */
    public WFlyPlaceholder(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
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
        return "1.0.8";
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
                    int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(player);
                    if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                        return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomConfig().getString("format-placeholder.unlimited"));
                    }
                    return formatTime(timeRemaining);
                case "fly_activate":
                    try {
                        UUID player1 = offlinePlayer.getUniqueId();
                        AccessPlayerDTO isFlying = plugin.getFlyManager().getPlayerFlyData(player1);
                        return String.valueOf(isFlying.isinFly());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                case "fly_remaining_seconds":
                    UUID player1 = offlinePlayer.getUniqueId();
                    return String.valueOf(plugin.getTimeFlyManager().getTimeRemaining(Bukkit.getPlayer(player1)));
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
    private String formatTime(int seconds) {
        Map<String, Boolean> enabledFormats = plugin.getTimeFormatTranslatorUtil().getTimeUnitsEnabled();
        String format = plugin.getTimeFormatTranslatorUtil().getPlaceholderFormat();
        boolean autoFormat = configUtil.getCustomConfig().getBoolean("format-placeholder.auto-format");
        boolean removeNullValues = configUtil.getCustomConfig().getBoolean("format-placeholder.remove-null-values.enabled");
        String nullValue = configUtil.getCustomConfig().getString("format-placeholder.remove-null-values.value", "0");

        String secondsSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.seconds_suffixe", "s");
        String minutesSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.minutes_suffixe", "m");
        String hoursSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.hours_suffixe", "h");
        String daysSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.days_suffixe", "j");

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
