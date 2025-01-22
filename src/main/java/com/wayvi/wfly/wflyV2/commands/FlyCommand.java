package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.FlyManager;
import com.wayvi.wfly.wflyV2.managers.TimeFlyManager;
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
    private final TimeFlyManager timeFlyManager;

    public FlyCommand(WFlyV2 plugin, FlyManager flyManager) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.flyManager = flyManager;
        this.timeFlyManager = plugin.getTimeFlyManager();
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;

        try {
            AccessPlayerDTO playersInFly = flyManager.getPlayerFlyData(player);
            flyManager.manageFly(player, !playersInFly.isinFly());
            timeFlyManager.decrementTimeRemaining(player, !playersInFly.isinFly());

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }
}
