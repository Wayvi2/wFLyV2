package com.wayvi.wfly.wflyv2;

import com.wayvi.wfly.wflyv2.util.NMSUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActionBar {

    public static void sendActionBar(Player player, String message) {
        if (player == null) return;

        try {
            player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(message)
            );
            return;
        } catch (Throwable ignored) { }

        try {
            Object icbc = Class.forName("net.minecraft.server." + getVersion() + ".IChatBaseComponent$ChatSerializer")
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}");

            Object packet = Class.forName("net.minecraft.server." + getVersion() + ".PacketPlayOutChat")
                    .getConstructor(
                            Class.forName("net.minecraft.server." + getVersion() + ".IChatBaseComponent"),
                            byte.class
                    )
                    .newInstance(icbc, (byte) 2);

            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            connection.getClass().getMethod("sendPacket",
                            Class.forName("net.minecraft.server." + getVersion() + ".Packet"))
                    .invoke(connection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}