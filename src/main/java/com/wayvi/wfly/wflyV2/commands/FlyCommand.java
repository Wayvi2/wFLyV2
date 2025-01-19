package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FlyCommand extends Command<JavaPlugin> {

    FlyManager flyManager;
    MiniMessageSupportUtil miniMessageSupportUtil;
    ConfigUtil configUtil;

    public FlyCommand(JavaPlugin plugin, FlyManager flyManager, MiniMessageSupportUtil miniMessageSupportUtil, ConfigUtil configUtil) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.RELOAD);
        this.flyManager = flyManager;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
        this.configUtil = configUtil;

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        //MESSAGE
        String messageActivateFly = configUtil.getCustomMessage().getString("message.fly-activated");
        String messageDisabledFly = configUtil.getCustomMessage().getString("message.fly-deactivated");

        if (flyManager.isFlying((Player) commandSender)) {
            flyManager.manageFly((Player) commandSender, false);
            commandSender.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageDisabledFly));
        } else {
            flyManager.manageFly((Player) commandSender, true);
            commandSender.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageActivateFly));
        }
    }
}
