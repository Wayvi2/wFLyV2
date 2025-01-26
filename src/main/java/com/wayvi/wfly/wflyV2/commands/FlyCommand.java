package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.MiniMessageSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class FlyCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;

    private MiniMessageSupportUtil miniMessageSupportUtil;

    private ConfigUtil configUtil;

    public FlyCommand(WFlyV2 plugin, MiniMessageSupportUtil miniMessageSupportUtil, ConfigUtil configUtil) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.plugin = plugin;
        this.miniMessageSupportUtil =miniMessageSupportUtil;
        this.configUtil = configUtil;

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;

        try {
            AccessPlayerDTO playersInFly = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());

            if(playersInFly.FlyTimeRemaining() == 0){
                player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(configUtil.getCustomMessage().getString("message.no-timefly-remaining")));
                return;
            }

            String message = playersInFly.isinFly() ? configUtil.getCustomMessage().getString("message.fly-deactivated") : configUtil.getCustomMessage().getString("message.fly-activated");
            plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
            player.sendMessage(miniMessageSupportUtil.sendMiniMessageFormat(message));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }
}
