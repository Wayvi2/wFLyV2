package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import com.wayvi.wfly.wflyv2.util.ConfigUtil;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class FlyPlayerCommands  extends Command<WFlyV2> {


    private final ConfigUtil configUtil;
    private final PvPListener pvpListener;
    /**
     * Constructs the FlyCommand.
     *
     * @param plugin                 The main plugin instance.
     * @param configUtil              Configuration utility for custom messages.
     * @param pvpListener             Listener to check for nearby players in PvP.
     */
    public FlyPlayerCommands(WFlyV2 plugin, ConfigUtil configUtil, PvPListener pvpListener) {
        super(plugin, "wfly.fly");
        addArgs("player", Player.class);
        setDescription("Fly command");
        setUsage("/wfly fly <player>");
        setPermission(Permissions.MANAGE_FLY.getPermission());
        this.configUtil = configUtil;
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
        Player player = arguments.get("player");
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



