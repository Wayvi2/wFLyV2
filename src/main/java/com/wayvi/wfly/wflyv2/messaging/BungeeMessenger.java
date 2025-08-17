package com.wayvi.wfly.wflyv2.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class BungeeMessenger implements PluginMessageListener {

    private final WFlyV2 plugin;

    public BungeeMessenger(WFlyV2 plugin) {
        this.plugin = plugin;

    }


    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();


        if ("flyAddAll".equals(subchannel)) {
            handleFlyAddAll(in);
        }

        if ("flyRemoveAll".equals(subchannel)) {
            handleFlyRemoveAll(in);
        }
    }

    private void handleFlyAddAll(ByteArrayDataInput in) {
        String data = in.readUTF();
        String[] parts = data.split(":", 2);

        try {
            UUID senderId = UUID.fromString(parts[0]);
            if (senderId.equals(plugin.getServerId())) return;

            int time = Integer.parseInt(parts[1]);
            WflyApi.get().getTimeFlyManager().addFlytimeForAllPlayers(time);

            String messageTime = plugin.getMessageFile().get(MessageEnum.FLY_TIME_ADDED);
            for (Player p : Bukkit.getOnlinePlayers()) {
                ColorSupportUtil.sendColorFormat(p, messageTime.replace("%time%", String.valueOf(time)));
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            // Ignore invalid data
        }
    }

    private void handleFlyRemoveAll(ByteArrayDataInput in) {
        String data = in.readUTF();
        String[] parts = data.split(":", 2);

        try {
            UUID senderId = UUID.fromString(parts[0]);
            if (senderId.equals(plugin.getServerId())) return;

            int time = Integer.parseInt(parts[1]);
            WflyApi.get().getTimeFlyManager().removeFlytimeForAllPlayers(time);

            String messageTime = plugin.getMessageFile().get(MessageEnum.FLY_TIME_REMOVED);
            for (Player p : Bukkit.getOnlinePlayers()) {
                ColorSupportUtil.sendColorFormat(p, messageTime.replace("%time%", String.valueOf(time)));
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            // Ignore invalid data
        }
    }



}


