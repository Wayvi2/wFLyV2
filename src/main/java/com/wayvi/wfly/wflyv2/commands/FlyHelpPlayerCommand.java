package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to display the help documentation link for the WayFly plugin.
 */
public class FlyHelpPlayerCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;

    /**
     * Constructs the FlyHelpCommand.
     *
     * @param plugin The main plugin instance.
     */
    public FlyHelpPlayerCommand(WFlyV2 plugin) {
        super(plugin, "fly.help");
        setPermission(Permissions.HELP_PLAYER.getPermission());
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

        List<String> message = plugin.getMessageFile().get(MessageEnum.HELP_MESSAGE_PLAYER);
        for (String s : message) {
            if (commandSender instanceof Player) {
                ColorSupportUtil.sendColorFormat((Player) commandSender, s);
            } else {
                commandSender.sendMessage((String) ColorSupportUtil.convertColorFormat(s));
            }
        }



    }
}
