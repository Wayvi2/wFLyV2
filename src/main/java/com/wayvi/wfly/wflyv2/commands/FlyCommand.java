package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;

import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.ConfigEnum;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.storage.models.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to toggle flight for a player.
 */
public class FlyCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final PvPListener pvpListener;

    /**
     * Constructs the FlyCommand.
     *
     * @param plugin      The main plugin instance.
     * @param pvpListener Listener to check for nearby players in PvP.
     */
    public FlyCommand(WFlyV2 plugin, PvPListener pvpListener) {
        super(plugin, "fly.fly");
        setDescription("Fly command");
        setUsage("/fly");
        setPermission(Permissions.FLY.getPermission());
        this.pvpListener = pvpListener;
        this.plugin = plugin;

        //create alias by config
        List<String> lst = plugin.getConfigFile().get(ConfigEnum.COMMAND_ALIAS);
        for (String s : lst) {
            s = s.replaceAll("\\s+", ".");
            addAlias(s);
        }
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Player player = (Player) commandSender;
        tryActivateFly(player);
    }

    public boolean tryActivateFly(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            String message = plugin.getMessageFile().get(MessageEnum.NO_SPECTATOR);
            ColorSupportUtil.sendColorFormat(player, message);
            return false;
        }

        boolean playersInFly = WflyApi.get().getTimeFlyManager().getIsFlying(player.getUniqueId());

        if (playersInFly) {
            String message = plugin.getMessageFile().get(MessageEnum.FLY_DEACTIVATED);
            ColorSupportUtil.sendColorFormat(player, message);
            WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), false);
            return false;
        }

        boolean hasInfiniteFly = player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp();
        boolean hasBypass = player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.isOp();

        if (!hasInfiniteFly) {
            if (WflyApi.get().getTimeFlyManager().getTimeRemaining(player) == 0) {
                String message = plugin.getMessageFile().get(MessageEnum.NO_TIMEFLY_REMAINING);
                ColorSupportUtil.sendColorFormat(player, message);
                return false;
            }
        }

        if (!hasBypass) {
            if (pvpListener.HasNearbyPlayers(player)) {
                String message = plugin.getMessageFile().get(MessageEnum.PLAYER_IN_RANGE);
                ColorSupportUtil.sendColorFormat(player, message);
                return false;
            }

            if (!WflyApi.get().getConditionManager().isFlyAuthorized(player)) {
                String message = plugin.getMessageFile().get(MessageEnum.NO_FLY_HERE);
                ColorSupportUtil.sendColorFormat(player, message);
                return false;
            }
        }

        WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), true);
        String message = plugin.getMessageFile().get(MessageEnum.FLY_ACTIVATED);
        ColorSupportUtil.sendColorFormat(player, message);
        return true;

    }

}
