package com.wayvi.wfly.wflyV2.placeholders;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;

public class TimeFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;

    ConfigUtil configUtil;

    public TimeFlyPlaceholder(WFlyV2 plugin, ConfigUtil configUtil) {
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
                try {
                    int timeRemaining = plugin.getTimeFlyManager().getTimeRemaining(player);
                    return formatTime(timeRemaining);
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

        if (autoFormat) {
            format = format
                    .replace("%days%", enabledFormats.get("days") ? "%days%" : "")
                    .replace("%hours%", enabledFormats.get("hours") ? "%hours%" : "")
                    .replace("%minutes%", enabledFormats.get("minutes") ? "%minutes%" : "")
                    .replace("%seconds%", enabledFormats.get("seconds") ? "%seconds%" : "")
                    .replaceAll("\\s+", " ") // Supprime les espaces inutiles
                    .trim();
        }

        String result = format
                .replace("%days%", days + "j")
                .replace("%hours%", hours + "h")
                .replace("%minutes%", minutes + "m")
                .replace("%seconds%", sec + "s");

        return result.replaceAll("\\s+", " ").trim();
    }



}
