package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.TimeFlyManager;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AddTimeCommand extends Command<JavaPlugin> {


    private TimeFlyManager timeFlyManager;

    public AddTimeCommand(JavaPlugin plugin, TimeFlyManager timeFlyManager) {
        super(plugin, "wfly.addtime");
        setDescription("Fly command");
        setUsage("/wfly addtime ");
        addArgs("addtime:int", "player:player");
        setPermission(Permissions.ADD_FLY_TIME.getPermission());
        this.timeFlyManager = timeFlyManager;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = arguments.get("player");
        int time = arguments.get("addtime");
        timeFlyManager.upsertTimeFly(player, time);
        player.sendMessage("Added " + time + " seconds to " + player.getName());
    }
}
