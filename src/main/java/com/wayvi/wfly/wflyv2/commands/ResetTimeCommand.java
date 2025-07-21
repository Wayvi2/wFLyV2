package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to reset a player's fly time.
 */
public class ResetTimeCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;

    /**
     * Constructs the ResetTimeCommand.
     *
     * @param plugin     The main plugin instance.
     * @param configUtil Utility class for managing configuration files.
     */
    public ResetTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.resettime");
        setDescription("Reset a player's fly time.");
        setUsage("/fly resettime <player>");
        addArgs("player", Player.class);
        setPermission(Permissions.ADD_RESET_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    /**
     * Executes the reset fly time command.
     *
     * @param sender The sender of the command.
     * @param args   The command arguments: player.
     */
    @Override
    public void execute(CommandSender sender, Arguments args) {
        Player target = args.get("player");

        if (target == null) {
            sender.sendMessage("Le joueur spécifié est introuvable.");
            return;
        }

        WflyApi.get().getTimeFlyManager().resetFlytime(target);
        ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage().getString("message.fly-time-reset"));

        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage()
                    .getString("message.fly-time-reset-to-player")
                    .replace("%player%", target.getName()));
        } else {
            sender.sendMessage("Vous avez réinitialisé le temps de vol de " + target.getName());
        }

        plugin.getLogger().info("Fly time reset for " + target.getName());
    }
}
