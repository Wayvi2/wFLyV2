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

import java.util.Map;
import java.util.UUID;

/**
 * Custom placeholder expansion for the WFlyV2 plugin that allows retrieving flying-related data
 * through PlaceholderAPI.
 */
public class WFlyPlaceholder extends PlaceholderExpansion {

    private final WFlyV2 plugin;
    private FlyTimeHybridRepository flyTimeHybridRepository;


    public WFlyPlaceholder(WFlyV2 plugin, FlyTimeRepository flyTimeRepository){
        this.plugin = plugin;
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
        return "1.0.3.1";
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
                    AccessPlayerDTO isFlying = flyTimeHybridRepository.getPlayerFlyData(player.getUniqueId());
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
    public static String formatTime(WFlyV2 plugin, int seconds) {
        Map<String, Boolean> enabledFormats = WflyApi.get().getPlugin().getTimeFormatTranslatorUtil().getTimeUnitsEnabled();
        String format = WflyApi.get().getPlugin().getTimeFormatTranslatorUtil().getPlaceholderFormat();

        boolean autoFormat = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_AUTO_FORMAT);
        boolean removeNullValues = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_ENABLED);
        String nullValue = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_VALUE);

        String secondsSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_SECONDS);
        String minutesSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_MINUTES);
        String hoursSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_HOURS);
        String daysSuffix = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_DAYS);

        secondsSuffix = secondsSuffix.trim();
        minutesSuffix = minutesSuffix.trim();
        hoursSuffix = hoursSuffix.trim();
        daysSuffix = daysSuffix.trim();

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

        // Amélioration : gestion intelligente des placeholders vides
        boolean showDays = days > 0 || !removeNullValues;
        boolean showHours = hours > 0 || !removeNullValues;
        boolean showMinutes = minutes > 0 || !removeNullValues;
        boolean showSeconds = sec > 0 || !removeNullValues;

        if (autoFormat) {
            if (!enabledFormats.getOrDefault("days", false) || !showDays) {

                format = format.replaceAll("\\s*%days%\\s*", "").replaceAll("\\s*%days_suffixe%\\s*", "");
            }
            if (!enabledFormats.getOrDefault("hours", false) || !showHours) {
                format = format.replaceAll("\\s*%hours%\\s*", "").replaceAll("\\s*%hours_suffixe%\\s*", "");
            }
            if (!enabledFormats.getOrDefault("minutes", false) || !showMinutes) {
                format = format.replaceAll("\\s*%minutes%\\s*", "").replaceAll("\\s*%minutes_suffixe%\\s*", "");
            }
            if (!enabledFormats.getOrDefault("seconds", false) || !showSeconds) {
                format = format.replaceAll("\\s*%seconds%\\s*", "").replaceAll("\\s*%seconds_suffixe%\\s*", "");
            }
        }

        String finalDays = showDays ? String.valueOf(days) : "";
        String finalHours = showHours ? String.valueOf(hours) : "";
        String finalMinutes = showMinutes ? String.valueOf(minutes) : "";
        String finalSeconds = showSeconds ? String.valueOf(sec) : "";

        String finalFormat = format
                .replace("%seconds%", finalSeconds)
                .replace("%minutes%", finalMinutes)
                .replace("%hours%", finalHours)
                .replace("%days%", finalDays)
                .replace("%seconds_suffixe%", finalSeconds.isEmpty() ? "" : secondsSuffix)
                .replace("%minutes_suffixe%", finalMinutes.isEmpty() ? "" : minutesSuffix)
                .replace("%hours_suffixe%", finalHours.isEmpty() ? "" : hoursSuffix)
                .replace("%days_suffixe%", finalDays.isEmpty() ? "" : daysSuffix);

        // Si tous les éléments sont vides, retourner la valeur null configurée
        if (finalDays.isEmpty() && finalHours.isEmpty() && finalMinutes.isEmpty() && finalSeconds.isEmpty()) {
            finalFormat = nullValue;
        }

        // Nettoyage amélioré des espaces
        finalFormat = finalFormat
                .replaceAll("\\r?\\n", "")
                .replaceAll("\\t", "")
                .replaceAll("\\s{2,}", " ")
                .replaceAll("^\\s+|\\s+$", "")
                .replaceAll("\\s+([#&§])", "$1")
                .replaceAll("([#&§][0-9a-fA-F])\\s+", "$1");

        return (String) ColorSupportUtil.convertColorFormat(finalFormat);
    }

}
