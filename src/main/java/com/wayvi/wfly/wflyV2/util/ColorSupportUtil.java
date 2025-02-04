package com.wayvi.wfly.wflyV2.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;



public class ColorSupportUtil {

    public static String translateToSpigot(String minimessage) {

        Component component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(minimessage);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static void sendColorFormat(Player player, String minimessage) {
        if (getPaperOrSpigot()) {
            String paperMessage = translateToSpigot(minimessage);
            player.sendMessage(paperMessage);
            return;
        }
        String spigotMessage = HexaColorSupportUtil.hex(minimessage);
        player.sendMessage(spigotMessage);
    }

    public static Object convertColorFormat(String minimessage) {
        if (getPaperOrSpigot()){
            return translateToSpigot(minimessage);
        }
        return HexaColorSupportUtil.hex(minimessage);
    }

    public static boolean getPaperOrSpigot() {

        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }

    }
}

