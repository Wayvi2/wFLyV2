package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.placeholders.WFlyPlaceholder;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetPlayerFlyTimeCommand extends Command<WFlyV2> {

    private WFlyV2 plugin;
    private ConfigUtil configUtil;
    private WFlyPlaceholder placeholder;

    public GetPlayerFlyTimeCommand(WFlyV2 plugin, ConfigUtil configUtil, WFlyPlaceholder placeholder) {
        super(plugin, "flytime");
        addOptionalArgs("target", Player.class);
        setPermission(Permissions.GET_FLY_TIME.getPermission());

        this.configUtil = configUtil;
        this.plugin = plugin;
        this.placeholder = placeholder;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player target = arguments.get("target");

        // Si la commande est envoyée depuis la console
        if (!(commandSender instanceof Player)) {
            if (target == null) {
                commandSender.sendMessage(ChatColor.DARK_RED + "You must specify a player when using this command from the console.");
                return;
            }

            int flyRemaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(target);

            if (target.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
                String unlimitedMsg = configUtil.getCustomConfig()
                        .getString("format-placeholder.unlimited", "&aThis player has unlimited fly time.");
                commandSender.sendMessage(ChatColor.stripColor(unlimitedMsg));
                return;
            }

            String rawMessage = configUtil.getCustomMessage()
                    .getString("message.get-player-fly-time", "&e%player% has %fly_remaining% of fly time left.");

            String formattedMessage = rawMessage
                    .replace("%player%", target.getName())
                    .replace("%fly_remaining%", placeholder.formatTime(flyRemaining));

            commandSender.sendMessage(ChatColor.stripColor(formattedMessage));
            return;
        }

        // Sinon, c'est un joueur
        Player sender = (Player) commandSender;

        if (target == null) {
            target = sender;
        }

        // Si la cible est différente du joueur
        if (!target.equals(sender) && !sender.hasPermission(Permissions.GET_FLY_TIME_ADMIN.getPermission())) {
            String noPermissionMsg = configUtil.getCustomMessage()
                    .getString("message.only-get-his-fly-time", "&cYou can only view your own fly time.");
            ColorSupportUtil.sendColorFormat(sender, noPermissionMsg);
            return;
        }

        int flyRemaining = WflyApi.get().getTimeFlyManager().getTimeRemaining(target);

        if (target.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
            String unlimitedMsg = configUtil.getCustomConfig()
                    .getString("format-placeholder.unlimited", "&aYou have unlimited fly time.");
            ColorSupportUtil.sendColorFormat(sender, unlimitedMsg);
            return;
        }

        String messageKey = target.equals(sender) ? "message.have-your-fly-time" : "message.get-player-fly-time";

        String rawMessage = configUtil.getCustomMessage().getString(messageKey);

        String formattedMessage = rawMessage
                .replace("%player%", target.getName())
                .replace("%fly_remaining%", placeholder.formatTime(flyRemaining));

        ColorSupportUtil.sendColorFormat(sender, formattedMessage);
    }
}