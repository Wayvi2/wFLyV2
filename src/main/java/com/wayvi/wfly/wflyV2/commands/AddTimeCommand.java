package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class AddTimeCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;

    public AddTimeCommand(WFlyV2 plugin) {
        super(plugin, "wfly.addtime");
        setDescription("Manage fly time for players");
        setUsage("/fly addtime <player> <time>");
        addArgs("player", Player.class);
        addArgs("time:int");
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {

        Player target = args.get("player");
        int time = args.get("time");
        plugin.getTimeFlyManager().upsertTimeFly(target, time);
        sender.sendMessage("ajouter " + time + " secondes");
    }
}
