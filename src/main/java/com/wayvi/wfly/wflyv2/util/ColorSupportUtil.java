package com.wayvi.wfly.wflyv2.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * Utility class that provides methods to send color-formatted messages to players and convert color formats.
 * This class helps in supporting color formatting in messages using Hexadecimal colors.
 */
public class ColorSupportUtil {

    /**
     * Sends a color-formatted message to a player.
     *
     * @param player the player to send the message to
     * @param message the Hexadecimal string to be sent, which may contain color codes in Hexadecimal format
     */
    public static void sendColorFormat(Player player, String message) {
        String spigotMessage = HexaColorSupportUtil.hex(message);
        player.sendMessage(PlaceholderAPI.setPlaceholders(player, spigotMessage));
    }

    /**
     * Converts a MiniMessage string to a color-formatted string supported by Spigot (using Hexadecimal color codes).
     *
     * @param message the message string containing color codes
     * @return the converted color-formatted string in Spigot-compatible format
     */
    public static Object convertColorFormat(String message) {
        return HexaColorSupportUtil.hex(message);
    }
}
