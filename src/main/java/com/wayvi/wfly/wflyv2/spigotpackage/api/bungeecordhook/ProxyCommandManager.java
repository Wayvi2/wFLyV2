package com.wayvi.wfly.wflyv2.spigotpackage.api.bungeecordhook;

import com.wayvi.wfly.wflyv2.spigotpackage.WFlyV2;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class ProxyCommandManager implements PluginMessageListener {

    private final Map<String, ProxyCommandHandler> handlers = new HashMap<>();
    private final WFlyV2 plugin;

    public ProxyCommandManager(WFlyV2 plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "my:channel", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "my:channel");
    }

    public void addProxyCommand(String command, ProxyCommandHandler handler) {
        handlers.put(command, handler);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("my:channel")) return;

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String subCommand = in.readUTF();
            int argsLength = in.readInt();
            String[] args = new String[argsLength];
            for (int i = 0; i < argsLength; i++) {
                args[i] = in.readUTF();
            }

            final ProxyCommandHandler handler = handlers.get(subCommand);
            if (handler != null) {
                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        handler.handle(args);
                    }
                });
            } else {
                plugin.getLogger().warning("Commande proxy inconnue reçue: " + subCommand);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
