package com.wayvi.wfly.wflyV2.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class MiniMessageSupportUtil {


        public static String translateToSpigot(String minimessage) {
            // Utilisation d'un parser Minimessage pour convertir la chaîne en Component
            Component component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(minimessage);

            // Sérialisation du Component en texte formaté pour Spigot
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        // Méthode d'envoi du message converti à un joueur
        public static void sendMiniMessageFormat(Player player, String minimessage) {
            String spigotMessage = translateToSpigot(minimessage);
            player.sendMessage(spigotMessage);
        }
    }

