package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.api.WflyApi;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command to remove fly time from a player.
 */
public class RemoveTimeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;

    /**
     * Constructs the RemoveTimeCommand.
     *
     * @param plugin     The main plugin instance.
     * @param configUtil Utility class for managing configuration files.
     */
    public RemoveTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.removetime");
        setDescription("Manage fly time for players.");
        setUsage("/fly removetime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time", Integer.class);
        setPermission(Permissions.REMOVE_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Executes the remove fly time command.
     *
     * @param sender The sender of the command.
     * @param args   The command arguments: player and time.
     */
    @Override
    public void execute(CommandSender sender, Arguments args) {
        Player target = args.get("player");
        int time = args.get("time");

        if (WflyApi.get().getTimeFlyManager().removeFlyTime((Player) sender, target, time)) {
            ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage()
                    .getString("message.fly-time-removed")
                    .replace("%time%", String.valueOf(time)));

            if (sender instanceof Player ) {
                Player playerSender = (Player) sender;
                ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage()
                        .getString("message.fly-time-remove-to-player")
                        .replace("%time%", String.valueOf(time))
                        .replace("%player%", target.getName()));
            } else {
                plugin.getLogger().info("You have removed " + time + " fly time from " + target.getName());
            }
        }
    }
}
