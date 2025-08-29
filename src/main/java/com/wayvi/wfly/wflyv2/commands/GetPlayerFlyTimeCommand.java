package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GetPlayerFlyTimeCommand extends Command<WFlyV2> {

    private WFlyV2 plugin;
    private WFlyPlaceholder placeholder;

    public GetPlayerFlyTimeCommand(WFlyV2 plugin, WFlyPlaceholder placeholder) {
        super(plugin, "fly.time");
        addAlias("flytime");
        addOptionalArgs("target", Player.class);
        setPermission(Permissions.GET_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.placeholder = placeholder;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        Optional<Player> targetOptional = arguments.getOptional("target");

        Player target = targetOptional.orElse(null);

        if (!(commandSender instanceof Player)) {
            if (target == null) {
                commandSender.sendMessage(ChatColor.DARK_RED + "You must specify a valid online player when using this command from the console.");
                return;
            }

            int flyRemaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(target);

            if (target.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                String rawMessage = plugin.getMessageFile().get(MessageEnum.PLAYER_HAS_UNLIMITED);

                String formattedMessage = rawMessage.replace("%player%", target.getName());
                ColorSupportUtil.sendColorFormat((Player) commandSender, formattedMessage);
                return;
            }
            String rawMessage = plugin.getMessageFile().get(MessageEnum.GET_PLAYER_FLY_TIME);

            String formattedMessage = rawMessage
                    .replace("%player%", target.getName())
                    .replace("%fly_remaining%", placeholder.formatTime(plugin,flyRemaining));

            ColorSupportUtil.sendColorFormat((Player) commandSender, formattedMessage);
            return;
        }

        Player sender = (Player) commandSender;

        if (target == null) {
            target = sender;
        }

        if (!target.equals(sender) && !sender.hasPermission(Permissions.GET_FLY_TIME_ADMIN.getPermission())) {
            String noPermissionMsg = plugin.getMessageFile().get(MessageEnum.ONLY_GET_HIS_FLY_TIME);
            ColorSupportUtil.sendColorFormat(sender, noPermissionMsg);
            return;
        }

        int flyRemaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(target);

        if (target.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
            String rawMessage = plugin.getMessageFile().get(MessageEnum.PLAYER_HAS_UNLIMITED);
            String formattedMessage = rawMessage.replace("%player%", target.getName());
            ColorSupportUtil.sendColorFormat(sender, formattedMessage);
            return;
        }


        String rawMessage = target.equals(sender) ? plugin.getMessageFile().get(MessageEnum.HAVE_YOUR_FLY_TIME) :  plugin.getMessageFile().get(MessageEnum.GET_PLAYER_FLY_TIME);
        String formattedMessage = rawMessage
                .replace("%player%", target.getName())
                .replace("%fly_remaining%", placeholder.formatTime(plugin,flyRemaining));

        ColorSupportUtil.sendColorFormat(sender, formattedMessage);
    }

}