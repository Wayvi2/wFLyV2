package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.listeners.PvPListener;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.storage.FlyTimeRepository;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class FlyPlayerCommands extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final PvPListener pvpListener;
    private FlyTimeRepository flyTimeRepository;
    /**
     * Constructs the FlyCommand.
     *
     * @param plugin                 The main plugin instance.
     * @param pvpListener             Listener to check for nearby players in PvP.
     */
    public FlyPlayerCommands(WFlyV2 plugin, PvPListener pvpListener, FlyTimeRepository flyTimeRepository) {
        super(plugin, "wfly.fly");
        addArgs("player", Player.class);
        setDescription("Fly command");
        setUsage("/wfly fly <player>");
        setPermission(Permissions.MANAGE_FLY.getPermission());
        this.plugin = plugin;
        this.pvpListener = pvpListener;
        this.flyTimeRepository = flyTimeRepository;
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

        AccessPlayerDTO playersInFly = flyTimeRepository.getPlayerFlyData(player.getUniqueId());

        String messageDeactivate = plugin.getMessageFile().get(MessageEnum.FLY_DEACTIVATED);
        String messageActivate = plugin.getMessageFile().get(MessageEnum.FLY_ACTIVATED);

        String message = playersInFly.isinFly() ?
                messageDeactivate :
                messageActivate;

        if (player.getGameMode() == GameMode.SPECTATOR) {
            String messageSpectator = plugin.getMessageFile().get(MessageEnum.NO_SPECTATOR);
            ColorSupportUtil.sendColorFormat(player, messageSpectator);
            return;
        }
        boolean hasInfiniteFly = player.hasPermission(Permissions.INFINITE_FLY.getPermission()) || player.isOp();

        if (!hasInfiniteFly) {
            if (WflyApi.get().getTimeFlyManager().getTimeRemaining(player) == 0) {
                String messageNoTimeFly = plugin.getMessageFile().get(MessageEnum.NO_TIMEFLY_REMAINING);
                ColorSupportUtil.sendColorFormat(player, messageNoTimeFly);
                return;
            }
        }

        boolean hasBypass = player.hasPermission(Permissions.BYPASS_FLY.getPermission()) || player.isOp();

        if (!hasBypass) {
            if (pvpListener.HasNearbyPlayers(player)) {
                String messagePlayerInRange = plugin.getMessageFile().get(MessageEnum.PLAYER_IN_RANGE);
                ColorSupportUtil.sendColorFormat(player, messagePlayerInRange);
                return;
            }

            if (!WflyApi.get().getConditionManager().isFlyAuthorized(player)) {
                String messageNoFlyHere = plugin.getMessageFile().get(MessageEnum.NO_FLY_HERE);
                ColorSupportUtil.sendColorFormat(player, messageNoFlyHere);
                return;
            }
        }
        WflyApi.get().getFlyManager().manageFly(player.getUniqueId(), !playersInFly.isinFly());
        ColorSupportUtil.sendColorFormat(player, message);

    }
}



