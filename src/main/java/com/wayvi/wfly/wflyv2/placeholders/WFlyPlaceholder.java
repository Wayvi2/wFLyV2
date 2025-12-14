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
        return "1.0.4.2";
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
                    return formatTime(plugin,timeRemaining, false);
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





    public static String formatTime(WFlyV2 plugin, int totalSeconds, boolean always) {



        String nullValue = plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_VALUE);
        Map<String, Boolean> enabledFormats = WflyApi.get()
                .getPlugin()
                .getTimeFormatTranslatorUtil()
                .getTimeUnitsEnabled();


        boolean removeNull = always ? true : plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_REMOVE_NULL_ENABLED);
        boolean autoFormat = always ? true : plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_AUTO_FORMAT);

        if (always) {
            enabledFormats.replaceAll((key, oldValue) -> true);
        }

        String mainTemplate = WflyApi.get()
                .getPlugin()
                .getTimeFormatTranslatorUtil()
                .getPlaceholderFormat();

        Map<String, String> unitFormats = new HashMap<>();
        unitFormats.put("seconds", (String) plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_SECONDS));
        unitFormats.put("minutes", (String) plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_MINUTES));
        unitFormats.put("hours", (String) plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_HOURS));
        unitFormats.put("days", (String) plugin.getConfigFile().get(ConfigEnum.FORMAT_PLACEHOLDER_OTHER_DAYS));

        if (removeNull && totalSeconds == 0) {
            return (String) ColorSupportUtil.convertColorFormat(nullValue);
        }

        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("days", totalSeconds / 86400);
        values.put("hours", (totalSeconds % 86400) / 3600);
        values.put("minutes", (totalSeconds % 3600) / 60);
        values.put("seconds", totalSeconds % 60);

        boolean hasRealValueInEnabledUnits = false;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            String unit = entry.getKey();
            int value = entry.getValue();
            boolean isEnabled = enabledFormats.getOrDefault(unit, false);

            if (isEnabled && value > 0) {
                hasRealValueInEnabledUnits = true;
                break;
            }
        }

        boolean useFallbackLogic = (totalSeconds > 0 && !hasRealValueInEnabledUnits);

        String finalString = mainTemplate;

        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            String unit = entry.getKey();
            int value = entry.getValue();
            String placeholder = "%" + unit + "%";
            String replacement = "";

            boolean isEnabled = enabledFormats.getOrDefault(unit, false);

            if (isEnabled) {
                if (!autoFormat || value > 0) {
                    String unitTemplate = unitFormats.get(unit);
                    replacement = unitTemplate.replace(placeholder, String.valueOf(value));
                }
            } else if (useFallbackLogic && unit.equals("seconds")) {
                String unitTemplate = unitFormats.get("seconds");
                replacement = unitTemplate.replace("%seconds%", String.valueOf(totalSeconds));
            }

            finalString = finalString.replace(placeholder, replacement);
        }

        String cleanedString = finalString.trim().replaceAll(" +", " ");

        if (cleanedString.isEmpty()) {
            return (String) ColorSupportUtil.convertColorFormat(nullValue);
        }

        return (String) ColorSupportUtil.convertColorFormat(cleanedString);
    }

}


