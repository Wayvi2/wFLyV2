package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class RemoveTimeCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;

    ConfigUtil configUtil;

    public RemoveTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.removetime");
        setDescription("Manage fly time for players");
        setUsage("/fly addtime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time:int");
        setPermission(Permissions.REMOVE_FLY_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {

        Player target = args.get("player");

        int time = args.get("time");
        if (plugin.getTimeFlyManager().removeFlyTime(target, time)){
            MiniMessageSupportUtil.sendMiniMessageFormat(target,configUtil.getCustomMessage().getString("message.fly-time-added").replace("%time%", String.valueOf(time)));
        }


    }
}



