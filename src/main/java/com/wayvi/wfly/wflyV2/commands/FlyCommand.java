package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.managers.ConditionManager;
import com.wayvi.wfly.wflyV2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyV2.util.ConfigUtil;
import com.wayvi.wfly.wflyV2.util.ColorSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class FlyCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;

    private final ConfigUtil configUtil;

    ConditionManager conditionWorldManager;

    public FlyCommand(WFlyV2 plugin, ConfigUtil configUtil, ConditionManager conditionWorldManager) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        addAlias("wfly.fly");
        setPermission(Permissions.FLY.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.conditionWorldManager = conditionWorldManager;

    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;
        try {
            AccessPlayerDTO playersInFly = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());

            String message = playersInFly.isinFly() ? configUtil.getCustomMessage().getString("message.fly-deactivated") : configUtil.getCustomMessage().getString("message.fly-activated");

            if (player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.isOp()) {
                plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
                ColorSupportUtil.sendColorFormat(player, message);
                return;
            }

            if (playersInFly.FlyTimeRemaining() == 0) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-timefly-remaining"));
                return;
            }

            if (conditionWorldManager.cannotFly(player) && conditionWorldManager.canFly(player)) {
                plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
                ColorSupportUtil.sendColorFormat(player, message);
                return;
            }

            if (conditionWorldManager.cannotFly(player)) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-fly-here"));
                return;
            }

            if (conditionWorldManager.canFly(player)) {
                plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
                ColorSupportUtil.sendColorFormat(player, message);
                return;
            }

            plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
            ColorSupportUtil.sendColorFormat(player, message);



            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }
}
