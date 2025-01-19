package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FlyCommand extends Command<JavaPlugin> {

    FlyManager flyManager;

    public FlyCommand(JavaPlugin plugin, FlyManager flyManager) {
        super(plugin, "fly");
        this.flyManager = flyManager;
        setPermission(Permissions.RELOAD);

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        flyManager.manageFly((Player) commandSender, !flyManager.isFlying((Player) commandSender));
    }
}
