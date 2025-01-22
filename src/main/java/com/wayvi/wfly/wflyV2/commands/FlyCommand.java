package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class FlyCommand extends Command<JavaPlugin> {

    private final FlyManager flyManager;
    private final MiniMessageSupportUtil miniMessageSupportUtil;
    private final ConfigUtil configUtil;


    public FlyCommand(JavaPlugin plugin, FlyManager flyManager, MiniMessageSupportUtil miniMessageSupportUtil, ConfigUtil configUtil) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.flyManager = flyManager;
        this.miniMessageSupportUtil = miniMessageSupportUtil;
        this.configUtil = configUtil;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;

        try {
            AccessPlayerDTO playersInFly = flyManager.getIsInFlyBeforeDeconnect(player);

            String messageFly = playersInFly.isinFly() ? configUtil.getCustomMessage().getString("message.fly-deactivated") : configUtil.getCustomMessage().getString("message.fly-activated");

            flyManager.manageFly(player, !playersInFly.isinFly());
            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(messageFly));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }
}
