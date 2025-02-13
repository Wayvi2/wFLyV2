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

public class ResetTimeCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;

    ConfigUtil configUtil;

    public ResetTimeCommand(WFlyV2 plugin, ConfigUtil configUtil) {
        super(plugin, "wfly.resettime");
        setDescription("Manage fly time for players");
        setUsage("/fly addtime <player> <time>");
        addArgs("player", Player.class);
        setPermission(Permissions.ADD_RESET_TIME.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {

        Player target = args.get("player");

        if (target == null) {
            sender.sendMessage("Le joueur spécifié est introuvable.");
            return;
        }

        plugin.getTimeFlyManager().resetFlytime(target);
        ColorSupportUtil.sendColorFormat(target, configUtil.getCustomMessage().getString("message.fly-time-reset"));

        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            ColorSupportUtil.sendColorFormat(playerSender, configUtil.getCustomMessage().getString("message.fly-time-reset-to-player").replace("%player%", target.getName()));
        } else {
            sender.sendMessage("Vous avez réinitialisé le temps de vol de " + target.getName());
        }

        plugin.getLogger().info("Fly time reset for " + target.getName());
    }
}






