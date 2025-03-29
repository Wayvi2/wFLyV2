package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

/**
 * Command to add fly time to a player.
 */
public class AddTimeCommand extends Command<JavaPlugin> {

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
        addArgs("time:int");
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Executes the command logic to add fly time to a player.
     *
     * @param sender The command sender (player or console).
     * @param args   The command arguments (player and time).
     */
    @Override
    public void execute(CommandSender sender, Arguments args) {
        Player target = args.get("player");
        int time = args.get("time");

        try {
            plugin.getTimeFlyManager().addFlytime(target, time);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage()
                .getString("message.fly-time-added")
                .replace("%time%", String.valueOf(time)));

        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage()
                    .getString("message.fly-time-added-to-player")
                    .replace("%time%", String.valueOf(time))
                    .replace("%player%", target.getName()));
        } else {
            plugin.getLogger().info("You have given " + time + " fly time to " + target.getName());
        }
    }
}
