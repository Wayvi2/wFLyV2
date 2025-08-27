package com.wayvi.wfly.wflyv2.commands;

import com.wayvi.wfly.wflyv2.WFlyV2;
import com.wayvi.wfly.wflyv2.api.WflyApi;
import com.wayvi.wfly.wflyv2.constants.Permissions;
import com.wayvi.wfly.wflyv2.constants.commands.ToggleType;
import com.wayvi.wfly.wflyv2.constants.configs.MessageEnum;
import com.wayvi.wfly.wflyv2.storage.AccessPlayerDTO;
import com.wayvi.wfly.wflyv2.storage.sql.FlyTimeRepository;
import com.wayvi.wfly.wflyv2.util.ColorSupportUtil;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleFlyPlayerCommand extends Command<WFlyV2> {

    private final WFlyV2 plugin;
    private final FlyCommand flyCommand;


    public ToggleFlyPlayerCommand(WFlyV2 plugin, FlyCommand flyCommand) {
        super(plugin, "wfly");
        this.plugin = plugin;
        this.flyCommand = flyCommand;

        addArgs("state", ToggleType.class);
        addArgs("player", Player.class);
        setPermission(Permissions.TOGGLE_FLY.getPermission());
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        ToggleType state = arguments.get("state");
        Player targetPlayer = arguments.get("player");


        if (state == null || targetPlayer == null) {
            String message = plugin.getMessageFile().get(MessageEnum.ARG_NOT_RECOGNIZED);
            if (sender instanceof Player) {
                ColorSupportUtil.sendColorFormat((Player) sender, message);
            } else {
                plugin.getLogger().warning(message);
            }
            return;
        }

        boolean toActivate = state == ToggleType.ACTIVATE;

        if (toActivate) {
            boolean activated = flyCommand.tryActivateFly(targetPlayer);

            if (activated) {
                String message = plugin.getMessageFile().get(MessageEnum.FLY_ACTIVATED_PLAYER);
                String msg = message.replace("%player%", targetPlayer.getName());

                if (sender instanceof Player) {
                    ColorSupportUtil.sendColorFormat((Player) sender, msg);
                } else {
                    plugin.getLogger().info(msg);
                }
            }

        } else {
            AccessPlayerDTO playerFlyData;
            playerFlyData = plugin.getStorage().getPlayerFlyData(targetPlayer.getUniqueId());

            if (!playerFlyData.isinFly()) {
                String message = plugin.getMessageFile().get(MessageEnum.PLAYER_NOT_IN_FLY);


                if (sender instanceof Player) {
                    ColorSupportUtil.sendColorFormat((Player) sender, message.replace("%player%", targetPlayer.getName()));
                } else {
                    plugin.getLogger().info(message.replace("%player%", targetPlayer.getName()));
                }
                return;
            }

            WflyApi.get().getFlyManager().manageFly(targetPlayer.getUniqueId(), false);

            String message = plugin.getMessageFile().get(MessageEnum.FLY_DEACTIVATED_PLAYER);
            String msgSender = message.replace("%player%", targetPlayer.getName());

            if (sender instanceof Player) {
                ColorSupportUtil.sendColorFormat((Player) sender, msgSender);
            } else {
                plugin.getLogger().info(msgSender);
            }

            String msgTarget = plugin.getMessageFile().get(MessageEnum.FLY_DEACTIVATED);
            if (msgTarget != null) {
                ColorSupportUtil.sendColorFormat(targetPlayer, msgTarget);
            }
        }
    }
}
