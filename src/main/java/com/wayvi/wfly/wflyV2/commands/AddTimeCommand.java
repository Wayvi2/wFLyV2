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

public class AddTimeCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;


    ConfigUtil configUtil;

    public AddTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.addtime");
        setDescription("Manage fly time for players");
        setUsage("/fly addtime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time:int");
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {

        Player target = args.get("player");

        int time = args.get("time");
        try {
            plugin.getTimeFlyManager().addFlytime(target, time);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ColorSupportUtil.sendColorFormat(target,configUtil.getCustomMessage().getString("message.fly-time-added").replace("%time%", String.valueOf(time)));
        ColorSupportUtil.sendColorFormat((Player) sender, configUtil.getCustomMessage().getString("message.fly-time-added-to-player").replace("%time%", String.valueOf(time)).replace("%player%", target.getName())
        );
        plugin.getLogger().info("You have been given %time% timefly to " + target.getName());
    }
}
