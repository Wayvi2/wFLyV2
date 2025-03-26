package com.wayvi.wfly.wflyV2.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for supporting hexadecimal color codes in Minecraft messages.
 * This class provides methods to convert hex color codes (e.g., #FF5733) into
 * Minecraft's supported color format using the '&' character for color codes.
 */
public class HexaColorSupportUtil {

    /**
     * Converts hexadecimal color codes in a message to Minecraft's supported color format.
     * It finds all occurrences of hex color codes in the format '#RRGGBB' and replaces them
     * with the Minecraft-compatible color codes.
     *
     * @param message the message containing hexadecimal color codes to convert
     * @return the message with converted Minecraft color codes
     */
    public static String hex(String message) {
        // Define a regular expression pattern to match hexadecimal color codes
        Pattern pattern = Pattern.compile("(#[a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(message);

        // Find all hexadecimal color codes in the message and convert them to Minecraft color format
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            // Convert the hex color code to Minecraft format
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            // Replace the original hex code in the message with the Minecraft color code
            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }

        // Translate the color codes and return the final message
        return ChatColor.translateAlternateColorCodes('&', message).replace('&', '§');
    }
}
