package com.wayvi.wfly.wflyv2.commands.all;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class addAllTimeFlyCommand  extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private ConfigUtil configUtil;

    /**
     * Constructs the AddTimeCommand.
     *
     * @param plugin     The main plugin instance.
     * @param configUtil Configuration utility to manage custom messages.
     */
    public addAllTimeFlyCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.addall");
        setDescription("Manage fly time for all players");
        setUsage("/wfly addtime <player> <time>");
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
        int time = arguments.get("time");

        WflyApi.get().getTimeFlyManager().addFlytimeForAllPlayers(time);

        for (Player target : plugin.getServer().getOnlinePlayers()) {
            ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage()
                    .getString("message.fly-time-added")
                    .replace("%time%", String.valueOf(time)));
        }


        if (commandSender instanceof Player) {
            Player playerSender = (Player) commandSender;
            ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage().getString("message.fly-time-added-to-all-player").replace("%time%", String.valueOf(time)));
        } else {
            plugin.getLogger().info("You have given " + time + " fly time to all players");
        }
    }
}