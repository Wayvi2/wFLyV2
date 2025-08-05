package com.wayvi.wfly.wflyv2.bungeepackage.api;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageSender {

    private final ProxyServer proxy;

    public MessageSender(ProxyServer proxy) {
        this.proxy = proxy;
    }

    public void sendToAllServers(String subCommand) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);

        try {
            out.writeUTF(subCommand);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] message = byteStream.toByteArray();

        for (ServerInfo serverInfo : proxy.getServers().values()) {
            if (!serverInfo.getPlayers().isEmpty()) {
                ProxiedPlayer player = serverInfo.getPlayers().iterator().next();
                player.getServer().sendData("my:channel", message);
            }
        }
    }
}
