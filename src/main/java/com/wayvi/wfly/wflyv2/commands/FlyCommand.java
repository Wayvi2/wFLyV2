package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.managers.WConditionManager;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

/**
 * Command to toggle flight for a player.
 */
public class FlyCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final ConfigUtil configUtil;
    private final WConditionManager conditionWorldManager;
    private final PvPListener pvpListener;

    /**
     * Constructs the FlyCommand.
     *
     * @param plugin                 The main plugin instance.
     * @param configUtil              Configuration utility for custom messages.
     * @param conditionWorldManager   Manager for world-based fly restrictions.
     * @param pvpListener             Listener to check for nearby players in PvP.
     */
    public FlyCommand(WFlyV2 plugin, ConfigUtil configUtil, WConditionManager conditionWorldManager, PvPListener pvpListener) {
        super(plugin, "fly.fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.plugin = plugin;
        this.configUtil = configUtil;

        //create alias by config
        for (String s : configUtil.getCustomConfig().getStringList("command.alias")) {
            s = s.replaceAll("\\s+", ".");
            addAlias(s);
        }

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
            AccessPlayerDTO playersInFly = WflyApi.get().getFlyManager().getPlayerFlyData(player.getUniqueId());

            // Determine the message based on the current flight status
            String message = playersInFly.isinFly() ?
                    configUtil.getCustomMessage().getString("message.fly-deactivated") :
                    configUtil.getCustomMessage().getString("message.fly-activated");


            if (player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp()) {
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
                ColorSupportUtil.sendColorFormat(player, message);
                return;
            }

            // Check if player has remaining fly time
            if (WflyApi.get().getTimeFlyManager().getTimeRemaining(player) == 0) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-timefly-remaining"));
                return;
            }

            // Bypass permission check
            if (player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.isOp()) {
                WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
                ColorSupportUtil.sendColorFormat(player, message);
                return;
            }

            // Check if player is near other players in PvP
            if (pvpListener.HasNearbyPlayers(player)) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.player-in-range"));
                return;
            }

            // Check if fly is allowed in the current world
            if (!WflyApi.get().getConditionManager().isFlyAuthorized(player)) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-fly-here"));
                return;
            }


            // Toggle flight mode
            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
            ColorSupportUtil.sendColorFormat(player, message);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
