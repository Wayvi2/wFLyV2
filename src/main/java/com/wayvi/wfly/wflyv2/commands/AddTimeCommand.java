package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

/**
 * Command to add fly time to a player.
 */
public class AddTimeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private ConfigUtil configUtil;

    /**
     * Constructs the AddTimeCommand.
     *
     * @param plugin     The main plugin instance.
     * @param configUtil Configuration utility to manage custom messages.
     */
    public AddTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.addtime");
        setDescription("Manage fly time for players");
        setUsage("/wfly addtime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time", Integer.class);
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Executes the command logic to add fly time to a player.
     *
     * @param commandSender The command sender (player or console).
     * @param arguments   The command arguments (player and time).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player target = arguments.get("player");
        int time = arguments.get("time");

        if (target.hasPermission(Permissions.INFINITE_FLY.getPermission())) {
            if (commandSender instanceof Player) {
                String unlimitedMsg = configUtil.getCustomMessage()
                        .getString("message.cannot-add-time-unlimited");
                ColorSupportUtil.sendColorFormat((Player) commandSender, unlimitedMsg);
                return;


            } else {
                plugin.getLogger().info("You cannot add fly time to " + target.getName() + " because they have unlimited fly time.");
                return;
            }
        }

        WflyApi.get().getTimeFlyManager().addFlytime(target, time);

        ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage()
                .getString("message.fly-time-added")
                .replace("%time%", String.valueOf(time)));

        if (commandSender instanceof Player) {
            Player playerSender = (Player) commandSender;
            ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage()
                    .getString("message.fly-time-added-to-player")
                    .replace("%time%", String.valueOf(time))
                    .replace("%player%", target.getName()));
        } else {
            plugin.getLogger().info("You have given " + time + " fly time to " + target.getName());
        }
    }
}
