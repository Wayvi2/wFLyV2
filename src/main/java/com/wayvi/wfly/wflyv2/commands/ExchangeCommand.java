package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExchangeCommand extends Command<WFlyV2> {

    private WFlyV2 plugin;
    private ConfigUtil configUtil;

    public ExchangeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.give");
        addArgs("receiver", Player.class);
        addArgs("time", Integer.class);

        this.plugin = plugin;
        this.configUtil = configUtil;

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        Player donator = (Player) commandSender;
        Player receiver = arguments.get("receiver");
        int time = arguments.get("time");

        if (receiver == donator) {
            String message = configUtil.getCustomMessage().getString("message.exchange-cannot-the-same");
            ColorSupportUtil.sendColorFormat(donator, message);
        }

        int tempflyDonator = WflyApi.get().getTimeFlyManager().getTimeRemaining(donator);
        int tempflyReceiver = WflyApi.get().getTimeFlyManager().getTimeRemaining(receiver);

        if (tempflyDonator >= time) {
            WflyApi.get().getExchangeManager().exchangeTimeFly(donator, receiver, time);
        } else {
            ColorSupportUtil.sendColorFormat(donator, configUtil.getCustomMessage().getString("message.exchange-time-out"));
        }

        // receiver
        String messageReceiver = configUtil.getCustomMessage()
                .getString("message.exchange-receiver")
                .replace("%donator%", donator.getName())
                .replace("%time%", String.valueOf(time));

        ColorSupportUtil.sendColorFormat(receiver, messageReceiver);

        // donator
        String messageDonator = configUtil.getCustomMessage()
                .getString("message.exchange-donator")
                .replace("%receiver%", receiver.getName())
                .replace("%time%", String.valueOf(time));

        ColorSupportUtil.sendColorFormat(donator, messageDonator);
    }

}
