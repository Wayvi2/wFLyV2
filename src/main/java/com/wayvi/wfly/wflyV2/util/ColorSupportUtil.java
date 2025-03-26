package com.wayvi.wfly.wflyV2.util;

import org.bukkit.entity.Player;

/**
 * Utility class that provides methods to send color-formatted messages to players and convert color formats.
 * This class helps in supporting color formatting in messages using MiniMessage and Hexadecimal colors.
 */
public class ColorSupportUtil {

    /**
     * Sends a color-formatted message to a player.
     * The message is converted from MiniMessage format to a format supported by Spigot (using Hexadecimal color codes).
     *
     * @param player the player to send the message to
     * @param minimessage the MiniMessage string to be sent, which may contain color codes in Hexadecimal format
     */
    public static void sendColorFormat(Player player, String minimessage) {
        String spigotMessage = HexaColorSupportUtil.hex(minimessage);
        player.sendMessage(spigotMessage);
    }

    /**
     * Converts a MiniMessage string to a color-formatted string supported by Spigot (using Hexadecimal color codes).
     *
     * @param minimessage the MiniMessage string containing color codes
     * @return the converted color-formatted string in Spigot-compatible format
     */
    public static Object convertColorFormat(String minimessage) {
        return HexaColorSupportUtil.hex(minimessage);
    }
}
