package com.wayvi.wfly.wflyV2.commands;

import com.wayvi.wfly.wflyV2.WFlyV2;
import com.wayvi.wfly.wflyV2.constants.Permissions;
import com.wayvi.wfly.wflyV2.listeners.PvPListener;
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

/**
 * Command to toggle flight for a player.
 */
public class FlyCommand extends Command<JavaPlugin> {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final ConditionManager conditionWorldManager;
    private final PvPListener pvpListener;

    /**
     * Constructs the FlyCommand.
     *
     * @param plugin                 The main plugin instance.
     * @param configUtil              Configuration utility for custom messages.
     * @param conditionWorldManager   Manager for world-based fly restrictions.
     * @param pvpListener             Listener to check for nearby players in PvP.
     */
    public FlyCommand(WFlyV2 plugin, ConfigUtil configUtil, ConditionManager conditionWorldManager, PvPListener pvpListener) {
        super(plugin, "fly");
        setDescription("Fly command");
        setUsage("/fly");
        addAlias("wfly.fly");
        setPermission(Permissions.FLY.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.conditionWorldManager = conditionWorldManager;
        this.pvpListener = pvpListener;
    }

    /**
     * Executes the fly command logic.
     *
     * @param commandSender The command sender (must be a player).
     * @param arguments     The command arguments (none required).
     */
    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;
        try {
            AccessPlayerDTO playersInFly = plugin.getFlyManager().getPlayerFlyData(player.getUniqueId());

            // Determine the message based on the current flight status
            String message = playersInFly.isinFly() ?
                    configUtil.getCustomMessage().getString("message.fly-deactivated") :
                    configUtil.getCustomMessage().getString("message.fly-activated");


            // Check if player has remaining fly time
            if (plugin.getTimeFlyManager().getTimeRemaining(player) == 0) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-timefly-remaining"));
                return;
            }

            // Bypass permission check
            if (player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.isOp()) {
                plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
                ColorSupportUtil.sendColorFormat(player, message);
                return;
            }

            // Check if player is near other players in PvP
            if (pvpListener.HasNearbyPlayers(player)) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.player-in-range"));
                return;
            }

            // Check if fly is allowed in the current world
            if (!conditionWorldManager.isFlyAuthorized(player)) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-fly-here"));
                return;
            }


            // Toggle flight mode
            plugin.getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
            ColorSupportUtil.sendColorFormat(player, message);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
