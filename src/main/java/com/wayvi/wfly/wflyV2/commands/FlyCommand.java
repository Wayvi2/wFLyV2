package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
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

    public FlyCommand(WFlyV2 plugin) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.plugin = plugin;

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;

        try {
            AccessPlayerDTO playersInFly = plugin.getFlyManager().getPlayerFlyData(player);


            plugin.getFlyManager().manageFly(player, !playersInFly.isinFly());
            plugin.getTimeFlyManager().decrementTimeRemaining(player, !playersInFly.isinFly());


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }
}
