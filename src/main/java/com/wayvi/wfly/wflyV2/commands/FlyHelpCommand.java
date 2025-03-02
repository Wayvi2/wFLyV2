package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class FlyHelpCommand extends Command<JavaPlugin> {

    private WFlyV2 plugin;

    public FlyHelpCommand(WFlyV2 plugin) {

        super(plugin, "wfly.help");
        setPermission(Permissions.RELOAD.getPermission());
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        commandSender.sendMessage("---------------------------------------------------");
        commandSender.sendMessage("See docs here :");
        commandSender.sendMessage("https://wayfly-documentation.gitbook.io/wayfly-wiki");
        commandSender.sendMessage("---------------------------------------------------");
    }
}
