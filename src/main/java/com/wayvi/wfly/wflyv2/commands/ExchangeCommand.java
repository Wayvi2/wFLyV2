package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.commands.TimeUnits;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExchangeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    public ExchangeCommand(WFlyV2 plugin) {
        super(plugin, "fly.give");
        addArgs("receiver", Player.class);
        addArgs("time", Integer.class);
        addArgs("units", TimeUnits.class);

        this.plugin = plugin;
        setPermission(Permissions.EXCHANGE_FLY_TIME.getPermission());
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be used by players.");
            return;
        }

        Player donator = (Player) commandSender;
        Player receiver = arguments.get("receiver");
        int basicTime = arguments.get("time");
        TimeUnits units = arguments.get("units");

        int time = TimeUnits.convertTimeToType(basicTime, units);

        if (plugin.getConfigFile().get(ConfigEnum.COOLDOWN_GIVE_LIMITS_ENABLED)) {
            int min = plugin.getConfigFile().get(ConfigEnum.COOLDOWN_GIVE_MIN);
            int max = plugin.getConfigFile().get(ConfigEnum.COOLDOWN_GIVE_MAX);

            if (time < min) {
                String message = plugin.getMessageFile().get(MessageEnum.EXCHANGE_TIME_BELOW_MINIMUM);

                ColorSupportUtil.sendColorFormat(donator, message.replace("%min%", String.valueOf(min)));
                return;
            }
            if (time > max) {
                String message = plugin.getMessageFile().get(MessageEnum.EXCHANGE_TIME_ABOVE_MAXIMUM);

                ColorSupportUtil.sendColorFormat(donator, message.replace("%max%", String.valueOf(max)));
                return;
            }

            if (WflyApi.get().getExchangeManager().getCooldown(donator) > 0) {
                String message = plugin.getMessageFile().get(MessageEnum.COOLDOWN_GIVE);
                ColorSupportUtil.sendColorFormat(donator, message);
                return;
            }

            if (time < 1) {
                String message = plugin.getMessageFile().get(MessageEnum.EXCHANGE_TIME_ZERO);
                ColorSupportUtil.sendColorFormat(donator, message);
                return;
            }

            if (receiver == donator) {
                String message = plugin.getMessageFile().get(MessageEnum.EXCHANGE_CANNOT_THE_SAME);
                ColorSupportUtil.sendColorFormat(donator, message);
                return;
            }

            int tempflyDonator = WflyApi.get().getTimeFlyManager().getTimeRemaining(donator);


            if (tempflyDonator < time) {
                String message = plugin.getMessageFile().get(MessageEnum.EXCHANGE_TIME_OUT);
                ColorSupportUtil.sendColorFormat(donator, message);
                return;
            }


            WflyApi.get().getExchangeManager().exchangeTimeFly(donator, receiver, time);

            String messageReceiver = plugin.getMessageFile().get(MessageEnum.EXCHANGE_RECEIVER);
            ColorSupportUtil.sendColorFormat(receiver, messageReceiver.replace("%donator%", donator.getName()).replace("%time%", WFlyPlaceholder.formatTime(plugin,time)));

            String messageDonator = plugin.getMessageFile().get(MessageEnum.EXCHANGE_DONATOR);
            ColorSupportUtil.sendColorFormat(donator, messageDonator.replace("%receiver%", receiver.getName()).replace("%time%", WFlyPlaceholder.formatTime(plugin,time)));
        }
    }
}
