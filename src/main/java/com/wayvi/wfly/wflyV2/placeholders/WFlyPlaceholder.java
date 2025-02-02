package com.wayvi.wfly.wflyV2.placeholders;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class WFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;

    public WFlyPlaceholder(WFlyV2 plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wfly";
    }

    @Override
    public @NotNull String getAuthor() {
        return "wPlugin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();


            if (params.equals("fly_remaining")) {
                int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(player);
                if (player.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                    return (String) ColorSupportUtil.convertColorFormat(configUtil.getCustomConfig().getString("format-placeholder.unlimited"));
                }
                return formatTime(timeRemaining);
            }

            if (params.equals("fly_activate")) {
                try {
                    UUID player1 = offlinePlayer.getUniqueId();
                    AccessPlayerDTO isFlying = plugin.getFlyManager().getPlayerFlyData(player1);
                    return String.valueOf(isFlying.isinFly());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private String formatTime(int seconds) {
        Map<String, Boolean> enabledFormats = plugin.getTimeFormatTranslatorUtil().getTimeUnitsEnabled();
        String format = plugin.getTimeFormatTranslatorUtil().getPlaceholderFormat();
        boolean autoFormat = configUtil.getCustomConfig().getBoolean("format-placeholder.auto-format");


        String secondsSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.seconds_suffixe");
        String minutesSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.minutes_suffixe");
        String hoursSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.hours_suffixe");
        String daysSuffix = configUtil.getCustomConfig().getString("format-placeholder.other-format.days_suffixe");

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
            sec = sec % 60;
        }

        if (enabledFormats.get("hours")) {
            hours += minutes / 60;
            minutes = minutes % 60;
        }

        if (enabledFormats.get("days")) {
            days += hours / 24;
            hours = hours % 24;
        }

        if (!autoFormat) {
            format = format.replace("%seconds%", sec + "");
            format = format.replace("%minutes%", minutes + "");
            format = format.replace("%hours%", hours + "");
            format = format.replace("%days%", days + "");
            format = format.replace("%seconds_suffixe%", secondsSuffix);
            format = format.replace("%minutes_suffixe%", minutesSuffix);
            format = format.replace("%hours_suffixe%", hoursSuffix);
            format = format.replace("%days_suffixe%", daysSuffix);
            return (String) ColorSupportUtil.convertColorFormat(format);
        }

        format = format.replace("%seconds%", enabledFormats.get("seconds") ? sec + "" : "");
        format = format.replace("%minutes%", enabledFormats.get("minutes") ? minutes + "" : "");
        format = format.replace("%hours%", enabledFormats.get("hours") ? hours + "" : "");
        format = format.replace("%days%", enabledFormats.get("days") ? days + "" : "");

        format = format.replace("%seconds_suffixe%", enabledFormats.get("seconds") ? secondsSuffix : "");
        format = format.replace("%minutes_suffixe%", enabledFormats.get("minutes") ? minutesSuffix : "");
        format = format.replace("%hours_suffixe%", enabledFormats.get("hours") ? hoursSuffix : "");
        format = format.replace("%days_suffixe%", enabledFormats.get("days") ? daysSuffix : "");

        format = format.replaceAll("\\s+", " ").trim();

        return (String) ColorSupportUtil.convertColorFormat(format);
    }
}
