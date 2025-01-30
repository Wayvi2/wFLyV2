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

public class ResetTimeCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;

    ConfigUtil configUtil;

    public ResetTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
    super(plugin, "wfly.resettime");
    setDescription("Manage fly time for players");
    setUsage("/fly addtime <player> <time>");
    addArgs("player",Player.class);
    setPermission(Permissions.ADD_RESET_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
}

@Override
public void execute(CommandSender sender, Arguments args) {

    Player target = args.get("player");

    try {
        plugin.getTimeFlyManager().resetFlytime(target);
        MiniMessageSupportUtil.sendMiniMessageFormat(target,configUtil.getCustomMessage().getString("message.fly-time-reset"));

    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
}





