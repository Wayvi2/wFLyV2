package com.wayvi.wfly.wflyV2.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;



public class MiniMessageSupportUtil {


        public static String translateToSpigot(String minimessage) {

            Component component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(minimessage);
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        public static void sendMiniMessageFormat(Player player, String minimessage) {
            String spigotMessage = translateToSpigot(minimessage);
            player.sendMessage(spigotMessage);
        }
    }

