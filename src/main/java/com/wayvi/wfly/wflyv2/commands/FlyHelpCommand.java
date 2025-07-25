package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;

/**
 * Command to display the help documentation link for the WayFly plugin.
 */
public class FlyHelpCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    /**
     * Constructs the FlyHelpCommand.
     *
     * @param plugin The main plugin instance.
     */
    public FlyHelpCommand(WFlyV2 plugin) {
        super(plugin, "wfly.help");
        setPermission(Permissions.RELOAD.getPermission());
        this.plugin = plugin;
    }

    /**
     * Executes the help command, displaying the documentation link.
     *
     * @param commandSender The sender of the command.
     * @param arguments     The command arguments (none required).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        commandSender.sendMessage("---------------------------------------------------");
        commandSender.sendMessage("See docs here:");
        commandSender.sendMessage("https://wayfly-documentation.gitbook.io/wayfly-wiki");
        commandSender.sendMessage("---------------------------------------------------");
    }
}
