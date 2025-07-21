package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;

import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

/**
 * Command to toggle flight for a player.
 */
public class FlyCommand extends Command<WFlyV2> {

    private final ConfigUtil configUtil;
    private final PvPListener pvpListener;

    /**
     * Constructs the FlyCommand.
     *
     * @param plugin      The main plugin instance.
     * @param configUtil  Configuration utility for custom messages.
     * @param pvpListener Listener to check for nearby players in PvP.
     */
    public FlyCommand(WFlyV2 plugin, ConfigUtil configUtil, PvPListener pvpListener) {
        super(plugin, "fly.fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.configUtil = configUtil;

        //create alias by config
        for (String s : configUtil.getCustomConfig().getStringList("command.alias")) {
            s = s.replaceAll("\\s+", ".");
            addAlias(s);
        }

        this.pvpListener = pvpListener;
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;
        try {
            AccessPlayerDTO playersInFly = WflyApi.get().getFlyManager().getPlayerFlyData(player.getUniqueId());

            String message = playersInFly.isinFly() ?
                    configUtil.getCustomMessage().getString("message.fly-deactivated") :
                    configUtil.getCustomMessage().getString("message.fly-activated");

            if (player.getGameMode() == GameMode.SPECTATOR) {
                ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-spectator"));
                return;
            }

            boolean hasInfiniteFly = player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp();

            if (!hasInfiniteFly) {
                if (WflyApi.get().getTimeFlyManager().getTimeRemaining(player) == 0) {
                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-timefly-remaining"));
                    return;
                }
            }

            boolean hasBypass = player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.isOp();

            if (!hasBypass) {
                if (pvpListener.HasNearbyPlayers(player)) {
                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.player-in-range"));
                    return;
                }

                if (!WflyApi.get().getConditionManager().isFlyAuthorized(player)) {
                    ColorSupportUtil.sendColorFormat(player, configUtil.getCustomMessage().getString("message.no-fly-here"));
                    return;
                }
            }
            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
            ColorSupportUtil.sendColorFormat(player, message);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
